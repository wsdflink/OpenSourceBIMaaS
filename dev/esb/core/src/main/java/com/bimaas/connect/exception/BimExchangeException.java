/**
 * 
 */
package com.bimaas.connect.exception;

/**
 * @author isuru
 * 
 */
public class BimExchangeException extends Exception {

	/**
	 * Uid.
	 */
	private static final long serialVersionUID = -1706749230042912102L;

	public BimExchangeException() {
		super();
	}

	public BimExchangeException(String errorMessage) {
		super(errorMessage);
	}

	public BimExchangeException(Exception e) {
		super(e);
	}

	public BimExchangeException(String errorMessage, Exception e) {
		super(errorMessage, e);
	}

}
