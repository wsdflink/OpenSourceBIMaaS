/**
 * 
 */
package com.bimaas.connect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
import com.bimaas.connect.exception.ErrorBuilder;

/**
 * This class is used to update the information about the given project in bim
 * exchange scope.
 * <p>
 * Basically this mediator update the fields in the bim_exchange.project table.
 * Latitude, longitude and geo fence, which is set as property, be updated by
 * giving the poid of the project.
 * </p>
 * 
 * @author isuru
 * 
 */
public class UpdateBimExchangeMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(UpdateBimExchangeMediator.class);

	/**
	 * Mediate overridden method.
	 */
	@Override
	public boolean mediate(MessageContext context) {
		try {

			String userAccessToken = (String) context
					.getProperty("AUTHZ_USER_TOKEN");
			String poid = String.valueOf(context.getProperty("POID"));

			String userName = getUserName(userAccessToken);
			List<String> poids = getAccessibleProjectPoids(userName);

			String latitude = (String) context.getProperty("LATITUDE");
			String longitude = (String) context.getProperty("LONGITUDE");
			String geoFence = (String) context.getProperty("GEO_FENCE");

			if (poids.contains(poid)) {
				LOG.debug("Accessing poid: " + poid
						+ ", is permitted for user: " + userName);
				boolean success = updateTheProjectDetails(poid, latitude,
						longitude, geoFence);

				if (success) {
					JSONObject response = new JSONObject();
					JSONObject status = new JSONObject();

					status.put("status", "success");
					response.put("response", status);
					setResponse(context, response.toString());
				}

			} else {
				JSONObject response = new JSONObject();
				JSONObject result = new JSONObject();
				JSONObject userRightsExceptions = new JSONObject();

				userRightsExceptions
				.put("Exception",
						"User does not have permision to update the given project");
				result.put("result", userRightsExceptions);
				response.put("response", result);
				setResponse(context, response.toString());
			}

		} catch (BimExchangeException e) {
			String error = "Error occurred\n" + e;
			LOG.error(e);
			try {
				setResponse(context, ErrorBuilder.getErrorResponse(error)
						.toString());
			} catch (AxisFault e1) {
				e1.printStackTrace();
				return false;
			}
			// Making true because the error is set to the repsonse.
			return true;
		} catch (AxisFault e) {
			String error = "Error occurred in Setting the response \n" + e;
			LOG.error(e);
			try {
				setResponse(context, ErrorBuilder.getErrorResponse(error)
						.toString());
			} catch (AxisFault e1) {
				e1.printStackTrace();
				return false;
			}
			// Making true because the error is set to the repsonse.
			return true;

		} catch (Exception e) {
			String error = "Error occurred in Setting the response \n" + e;
			LOG.error(error);
			return false;
		}
		return true;
	}

	private boolean updateTheProjectDetails(String poid, String latitude,
			String longitude, String geoFence) throws BimExchangeException {
		LOG.info("Updating project: " + poid + ", lat: " + latitude
				+ ", long: " + longitude + ", geo_fence: " + geoFence);

		String query = "";
		List<String> keyValues = new ArrayList<String>();

		if (!latitude.isEmpty()) {
			keyValues.add("latitude=" + latitude);
		}
		if (!longitude.isEmpty()) {
			keyValues.add("longitude=" + longitude);
		}

		if (keyValues.size() == 1) {
			if (!geoFence.isEmpty()) {
				query = "UPDATE project SET " + keyValues.get(0)
						+ ", geo_fence=" + geoFence + " WHERE poid='" + poid
						+ "'";
			} else {
				query = "UPDATE project SET " + keyValues.get(0)
						+ " WHERE poid='" + poid + "'";
			}
		} else if (keyValues.size() == 2) {
			if (!geoFence.isEmpty()) {
				query = "UPDATE project SET " + keyValues.get(0) + ", "
						+ keyValues.get(1) + ", geo_fence=" + geoFence
						+ " WHERE poid='" + poid + "'";
			} else {
				query = "UPDATE project SET " + keyValues.get(0) + ", "
						+ keyValues.get(1) + " WHERE poid='" + poid + "'";
			}
		} else {
			if (!geoFence.isEmpty()) {
				query = "UPDATE project SET geo_fence=" + geoFence
						+ " WHERE poid='" + poid + "'";
			} else {
				LOG.info("Couldn't find any data to be udpated");
			}
		}

		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Updating the project details...\n" + query);
			}
			return executeInsertUpdate(query);
		} catch (BimExchangeException e) {
			LOG.error("Error in updating the project details");
			throw e;
		}
	}

	/**
	 * Return the name of the user for the given user access token.
	 * 
	 * @param userAccessToken
	 *            access token of the user.
	 * @return name of the user.
	 * @throws BimExchangeException
	 *             custom exception.
	 */
	private String getUserName(String userAccessToken)
			throws BimExchangeException {

		String query = "SELECT AUTHZ_USER FROM IDN_OAUTH2_ACCESS_TOKEN WHERE ACCESS_TOKEN='"
				+ userAccessToken.split(" ")[1]
						+ "' AND USER_TYPE = 'APPLICATION_USER' AND TOKEN_STATE='ACTIVE'";
		try {
			return executeSelect(query, "AUTHZ_USER",
					BimExConstants.APIM_DB_SCHEMA).get(0);

		} catch (BimExchangeException e) {
			LOG.error("Error occurred in selecting the user name for the user access token: \n"
					+ e);
			throw e;
		}

	}

	/**
	 * Return the accessible project poids.
	 * 
	 * @param userName
	 *            name of the suer.
	 * @return poid list.
	 * @throws BimExchangeException
	 */
	private List<String> getAccessibleProjectPoids(String userName)
			throws BimExchangeException {
		String query = "SELECT poid FROM project_rights WHERE authz_user='"
				+ userName + "'";
		return executeSelect(query, "poid", BimExConstants.BIMEX_DB_SCHEMA);
	}

	/**
	 * Execute a select query.
	 * 
	 * @param query
	 *            query to be executed.
	 * @param resquestColumn
	 *            column that required.
	 * @param dbSchema
	 *            schema.
	 * @return Results as list.
	 * @throws BimExchangeException
	 *             custom exception.
	 */
	private List<String> executeSelect(String query, String resquestColumn,
			String dbSchema) throws BimExchangeException {

		ResultSet resultSet = null;
		List<String> response = new ArrayList<>();

		try {
			Connection connection = DbConnectionManager.getInstance(dbSchema)
					.getConnection();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Executing the query \n" + query);
			}
			resultSet = connection.createStatement().executeQuery(query);

			while (resultSet.next()) {
				response.add(resultSet.getString(resquestColumn));
			}

		} catch (BimExchangeException e) {
			LOG.error("Error occurred \n" + e);
			throw e;
		} catch (SQLException e) {
			LOG.error("Error occurred \n" + e);
			throw new BimExchangeException(
					"SQL error occurred in Data Agent. ", e);
		}

		return response;
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
	private boolean executeInsertUpdate(String query)
			throws BimExchangeException {

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

}
