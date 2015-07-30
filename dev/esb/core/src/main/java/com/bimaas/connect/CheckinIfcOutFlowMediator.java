/**
 * 
 */
package com.bimaas.connect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
 * This class is used to Update data record when an IFC file is successfully checkedin to the bim
 * exchange.
 * 
 * @author isuru
 * 
 */
public class CheckinIfcOutFlowMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(CheckinIfcOutFlowMediator.class);

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

			if (new JSONObject(originalJsonBody.getString("response"))
			.has("exception")) {

				LOG.warn("Exception response found from bim server\n"
						+ originalJsonBody);

				query = "UPDATE checkin_status SET status='FAILED', info='"
						+ originalJsonBody + "' "
						+ "WHERE status='QUEUED' OR status='WAITING' "
						+ "ORDER BY checkin_status_id ASC LIMIT 1";

			} else {

				LOG.info("Updating the status to SUCCESS, Response found from bim server\n"
						+ originalJsonBody);

				query = "UPDATE checkin_status SET status='SUCCESS' "
						+ "WHERE status='QUEUED' OR status='WAITING' "
						+ "ORDER BY checkin_status_id ASC LIMIT 1";
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Updating the status");
			}

			execute(query);

		} catch (Exception e) {
			LOG.error("Error occurred in updating status for Checkin Ifc \n"
					+ e);
			return false;
		}
		return true;
	}

	/**
	 * Execute the given query.
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

}
