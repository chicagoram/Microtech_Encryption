package com.encrypt.exception;

public class FileORNetworkNotAvailableException extends RecoverableException {

	private static final long serialVersionUID = 1L;

	public FileORNetworkNotAvailableException(String message, Throwable t) {
		super(message, t);
	}

	public FileORNetworkNotAvailableException(String message) {
		super(message);

	}

}
