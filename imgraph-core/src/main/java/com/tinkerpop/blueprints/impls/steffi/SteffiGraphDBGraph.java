/*******************************************************************************
 * Copyright (c) 2014 EURA NOVA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Aldemar Reynaga - initial API and implementation
 *     Salim Jouili - initial API and implementation
 ******************************************************************************/
package com.tinkerpop.blueprints.impls.steffi;

import com.steffi.index.ImgIndex;
import com.steffi.model.Cell;
import com.steffi.model.CellType;
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiGraph;
import com.steffi.model.SteffiVertex;
import com.steffi.storage.CellTransaction.TransactionConclusion;
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
public class SteffiGraphDBGraph implements Graph, TransactionalGraph, IndexableGraph {
	
	private SteffiGraph rawGraph;

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
        public static final SteffiGraphDBGraph instance = new SteffiGraphDBGraph();
	}
	
	
	public static SteffiGraphDBGraph getInstance() {
		return SingletonHolder.instance;
	}

	
	private SteffiGraphDBGraph() {
		this.rawGraph = SteffiGraph.getInstance();
	}
	
	public SteffiGraph getRawGraph() {
		return rawGraph;
	}


	@Override
	public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String name) {
		
		SteffiVertex outImgVertex = ((SteffiGraphDBVertex)outVertex).getRawVertex();
		SteffiVertex inImgVertex = ((SteffiGraphDBVertex)inVertex).getRawVertex();
		
		SteffiEdge rawEdge = outImgVertex.addEdge(inImgVertex, true, name);
		
		return new SteffiGraphDBEdge(rawEdge, this);
		
		
	}
	
	public Edge addUndirectedEdge(Object id, Vertex outVertex, Vertex inVertex, String name) {
		SteffiVertex outImgVertex = ((SteffiGraphDBVertex)outVertex).getRawVertex();
		SteffiVertex inImgVertex = ((SteffiGraphDBVertex)inVertex).getRawVertex();
		
		SteffiEdge rawEdge = outImgVertex.addEdge(inImgVertex, false, name);
		
		return new SteffiGraphDBEdge(rawEdge, this);
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
		
		Vertex vertex = new SteffiGraphDBVertex(this, this.rawGraph.addVertex(cellId, null));
		
		return vertex;
	}

	@Override
	public Edge getEdge(Object id) {
		Cell cell = null;
		SteffiVertex rawVertex = null;
		for (Long cellId : rawGraph.getCellIds()) {
			cell = rawGraph.retrieveCell(cellId);
			if (cell.getCellType().equals(CellType.VERTEX)) {
				rawVertex = (SteffiVertex) cell;
				for (SteffiEdge rawEdge : rawVertex.getEdges())
					if (new Long(rawEdge.getId()).equals(id))
						return new SteffiGraphDBEdge(rawEdge, this);
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
			
			return new SteffiGraphDBVertex(this, (SteffiVertex) cell);
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
		SteffiEdge rawEdge = ((SteffiGraphDBEdge)edge).getRawEdge();
		SteffiVertex rawVertex = (SteffiVertex) rawGraph.retrieveCell(rawEdge.getSourceCellId()); 
		
		rawVertex.removeEdge(rawEdge);
		
		
	}

	@Override
	public void removeVertex(Vertex vertex) {
		((SteffiVertex)((SteffiGraphDBVertex)vertex).cell).remove();
		
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
		
		if (indexClass.equals(SteffiGraphDBVertex.class)) {
			ImgIndex<SteffiVertex> rawIndex = rawGraph.createVertexIndex(indexName);
			SteffiGraphDBIndex<SteffiGraphDBVertex, SteffiVertex> index = new SteffiGraphDBIndex<SteffiGraphDBVertex, SteffiVertex>(rawIndex, 
					SteffiGraphDBVertex.class);
			return (Index<T>) index;
		} else if (indexClass.isAssignableFrom(SteffiGraphDBEdge.class)) {
			ImgIndex<SteffiEdge> rawIndex = rawGraph.createEdgeIndex(indexName);
			SteffiGraphDBIndex<SteffiGraphDBEdge, SteffiEdge> index = new SteffiGraphDBIndex<SteffiGraphDBEdge, SteffiEdge>(rawIndex, 
					SteffiGraphDBEdge.class);
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
		if (indexClass.equals(SteffiGraphDBVertex.class)) {
			ImgIndex<SteffiVertex> rawIndex = rawGraph.getIndex(indexName, SteffiVertex.class);
			SteffiGraphDBIndex<SteffiGraphDBVertex, SteffiVertex> index = new SteffiGraphDBIndex<SteffiGraphDBVertex, SteffiVertex>(rawIndex, 
					SteffiGraphDBVertex.class);
			return (Index<T>) index;
		} else if (indexClass.isAssignableFrom(SteffiGraphDBEdge.class)) {
			ImgIndex<SteffiEdge> rawIndex = rawGraph.getIndex(indexName, SteffiEdge.class);
			SteffiGraphDBIndex<SteffiGraphDBEdge, SteffiEdge> index = new SteffiGraphDBIndex<SteffiGraphDBEdge, SteffiEdge>(rawIndex, 
					SteffiGraphDBEdge.class);
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
