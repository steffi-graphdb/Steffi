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
package com.steffi.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;



/**
 * @author Aldemar Reynaga
 * Configuration properties of Imgraph, they are loaded from the deafult configuration.xml file included
 * in the resources folder or they can be read from a file specified on Imgraph start
 */
public class Configuration {

	private Properties properties;
	
	public static enum Key {
		NODE_PORT,
		MANAGER_PORT,
		MANAGER_IP,
		NUM_SEARCH_WORKERS,
		NUM_LOADERS,
		NUM_TRAVERSAL_WORKERS,
		NUM_SYNC_COMMAND_WORKERS,
		NUM_ASYNC_COMMAND_WORKERS,
		CACHE_CONFIG_FILE,
		GOSSIP_ROUTER_ADDRESS,
		LOG_FILE,
		ERROR_FILE,
		MANAGER_ZQM_SWAP,
		MANAGER_ZMQ_HWM,
		NUM_TEXT_FILE_LOADERS,
		USE_JTA_TRANSACTIONS,
		STORE_SERIALIZED_CELLS,
		LOG_STATISTICS,
		NUM_STORAGE_WORKERS,
		MAX_ENTRIES_EXPLORED,
		COMPRESS_CELLS,
		VIRTUAL_EDGES,
		MEMBER_IP_ADDRESSES,
		TCP_PING_PORT,
		MANAGER_IPS,
		
	}
	
	public Configuration() {
		properties = new Properties();
		try {
			
			properties.load(new InputStreamReader(getClass().getResourceAsStream("/config.properties")));
			setSystemProperties();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	private void setSystemProperties() {
		System.setProperty("imgraph.logfile", properties.getProperty(Key.LOG_FILE.toString()));
	}
	
	private static class SingletonHolder { 
        public static final Configuration instance = new Configuration();
	}
	
	public static void loadProperties(String fileName) throws FileNotFoundException, IOException {
		SingletonHolder.instance.properties = new Properties(); 
		SingletonHolder.instance.properties.load(new FileInputStream(fileName));
		SingletonHolder.instance.setSystemProperties();
	}
	
	
	public static String getProperty(Key key){
		return SingletonHolder.instance.properties.getProperty(key.toString()); 
	}
	
	
}
