package com.imgraph.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import com.imgraph.common.Configuration;

/**
 * @author Aldemar Reynaga
 * Functions to manage the Infinispan cache creation, retrieval and management
 */
public class CacheContainer {
	private static final String CONFIG_FILE = Configuration.getProperty(Configuration.Key.CACHE_CONFIG_FILE);

	
	private static final EmbeddedCacheManager CACHE_MANAGER;
	
	public static final String CELL_CACHE_NAME = "___cell-storage___";
	public static final String VERTEX_INDEX_CACHE_NAME = "___vertex-index-storage___";
	public static final String EDGE_INDEX_CACHE_NAME = "___edge-index-storage___";
	
	
	

	static {
		try {
			CACHE_MANAGER = new DefaultCacheManager(CONFIG_FILE);
			
		} catch (IOException e) {
			throw new RuntimeException("Unable to configure Infinispan", e);
		}
	}

	
	/**
	 * Retrieves the default cache.
	 * @param <K> type used as keys in this cache
	 * @param <V> type used as values in this cache
	 * @return a cache
	 */
	public static <K, V> Cache<K, V> getCache() {
		return CACHE_MANAGER.getCache();
	}

	/**
	 * Retrieves a named cache.
	 * @param cacheName name of cache to retrieve
	 * @param <K> type used as keys in this cache
	 * @param <V> type used as values in this cache
	 * @return a cache
	 */
	
	public static <K, V> Cache<K, V> getCache(String cacheName) {
		if (cacheName == null) throw new NullPointerException("Cache name cannot be null!");
		Cache<K,V> cache = CACHE_MANAGER.getCache(cacheName);
		
		return cache;
	}
	
	public static <K, V> Cache<K, V> getCellCache() {
		
		return CACHE_MANAGER.getCache(CELL_CACHE_NAME);
	}
	
	public static <K, V, T> Cache <K,V> createIndexCache(String indexName, Class<T> className) {
		if (CACHE_MANAGER.cacheExists(indexName))
			return CACHE_MANAGER.getCache(indexName);
		
		if (indexName.equals(CELL_CACHE_NAME) || indexName.equals(VERTEX_INDEX_CACHE_NAME) ||
				indexName.equals(EDGE_INDEX_CACHE_NAME))
			throw new RuntimeException("The name " + indexName + " cannot be used for an index, please provide another name");
		
		org.infinispan.configuration.cache.Configuration baseConfig = CACHE_MANAGER.getCacheConfiguration("___index-storage___");
		org.infinispan.configuration.cache.Configuration idxConfig = new ConfigurationBuilder().read(baseConfig).build();
		CACHE_MANAGER.defineConfiguration(indexName, idxConfig);
		Cache <K, V> cache = CACHE_MANAGER.getCache(indexName);
		cache.start();
		
		return cache;
	}
	
	
	
	
	
	/**
	 * Retrieves the embedded cache manager.
	 * @return a cache manager
	 */
	public static EmbeddedCacheManager getCacheContainer() {
		return CACHE_MANAGER;
	}
	
	
	public static List<String> getIndexCacheNames() {
		List<String> names = new ArrayList<String>();
		
		for (String cacheName : CACHE_MANAGER.getCacheNames()) {
			if (!cacheName.equals(CELL_CACHE_NAME))
				names.add(cacheName);
		}
		
		return names;
	}
	
}
