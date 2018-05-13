package com.encrypt.exception;

/*
 *
 * @author Ram Narasimhan
 * Created on 2005
 *
 * Copyright (c) 2005 Boise Office Solutions
 * 800 W. Bryn Marw Ave, Itasca, IL 60015
 * All Rights Reserved.

 *
 */

import java.io.PrintWriter;

public class RecoverableException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Throwable baseThrowable = null;

	/**
	 * @param e
	 */
	public RecoverableException(String message, Throwable t) {
		super(message);
		baseThrowable = t;

	}

	public RecoverableException(String message) {
		super(message);

	}

	public Throwable getRootCause() {
		return baseThrowable;
	}

	@Override
	public String getMessage() {
		if (baseThrowable == null) {
			return super.getMessage();
		} else {
			return super.getMessage() + "; nested exception is:  \n\t" + baseThrowable.toString();
		}
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
		if (baseThrowable != null) {
			s.print("\n\t Underlying Throwable stack trace follows : \n\t");
			baseThrowable.printStackTrace(s);
		}
	}

	@Override
	public void printStackTrace() {
		this.printStackTrace(System.err);
	}

}
