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
package com.tinkerpop.rexster.config;

import org.apache.commons.configuration.Configuration;
import org.infinispan.lifecycle.ComponentStatus;

import com.steffi.networking.NodeServer;
import com.steffi.storage.CacheContainer;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.steffi.SteffiGraphDBGraph;

/**
 * @author Aldemar Reynaga
 * Implementation of the Rexster specification to start an Imgraph data server inside a Rexster server 
 */
public class ImgraphGraphConfiguration implements GraphConfiguration {

	@Override
	public Graph configureGraphInstance(Configuration properties)
			throws GraphConfigurationException {
		String configFile = properties.getString("config-file", null);
		
		try {
			
			System.setProperty("java.net.preferIPv4Stack" , "true");
		
			if (configFile != null)
				com.steffi.common.Configuration.loadProperties(configFile);
			
			CacheContainer.getCacheContainer().start();
			CacheContainer.getCellCache().start();
			
			while (!CacheContainer.getCacheContainer().getStatus().equals(ComponentStatus.RUNNING));
			
			new Thread(new NodeServer()).start();
			
			Thread.sleep(2000);
			
			
		} catch (Exception x) {
			throw new GraphConfigurationException(x);
		}
		
		
		return SteffiGraphDBGraph.getInstance();
	}

}
