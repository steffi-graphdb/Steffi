package com.imgraph.storage;

import java.util.ArrayList;
import java.util.List;

import com.imgraph.model.Cell;

/**
 * @author Aldemar Reynaga
 * Groups a set of put and removal operations on an index. These operations are stored in the transaction
 * context for posterior applying on the backend index storage
 * @param <T>
 */
public class IndexOperation<T extends Cell> {
	private final String indexName;
	private final Class<T> className;
	private List<IndexOperationItem> newKeyValues;
	private List<IndexOperationItem> removedKeyValues;
	
	
	public List<IndexOperationItem> getNewKeyValues() {
		return newKeyValues;
	}

	public void setNewKeyValues(List<IndexOperationItem> newKeyValues) {
		this.newKeyValues = newKeyValues;
	}

	public List<IndexOperationItem> getRemovedKeyValues() {
		return removedKeyValues;
	}

	public void setRemovedKeyValues(List<IndexOperationItem> removedKeyValues) {
		this.removedKeyValues = removedKeyValues;
	}

	public String getIndexName() {
		return indexName;
	}

	public Class<T> getClassName() {
		return className;
	}

	public IndexOperation(String indexName, Class<T> className) {
		this.indexName = indexName;
		this.className = className;
	
	}
	
	public void addKeyValue(String key, Object value, T object) {
		if (newKeyValues == null)
			newKeyValues =  new ArrayList<IndexOperation<T>.IndexOperationItem>();
		newKeyValues.add(new IndexOperationItem(key, value, object));
	}
	
	public void removeKeyValue(String key, Object value, T object) {
		if (removedKeyValues == null)
			removedKeyValues =  new ArrayList<IndexOperation<T>.IndexOperationItem>();
		removedKeyValues.add(new IndexOperationItem(key, value, object));
	}
	
	public class IndexOperationItem {
		
		private final String key;
		private final Object value;
		private final T object;
		
		public IndexOperationItem(String key, Object value, T object) {
			this.key = key;
			this.value = value;
			this.object = object;
		}

		public String getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public T getObject() {
			return object;
		}
		
		
	}
	
	
	
	
}
