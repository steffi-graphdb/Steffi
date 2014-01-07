package com.imgraph.tests.titan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
