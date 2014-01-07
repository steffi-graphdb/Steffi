package com.imgraph.index;

import java.io.Serializable;


/**
 * @author Aldemar Reynaga
 * The objects used as keys for the ImgMap Index implementation.
 */
public class IndexKeyValue implements Serializable {
	
	private static final long serialVersionUID = 1163289497704345671L;
	private final String key;
	private final Object object;
	
	public IndexKeyValue(String key, Object object) {
		super();
		this.key = key;
		this.object = object;
		
		try {
			if (!object.getClass().getMethod("hashCode").getDeclaringClass().equals(object.getClass()))
				throw new RuntimeException("Object must implement equals and hash");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	public String getKey() {
		return key;
	}

	public Object getObject() {
		return object;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexKeyValue other = (IndexKeyValue) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}
	
		
	
	
}
