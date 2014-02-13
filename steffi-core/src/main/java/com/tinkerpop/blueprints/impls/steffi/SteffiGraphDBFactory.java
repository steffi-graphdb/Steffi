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



import com.steffi.Main;
import com.steffi.common.Configuration;
import com.steffi.networking.ClusterConfigManager;
import com.steffi.networking.NodeServer;
import com.steffi.storage.CacheContainer;

/**
 * @author Aldemar Reynaga
 * Main functions to control and access the Imgraph database
 */
public class SteffiGraphDBFactory {
	
	public static void startInfinispan() {
		CacheContainer.getCacheContainer().start();
		CacheContainer.getCellCache().start();
	}
	
	public static void stopInfinispan() {
		CacheContainer.getCellCache().stop();
		CacheContainer.getCacheContainer().stop();
	}
	
	public static void startSteffiGraphDBEngine() throws Exception {
		startSteffiGraphDBEngine(null);
	}
	
	public static void startSteffiGraphDBEngine(String configFile) throws Exception {
		
		if (configFile != null)
			Configuration.loadProperties(configFile);
		System.setProperty("java.net.preferIPv4Stack" , "true");
		CacheContainer.getCacheContainer().start();
		CacheContainer.getCellCache().start();
		new Thread(new NodeServer()).start();
		System.out.println("Imgraph engine started");
	}
	
	public static void stopSteffiGraphDBEngine() {
		Main.sendStopMessage(Configuration.getProperty(Configuration.Key.NODE_PORT));
		System.out.println("Imgraph engine stopped");
	}
	
	public static SteffiGraphDBGraph getInstance() {
		return SteffiGraphDBGraph.getInstance();
	}
	
	
	public static void configureCluster() {
		ClusterConfigManager configManager = new ClusterConfigManager();
		try {
			configManager.initialize();
			System.out.println("Configuration successfuly executed");
		} catch (Exception x) {
			x.printStackTrace();
		} finally {
			configManager.closeClientThreads();
			
		}
	}
}
