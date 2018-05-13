package com.encrypt.exception;

public class FileMoveException extends RecoverableException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileMoveException(String message, Throwable t) {
		super(message, t);

	}

	public FileMoveException(String message) {
		super(message);

	}

}
