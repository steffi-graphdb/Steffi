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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Aldemar Reynaga
 * Functions to read strings from the standard input stream. This class is used by the basic console
 */
public class IOUtils {

	static InputStreamReader istream;

	static BufferedReader bufRead;
 
	static {
	 istream = new InputStreamReader(System.in) ;

	 bufRead = new BufferedReader(istream) ;
	}
	
	public static String readLine(String s) {
		System.out.print(s);
		String returnval = null;
		try {
			returnval =  bufRead.readLine();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return returnval;
	}
}
