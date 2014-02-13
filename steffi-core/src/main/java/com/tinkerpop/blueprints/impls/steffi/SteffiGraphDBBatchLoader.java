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



import org.jgroups.util.UUID;

import com.steffi.common.ImgLogger;
import com.steffi.loader.TextFileLoader;
import com.steffi.networking.messages.LoadMessage.LoadFileType;
import com.steffi.storage.SteffipFileTools;

/**
 * @author Aldemar Reynaga
 * Blueprints point of access for the batch loader
 */
public class SteffiGraphDBBatchLoader {

		
			
	public static void loadImgFile(String fileName) {
		SteffiGraphDBGraph graph = SteffiGraphDBGraph.getInstance();
		SteffipFileTools fileLoader =  new SteffipFileTools(UUID.randomUUID().toString());
		try {
			fileLoader.readFromFile(graph, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { fileLoader.closeClientThreads();}catch(Exception x) {}
		}
	}
	
	public static void loadTextFile(String fileName, boolean directedEdges, boolean adjacentListFile) {
		TextFileLoader loader = new TextFileLoader();
		
		LoadFileType loadFileType = adjacentListFile?LoadFileType.ADJ_LIST_TEXT_FILE:
					LoadFileType.SIMPLE_TEXT_FILE;
		try {
			loader.load(fileName, loadFileType, directedEdges);
		} catch (Exception e) {
			ImgLogger.logError(e, "Error loading text file");
		} finally {
			loader.close();
		}
	}
	
}
