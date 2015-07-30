/**
 * 
 */
package com.bimaas.connect.exception;

import org.json.JSONObject;

;

/**
 * Builds the errors.
 * 
 * @author isuru
 * 
 */
public class ErrorBuilder {

	/**
	 * Build the error as a json object.
	 * 
	 * @param e
	 *            to be included.
	 * @return {@link JSONObject} to be return.
	 */
	public static JSONObject getErrorResponse(String e) {
		JSONObject errorResponse = new JSONObject();
		JSONObject error = new JSONObject();
		error.put("error", e);
		errorResponse.put("response", error);
		return errorResponse;
	}

	/**
	 * Build the warning as a json object.
	 * 
	 * @param e
	 *            to be included.
	 * @return {@link JSONObject} to be return.
	 */
	public static JSONObject getWarningResponse(String e) {
		JSONObject errorResponse = new JSONObject();
		JSONObject error = new JSONObject();
		error.put("warning", e);
		errorResponse.put("response", error);
		return errorResponse;
	}
}
