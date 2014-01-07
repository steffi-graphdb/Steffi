package com.imgraph.testing;

/**
 * @author Aldemar Reynaga
 * Read client thread used for read throughput tests
 */
public class ReadClient implements Runnable {

	private final String fileName;
	
	public ReadClient(String fileName) {
		super();
		this.fileName = fileName;
	}


	@Override
	public void run() {
		try {
			TestTools.testReads(fileName, null, true);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	

}
