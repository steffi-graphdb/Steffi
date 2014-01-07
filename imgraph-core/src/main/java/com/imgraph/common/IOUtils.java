package com.imgraph.common;

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
