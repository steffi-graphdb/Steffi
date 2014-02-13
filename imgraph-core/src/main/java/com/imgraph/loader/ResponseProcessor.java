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
package com.imgraph.loader;

import com.imgraph.networking.messages.Message;

/**
 * @author Aldemar Reynaga
 * Defines the callback function to be called when the server receives a response for a previous sent message  
 */
public interface ResponseProcessor {

	public void processResponse(Message message);
}
