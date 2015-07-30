/**
 * 
 */
package com.bimaas.connect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONObject;

import com.bimaas.connect.constants.BimExConstants;
import com.bimaas.connect.data.DbConnectionManager;
import com.bimaas.connect.exception.BimExchangeException;

/**
 * This class is used to return the status of checkin ifc file.
 * 
 * <p>
 * Expecting reques { "request": { "checkinStatus": { "referenceId":"id" } } }
 * </p>
 * 
 * @author isuru
 * 
 */
public class CheckinStatusMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(CheckinStatusMediator.class);

	/**
	 * Mediate overridden method.
	 */
	@Override
	public boolean mediate(MessageContext context) {
		try {
			String jsonPayloadToString = JsonUtil
					.jsonPayloadToString(((Axis2MessageContext) context)
							.getAxis2MessageContext());
			JSONObject originalJsonBody = new JSONObject(jsonPayloadToString);

			String referenceId = new JSONObject(
					new JSONObject(originalJsonBody.getString("request"))
					.getString("checkinStatus"))
			.getString("referenceId");

			String query = "SELECT * FROM checkin_status WHERE reference_id='"
					+ referenceId + "'";

			JSONObject statusObj = executeSelect(query);
			JSONObject responseBody = new JSONObject();
			responseBody.put("response", statusObj);

			setResponse(context, responseBody.toString());

		} catch (Exception e) {
			LOG.error("Error occurred in updating status for Checkin Ifc \n"
					+ e);
			return false;
		}
		return true;
	}

	/**
	 * Retrieve and build the json response.
	 * 
	 * @param queury
	 *            select queury.
	 * @return json body build from the data retrieved from db.
	 * @throws BimExchangeException
	 *             custom exception.
	 */
	private JSONObject executeSelect(String queury) throws BimExchangeException {

		ResultSet resultSet = null;
		JSONObject response = new JSONObject();

		try {
			Connection connection = DbConnectionManager.getInstance(
					BimExConstants.BIMEX_DB_SCHEMA).getConnection();
			resultSet = connection.createStatement().executeQuery(queury);

			// Move to the first result item.
			resultSet.next();

			response.put("checkin_status_id",
					resultSet.getString("checkin_status_id"));
			response.put("poid", resultSet.getString("poid"));
			response.put("file_name", resultSet.getString("file_name"));
			response.put("reference_id", resultSet.getString("reference_id"));
			response.put("status", resultSet.getString("status"));
			response.put("last_updated_timestamp",
					resultSet.getString("last_updated_timestamp"));
			response.put("info", resultSet.getString("info"));

		} catch (BimExchangeException e) {
			throw e;
		} catch (SQLException e) {
			throw new BimExchangeException(
					"SQL error occurred in Data Agent. ", e);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Data retrieved from db: " + response.toString());
		}
		return response;
	}

	/**
	 * Set the response to message context.
	 * 
	 * @param messageContext
	 * @param responseBody
	 *            json body to set.
	 * @throws AxisFault
	 *             Exception.
	 */
	private void setResponse(MessageContext messageContext, String responseBody)
			throws AxisFault {
		JsonUtil.newJsonPayload(
				((Axis2MessageContext) messageContext).getAxis2MessageContext(),
				responseBody, true, true);
	}
}
