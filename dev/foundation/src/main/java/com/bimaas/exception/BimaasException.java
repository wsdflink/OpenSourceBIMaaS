/**
 * 
 */
package com.bimaas.exception;

/**
 * @author isuru
 * 
 */
public class BimaasException extends Exception {

	/**
	 * Uid.
	 */
	private static final long serialVersionUID = -1706749230042912102L;

	public BimaasException() {
		super();
	}

	public BimaasException(String errorMessage) {
		super(errorMessage);
	}

	public BimaasException(Exception e) {
		super(e);
	}

	public BimaasException(String errorMessage, Exception e) {
		super(errorMessage, e);
	}

}
