/**
 * 
 */
package com.bimaas.connect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bimaas.connect.constants.BimExConstants;
import com.bimaas.connect.data.DbConnectionManager;
import com.bimaas.connect.exception.BimExchangeException;
import com.bimaas.connect.exception.ErrorBuilder;

/**
 * This class is used to filter projects by given latitude and longitude.
 * <p>
 * This mediator filter the projects of get all projects request, by latitude
 * and longitude given and it the projects are also filtered considering the
 * accuracy points.
 * 
 * AUTHZ_USER_TOKEN property in synapse scope, LAT_LONG_ACCURACY property in
 * synapse scope, should be set before this mediator.
 * </p>
 * 
 * @author isuru
 * 
 */
public class FilterProjectsByLatLongMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(FilterProjectsByLatLongMediator.class);

	/**
	 * Mediate overridden method.
	 */
	@Override
	public boolean mediate(MessageContext context) {

		try {
			String userAccessToken = (String) context
					.getProperty("AUTHZ_USER_TOKEN");
			LOG.info("AUTHZ_USER_TOKEN: " + userAccessToken);
			double accuracy = Double.valueOf((String) context
					.getProperty("ACCURACY"));
			LOG.info("ACCURACY: " + accuracy);
			double latitude = Double.valueOf((String) context
					.getProperty("LATITUDE"));
			LOG.info("LATITUDE: " + latitude);
			double longitude = Double.valueOf((String) context
					.getProperty("LONGITUDE"));
			LOG.info("LONGITUDE: " + longitude);

			String userName = getUserName(userAccessToken);
			List<String> poids = getAccessibleProjectPoids(userName);

			String jsonPayloadToString = JsonUtil
					.jsonPayloadToString(((Axis2MessageContext) context)
							.getAxis2MessageContext());
			JSONObject originalJsonBody = new JSONObject(jsonPayloadToString);

			JSONArray allProjects = new JSONArray(new JSONObject(
					originalJsonBody.getString("response")).getString("result"));

			JSONArray matchingProjects = new JSONArray();

			for (int i = 0; i < allProjects.length(); i++) {
				JSONObject project = (JSONObject) allProjects.get(i);

				if (poids.contains(String.valueOf(project.get("oid")))) {
					String oid = String.valueOf(project.get("oid"));
					if (doesLatLongMatch(oid, latitude, longitude, accuracy)) {
						LOG.info("Found a matching project for the given location, poid: "
								+ oid);
						matchingProjects.put(project);
					}
				}
			}

			JSONObject response = new JSONObject();
			JSONObject result = new JSONObject();

			result.put("result", (Object) matchingProjects);
			response.put("response", result);

			setResponse(context, response.toString());

		} catch (PatternSyntaxException e) {
			String error = "Error in the given regex for the project name \n"
					+ e;
			LOG.error(e);
			try {
				setResponse(context, ErrorBuilder.getErrorResponse(error)
						.toString());
			} catch (AxisFault e1) {
				e1.printStackTrace();
				return false;
			}
			return true;
		} catch (JSONException e) {
			String error = "Unexpected response from the backend server \n" + e;
			LOG.error(e);
			try {
				setResponse(context, ErrorBuilder.getErrorResponse(error)
						.toString());
			} catch (AxisFault e1) {
				e1.printStackTrace();
				return false;
			}
			return true;
		} catch (Exception e) {
			String error = "Error occurred\n" + e;
			LOG.error(e);
			try {
				setResponse(context, ErrorBuilder.getErrorResponse(error)
						.toString());
			} catch (AxisFault e1) {
				e1.printStackTrace();
				return false;
			}
			return true;
		}
		return true;
	}

	/**
	 * Checking whether the project is a matching with the latitude, longitude
	 * and accuracy parameters.
	 * 
	 * @param oid
	 *            id of the project.
	 * @param accuracy
	 *            accuracy.
	 * @param accuracy2
	 * @param longitude
	 * @return true if matching.
	 * @throws BimExchangeException
	 */
	private boolean doesLatLongMatch(String oid, double latitude,
			double longitude, double accuracy) throws BimExchangeException {
		String query = "SELECT latitude, longitude FROM project WHERE poid='"
				+ oid + "'";
		List<String> result = executeSelect(query,
				BimExConstants.BIMEX_DB_SCHEMA, "latitude", "longitude");
		if (LOG.isDebugEnabled()) {
			LOG.debug("Searching for matching project with the given longitude and latitude...");
		}
		try {
			String val_1 = result.get(0).split(",")[0];
			String val_2 = result.get(0).split(",")[1];

			if ((val_1.equals("null")) || (val_2.equals("null"))
					|| (val_1.isEmpty()) || (val_2.isEmpty())) {
				return false;
			}
			double lat = Double.valueOf(val_1);
			double longt = Double.valueOf(val_2);

			if ((Math.abs((latitude - lat)) <= accuracy)
					&& (Math.abs((longitude - longt)) <= accuracy)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found a matching project for the given location, poid: "
							+ oid);
				}
				return true;
			}
		} catch (NumberFormatException e) {
			LOG.error("Error in latitude or longitude found");
			throw new BimExchangeException(
					"Error found in comparing geo locations\n" + e);
		}
		return false;
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
			return executeSelect(query, BimExConstants.APIM_DB_SCHEMA,
					"AUTHZ_USER").get(0);

		} catch (BimExchangeException e) {
			LOG.error("Error occurred in selecting the user name for the user access token: \n"
					+ e);
			throw e;
		}

	}

	/**
	 * Get accessible projects by the user.
	 * 
	 * @param userName
	 *            user name.
	 * @return list of accessible projects.
	 * @throws BimExchangeException
	 *             custom exception.
	 */
	private List<String> getAccessibleProjectPoids(String userName)
			throws BimExchangeException {
		String query = "SELECT poid FROM project_rights WHERE authz_user='"
				+ userName + "'";
		return executeSelect(query, BimExConstants.BIMEX_DB_SCHEMA, "poid");
	}

	/**
	 * Select the requested columns and returned as a list, and each entry has
	 * column values separated by a comma.
	 * 
	 * @param query
	 *            query to be executed.
	 * @param requestColumns
	 *            requested columns.
	 * @param dbSchema
	 *            db.
	 * @return list of
	 * @throws BimExchangeException
	 */
	private List<String> executeSelect(String query, String dbSchema,
			String... requestColumns) throws BimExchangeException {

		ResultSet resultSet = null;
		List<String> response = new ArrayList<>();
		Connection connection = null;

		try {
			connection = DbConnectionManager.getInstance(dbSchema)
					.getConnection();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Executing the query to fetch the user name for the access token \n"
						+ query);
			}
			resultSet = connection.createStatement().executeQuery(query);

			while (resultSet.next()) {

				StringBuilder valueLine = new StringBuilder();
				String column = null;
				int length = requestColumns.length;
				for (int i = 0; i < length; i++) {
					column = requestColumns[i];
					valueLine
					.append(String.valueOf(resultSet.getObject(column)));
					if ((i + 1) < length) {
						valueLine.append(",");
					}
				}
				response.add(valueLine.toString());
			}

		} catch (BimExchangeException e) {
			LOG.error("Error occurred in selecting the user name for the user access token: \n"
					+ e);
			throw e;
		} catch (SQLException e) {
			LOG.error("Error occurred in selecting the user name for the user access token: \n"
					+ e);
			throw new BimExchangeException(
					"SQL error occurred in Data Agent. ", e);
		} finally {

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
