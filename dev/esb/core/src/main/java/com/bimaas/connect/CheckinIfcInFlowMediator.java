/**
 * 
 */
package com.bimaas.connect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
 * This class is used to Insert | Update the data record when an IFC file
 * checkin to the bim exchange.
 * 
 * @author isuru
 * 
 */
public class CheckinIfcInFlowMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(CheckinIfcInFlowMediator.class);

	/**
	 * Status of the file checkin.
	 */
	private String status;

	/**
	 * Operation INSERT | UPDATE.
	 */
	private String operation;

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

			String query = "";
			String referenceId = "";

			if ("INSERT".equalsIgnoreCase(operation)) {

				String poid = new JSONObject(
						new JSONObject(originalJsonBody.getString("request"))
						.getString("parameters")).getString("poid");

				String fileName = new JSONObject(
						new JSONObject(originalJsonBody.getString("request"))
						.getString("parameters")).getString("fileName");

				referenceId = poid
						+ "_"
						+ fileName
						+ "_"
						+ new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(Calendar.getInstance().getTime());

				originalJsonBody.put("referenceId", referenceId);
				context.setProperty("referenceId", referenceId);
				setResponse(context, originalJsonBody.toString());

				query = "INSERT INTO checkin_status (poid, file_name, reference_id, status) VALUES ('"
						+ poid
						+ "', '"
						+ fileName
						+ "', '"
						+ referenceId
						+ "', '" + status + "')";
			}

			if ("UPDATE".equalsIgnoreCase(operation)) {
				referenceId = originalJsonBody.getString("referenceId");
				query = "UPDATE checkin_status SET status='" + status
						+ "' WHERE reference_id='" + referenceId + "'";
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Creating or updating checkin status related to referenceId: "
						+ referenceId);
			}

			execute(query);

		} catch (Exception e) {
			LOG.error("Error occurred in Creating | Updating status for Checkin Ifc \n"
					+ e);
			return false;
		}
		return true;
	}

	/**
	 * Execute the given insert or update query.
	 * 
	 * @param query
	 *            query to be executed.
	 * @return true if success.
	 * @throws BimExchangeException
	 *             custom exception.
	 */
	private boolean execute(String query) throws BimExchangeException {

		Connection connection = null;
		Statement statement = null;

		try {
			connection = DbConnectionManager.getInstance(
					BimExConstants.BIMEX_DB_SCHEMA).getConnection();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Executing query: " + query);
			}
			statement = connection.createStatement();
			return statement.executeUpdate(query) > 0;

		} catch (BimExchangeException e) {
			LOG.error(e);
			throw e;
		} catch (SQLException e) {
			LOG.error(e);
			throw new BimExchangeException(
					"SQL error occurred in Data Agent. ", e);
		} finally {

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					LOG.error(e);
					throw new BimExchangeException(
							"Error in closing statement", e);
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					LOG.error(e);
					throw new BimExchangeException(
							"Error in closing connection", e);
				}
			}
		}
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

	/**
	 * @return the status
	 */
	public final String getStatus() {
		return status;
	}

	/**
	 * @param paramStatus
	 *            the status to set
	 */
	public final void setStatus(String paramStatus) {
		this.status = paramStatus.toUpperCase();
	}

	/**
	 * @return the operation
	 */
	public final String getOperation() {
		return operation;
	}

	/**
	 * @param paramOperation
	 *            the operation to set
	 */
	public final void setOperation(String paramOperation) {
		this.operation = paramOperation;
	}

}
