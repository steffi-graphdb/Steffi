package com.imgraph.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Aldemar Reynaga
 * General functions for logging of events
 */
public abstract class ImgLogger {
	
	
	public enum LogLevel {
		INFO,
		ERROR,
		WARN,
		DEBUG,
	}
	
	
	public static void log(LogLevel level, String event) {
		Logger logger = LoggerFactory.getLogger(ImgLogger.class);
		
		switch (level) {
		case DEBUG:
			logger.debug(event);
			break;
		case WARN:
			logger.warn(event);
			break;
		case INFO:
			logger.info(event);
			break;
		default:
			break;
		}
	}
	
	public static void logError(Throwable error, String message) {
		Logger logger = LoggerFactory.getLogger(ImgLogger.class);
		//System.out.println("logging error...." + error.toString());
		logger.error(message, error);
	}
	
}
