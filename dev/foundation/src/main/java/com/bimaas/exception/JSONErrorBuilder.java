/**
 * 
 */
package com.bimaas.exception;

import org.json.JSONObject;

/**
 * Builds the errors and return a {@link JSONObject} with the error.
 * <p>
 * <code>
 * {"response":
 * 	  {"error|warning": "error details"}
 * }
 * </code>
 * </p>
 * 
 * @author isuru
 * 
 */
public class JSONErrorBuilder {

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
