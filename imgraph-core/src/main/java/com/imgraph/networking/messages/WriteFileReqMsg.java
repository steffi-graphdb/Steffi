package com.imgraph.networking.messages;



public class WriteFileReqMsg extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2355442677955450152L;
	private String fileNamePrefix;
	private String directory;
	
	
	public WriteFileReqMsg() {
		super(MessageType.WRITE_TO_FILE_REQ);
	}

	public String getFileNamePrefix() {
		return fileNamePrefix;
	}

	public void setFileNamePrefix(String fileNamePrefix) {
		this.fileNamePrefix = fileNamePrefix;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

}
