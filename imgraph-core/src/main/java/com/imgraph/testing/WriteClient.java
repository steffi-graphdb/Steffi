package com.imgraph.testing;

/**
 * @author Aldemar Reynaga
 * Process defined to execute parallel write tests
 */
public class WriteClient implements Runnable {

private final String fileName;
	
	public WriteClient(String fileName) {
		super();
		this.fileName = fileName;
	}


	@Override
	public void run() {
		try {
			TestTools.testWrites(fileName, null);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
}
