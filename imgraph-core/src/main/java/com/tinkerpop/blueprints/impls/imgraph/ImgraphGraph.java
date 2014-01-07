package com.tinkerpop.blueprints.impls.imgraph;

import com.imgraph.index.ImgIndex;
import com.imgraph.model.Cell;
import com.imgraph.model.CellType;
import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgGraph;
import com.imgraph.model.ImgVertex;
import com.imgraph.storage.CellTransaction.TransactionConclusion;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultGraphQuery;


/**
 * @author Aldemar Reynaga
 * Implementation of the Blueprints Graph specification
 */
public class ImgraphGraph implements Graph, TransactionalGraph, IndexableGraph {
	
	private ImgGraph rawGraph;

	private static final Features FEATURES = new Features();
	private static final Features PERSISTENT_FEATURES;

	static {
		FEATURES.supportsDuplicateEdges = false;
		FEATURES.supportsSelfLoops = true;
		FEATURES.supportsSerializableObjectProperty = true;
		FEATURES.supportsBooleanProperty = true;
		FEATURES.supportsDoubleProperty = true;
		FEATURES.supportsFloatProperty = true;
		FEATURES.supportsIntegerProperty = true;
		FEATURES.supportsPrimitiveArrayProperty = true;
		FEATURES.supportsUniformListProperty = true;
		FEATURES.supportsMixedListProperty = true;
		FEATURES.supportsLongProperty = true;
		FEATURES.supportsMapProperty = true;
		FEATURES.supportsStringProperty = true;

		FEATURES.ignoresSuppliedIds = false;
		FEATURES.isPersistent = false;
		FEATURES.isWrapper = false;

		FEATURES.supportsIndices = false;
		FEATURES.supportsKeyIndices = false;
		FEATURES.supportsVertexKeyIndex = false;
		FEATURES.supportsEdgeKeyIndex = false;
		FEATURES.supportsVertexIndex = false;
		FEATURES.supportsEdgeIndex = false;
		FEATURES.supportsTransactions = true;
		FEATURES.supportsVertexIteration = true;
		FEATURES.supportsEdgeIteration = true;
		FEATURES.supportsEdgeRetrieval = true;
		FEATURES.supportsVertexProperties = true;
		FEATURES.supportsEdgeProperties = true;
		FEATURES.supportsThreadedTransactions = false;

		PERSISTENT_FEATURES = FEATURES.copyFeatures();
		PERSISTENT_FEATURES.isPersistent = false;
	}
	
	
	private static class SingletonHolder { 
        public static final ImgraphGraph instance = new ImgraphGraph();
	}
	
	
	public static ImgraphGraph getInstance() {
		return SingletonHolder.instance;
	}

	
	private ImgraphGraph() {
		this.rawGraph = ImgGraph.getInstance();
	}
	
	public ImgGraph getRawGraph() {
		return rawGraph;
	}


	@Override
	public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String name) {
		
		ImgVertex outImgVertex = ((ImgraphVertex)outVertex).getRawVertex();
		ImgVertex inImgVertex = ((ImgraphVertex)inVertex).getRawVertex();
		
		ImgEdge rawEdge = outImgVertex.addEdge(inImgVertex, true, name);
		
		return new ImgraphEdge(rawEdge, this);
		
		
	}
	
	public Edge addUndirectedEdge(Object id, Vertex outVertex, Vertex inVertex, String name) {
		ImgVertex outImgVertex = ((ImgraphVertex)outVertex).getRawVertex();
		ImgVertex inImgVertex = ((ImgraphVertex)inVertex).getRawVertex();
		
		ImgEdge rawEdge = outImgVertex.addEdge(inImgVertex, false, name);
		
		return new ImgraphEdge(rawEdge, this);
	}
	

	@Override
	public Vertex addVertex(Object id) {
		
		Long cellId = null;
		
		if (id != null) {
			if (id instanceof Long)
				cellId = (Long) id;
			else if (id instanceof Integer)
				cellId = ((Integer) id).longValue();
			else
				throw new RuntimeException("Id must be a valid number");
		}
		
		Vertex vertex = new ImgraphVertex(this, this.rawGraph.addVertex(cellId, null));
		
		return vertex;
	}

	@Override
	public Edge getEdge(Object id) {
		Cell cell = null;
		ImgVertex rawVertex = null;
		for (Long cellId : rawGraph.getCellIds()) {
			cell = rawGraph.retrieveCell(cellId);
			if (cell.getCellType().equals(CellType.VERTEX)) {
				rawVertex = (ImgVertex) cell;
				for (ImgEdge rawEdge : rawVertex.getEdges())
					if (new Long(rawEdge.getId()).equals(id))
						return new ImgraphEdge(rawEdge, this);
			}
				
		}
		return null;
	}

	@Override
	public Iterable<Edge> getEdges() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<Edge> getEdges(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Features getFeatures() {
		return FEATURES;
	}
	
	@Override
	public Vertex getVertex(Object id) {
		
		Cell cell = null;
		
		if (id != null) {
			if (id instanceof Long)
				cell = rawGraph.retrieveCell((Long) id);
			else if (id instanceof Integer)
				cell = rawGraph.retrieveCell((Integer) id);
			else
				throw new RuntimeException("Id must be a valid Number");
		} else {
			throw new RuntimeException("Id cannot be null");
		}
			
			
			
		
		if (cell != null) {
			if (!cell.getCellType().equals(CellType.VERTEX)) {
				throw new RuntimeException("The provided id doesn't correspond to a vertex");
			}
			
			return new ImgraphVertex(this, (ImgVertex) cell);
		}
		
		return null;
	}

	@Override
	public Iterable<Vertex> getVertices() {
		
		
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<Vertex> getVertices(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeEdge(Edge edge) {
		ImgEdge rawEdge = ((ImgraphEdge)edge).getRawEdge();
		ImgVertex rawVertex = (ImgVertex) rawGraph.retrieveCell(rawEdge.getSourceCellId()); 
		
		rawVertex.removeEdge(rawEdge);
		
		
	}

	@Override
	public void removeVertex(Vertex vertex) {
		((ImgVertex)((ImgraphVertex)vertex).cell).remove();
		
	}

	@Override
	public void shutdown() {
		rawGraph.removeAll();
	}
	
	public void startTransaction() {
		rawGraph.startTransaction();
	}
	

	@Override
	public void stopTransaction(Conclusion conclusion) {
		rawGraph.stopTransaction(conclusion.equals(Conclusion.SUCCESS)?TransactionConclusion.COMMIT:
			TransactionConclusion.ROLLBACK);
	}
	
	

	@Override
	public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass,
			Parameter... indexParameters) {
		
		if (indexClass.equals(ImgraphVertex.class)) {
			ImgIndex<ImgVertex> rawIndex = rawGraph.createVertexIndex(indexName);
			ImgraphIndex<ImgraphVertex, ImgVertex> index = new ImgraphIndex<ImgraphVertex, ImgVertex>(rawIndex, 
					ImgraphVertex.class);
			return (Index<T>) index;
		} else if (indexClass.isAssignableFrom(ImgraphEdge.class)) {
			ImgIndex<ImgEdge> rawIndex = rawGraph.createEdgeIndex(indexName);
			ImgraphIndex<ImgraphEdge, ImgEdge> index = new ImgraphIndex<ImgraphEdge, ImgEdge>(rawIndex, 
					ImgraphEdge.class);
			return (Index<T>) index;
		} else {
			throw new RuntimeException("The index class must be either ImgraphVertex or ImgraphEdge");
		}
			
		
	}

	@Override
	public void dropIndex(String indexName) {
		rawGraph.removeIndex(indexName);
		
	}

	@Override
	public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
		if (indexClass.equals(ImgraphVertex.class)) {
			ImgIndex<ImgVertex> rawIndex = rawGraph.getIndex(indexName, ImgVertex.class);
			ImgraphIndex<ImgraphVertex, ImgVertex> index = new ImgraphIndex<ImgraphVertex, ImgVertex>(rawIndex, 
					ImgraphVertex.class);
			return (Index<T>) index;
		} else if (indexClass.isAssignableFrom(ImgraphEdge.class)) {
			ImgIndex<ImgEdge> rawIndex = rawGraph.getIndex(indexName, ImgEdge.class);
			ImgraphIndex<ImgraphEdge, ImgEdge> index = new ImgraphIndex<ImgraphEdge, ImgEdge>(rawIndex, 
					ImgraphEdge.class);
			return (Index<T>) index;
		} else {
			throw new RuntimeException("The index class must be either ImgraphVertex or ImgraphEdge");
		}
		
	}

	@Override
	public Iterable<Index<? extends Element>> getIndices() {
		
		throw new UnsupportedOperationException();
	}
	
	
	public void registerItemName(String name) {
		rawGraph.registerItemName(name);
	}


	@Override
	public void commit() {
		rawGraph.stopTransaction(TransactionConclusion.COMMIT);
	}


	@Override
	public void rollback() {
		rawGraph.stopTransaction(TransactionConclusion.ROLLBACK);
		
	}


	@Override
	public GraphQuery query() {
		return new DefaultGraphQuery(this);
	}
	

}
