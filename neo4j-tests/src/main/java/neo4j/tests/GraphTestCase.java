package neo4j.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class GraphTestCase {
	
	public enum LoadFileType {
		IMGP_FILE,
		SIMPLE_TEXT_FILE,
		ADJ_LIST_TEXT_FILE;
	}
	
	
	private String storageDirectory;
	private String graphFileName;
	private String testFileName;
	private String test3HFileName;
	private String test4HFileName;
	private String workDirectory;
	private boolean isDirected;
	private LoadFileType loadFileType;
	private long maxId;
	private int delay;
	
	
	
	public GraphTestCase(String propertyFile) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(propertyFile));
		
		
		storageDirectory = properties.getProperty("STORAGE_DIRECTORY");
		graphFileName = properties.getProperty("GRAPH_FILENAME");
		loadFileType = properties.getProperty("ADJACENT_GRAPH_FILENAME").equals("Y")?LoadFileType.ADJ_LIST_TEXT_FILE:
			LoadFileType.SIMPLE_TEXT_FILE;
		testFileName = properties.getProperty("TEST_FILENAME");
		test3HFileName = properties.getProperty("TEST_3H_FILENAME");
		test4HFileName = properties.getProperty("TEST_4H_FILENAME");
		workDirectory = properties.getProperty("WORK_DIRECTORY");
		isDirected = properties.getProperty("DIRECTED_GRAPH").equals("Y");
		
		maxId = Long.parseLong(properties.getProperty("MAX_GRAPH_ID"));
		delay = Integer.parseInt(properties.getProperty("DELAY"));
		
	}
	
	public GraphTestCase(LoadFileType loadFileType, String storageDirectory, String graphFileName, String testFileName,
			String test3hFileName, String test4hFileName, String workDirectory,
			boolean isDirected, long maxId, int delay) {
		
		this.setLoadFileType(loadFileType);
		this.storageDirectory = storageDirectory;
		this.graphFileName = graphFileName;
		this.testFileName = testFileName;
		test3HFileName = test3hFileName;
		test4HFileName = test4hFileName;
		this.workDirectory = workDirectory;
		this.isDirected = isDirected;
		
		this.maxId = maxId;
		this.delay = delay;
	}
	public String getGraphFileName() {
		return graphFileName;
	}
	public void setGraphFileName(String graphFileName) {
		this.graphFileName = graphFileName;
	}
	public String getTestFileName() {
		return testFileName;
	}
	public void setTestFileName(String testFileName) {
		this.testFileName = testFileName;
	}
	public String getTest3HFileName() {
		return test3HFileName;
	}
	public void setTest3HFileName(String test3hFileName) {
		test3HFileName = test3hFileName;
	}
	public String getTest4HFileName() {
		return test4HFileName;
	}
	public void setTest4HFileName(String test4hFileName) {
		test4HFileName = test4hFileName;
	}
	public String getWorkDirectory() {
		return workDirectory;
	}
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}
	public boolean isDirected() {
		return isDirected;
	}
	public void setDirected(boolean isDirected) {
		this.isDirected = isDirected;
	}
	
	public long getMaxId() {
		return maxId;
	}
	public void setMaxId(long maxId) {
		this.maxId = maxId;
	}
	public int getDelay() {
		return delay;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}
	public String getStorageDirectory() {
		return storageDirectory;
	}
	public void setStorageDirectory(String storageDirectory) {
		this.storageDirectory = storageDirectory;
	}
	public LoadFileType getLoadFileType() {
		return loadFileType;
	}
	public void setLoadFileType(LoadFileType loadFileType) {
		this.loadFileType = loadFileType;
	}
	
	
	

}
