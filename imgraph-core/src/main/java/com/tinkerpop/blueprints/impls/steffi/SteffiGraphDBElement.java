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

import java.util.HashSet;
import java.util.Set;

import com.steffi.model.Cell;
import com.tinkerpop.blueprints.Element;

/**
 * @author Aldemar Reynaga
 * Implementation of the Blueprints Element specification using the Cell class
 */
public abstract class SteffiGraphDBElement implements Element {

	protected final SteffiGraphDBGraph graph;
	protected Cell cell;
	
	public SteffiGraphDBElement(final SteffiGraphDBGraph graph) {
		this.graph = graph;
	}
	
	@Override
	public Object getId() {
		return cell.getId();
	}

	
	@SuppressWarnings("unchecked")
	public <T> T getProperty(final String key) {
		return (T) cell.getAttribute(key);
	}
	
	public void setProperty(String key, Object value) {
		//graph.autoStartTransaction();
		cell.putAttribute(key, value);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <T> T removeProperty(final String key) {
		return (T) cell.removeAttribute(key);
	}
	
	public Set<String> getPropertyKeys() {
		final Set<String> keys = new HashSet<String>();
        for (final String key : this.cell.getAttributeKeys()) {
            keys.add( key);
        }
        return keys;
	}
	

}
