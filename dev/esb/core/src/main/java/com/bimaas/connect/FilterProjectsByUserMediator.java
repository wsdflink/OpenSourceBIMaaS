/**
 * 
 */
package com.bimaas.connect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

/**
 * This class is used to filter projects in out sequence.
 * <p>
 * When access projectS request is made this mediator filters the projects in
 * the RESPONSE of get all projects. This examine the POIDs in project_rights
 * table of the bim_exchange schema and decide which are the projects that the
 * requesting user can access.
 * </p>
 * 
 * @author isuru
 * 
 */
public class FilterProjectsByUserMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(FilterProjectsByUserMediator.class);

	/**
	 * Mediate overridden method.
	 */
	@Override
	public boolean mediate(MessageContext context) {
		try {

			String userAccessToken = (String) context
					.getProperty("AUTHZ_USER_TOKEN");
			String userName = getUserName(userAccessToken);
			List<String> poids = getAccessibleProjectPoids(userName);

			String jsonPayloadToString = JsonUtil
					.jsonPayloadToString(((Axis2MessageContext) context)
							.getAxis2MessageContext());
			JSONObject originalJsonBody = new JSONObject(jsonPayloadToString);

			JSONArray allProjects = new JSONArray(new JSONObject(
					originalJsonBody.getString("response")).getString("result"));

			JSONArray allowedProjects = new JSONArray();

			for (int i = 0; i < allProjects.length(); i++) {
				JSONObject project = (JSONObject) allProjects.get(i);
				if (poids.contains(String.valueOf(project.get("oid")))) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("poid: " + project.get("oid"));
					}
					allowedProjects.put(project);
				}
			}

			JSONObject response = new JSONObject();
			JSONObject result = new JSONObject();

			result.put("result", (Object) allowedProjects);
			response.put("response", result);

			if (LOG.isDebugEnabled()) {
				LOG.debug("User " + userName + " has acceess to "
						+ poids.size() + " projects");
				LOG.debug("Project list: \n" + allowedProjects);
				LOG.debug("Constructed response to be sent: " + response);
			}
			setResponse(context, response.toString());

		} catch (JSONException e) {
			LOG.error("Unexpected response from the backend server \n" + e);
			return false;
		} catch (Exception e) {
			LOG.error("Error occurred in Setting the project rights after creation \n"
					+ e);
			return false;
		}
		return true;
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
			return execute(query, "AUTHZ_USER", BimExConstants.APIM_DB_SCHEMA)
					.get(0);

		} catch (BimExchangeException e) {
			LOG.error("Error occurred in selecting the user name for the user access token: \n"
					+ e);
			throw e;
		}

	}

	private List<String> getAccessibleProjectPoids(String userName)
			throws BimExchangeException {
		String query = "SELECT poid FROM project_rights WHERE authz_user='"
				+ userName + "'";
		return execute(query, "poid", BimExConstants.BIMEX_DB_SCHEMA);
	}

	private List<String> execute(String query, String resquestColumn,
			String dbSchema) throws BimExchangeException {

		ResultSet resultSet = null;
		List<String> response = new ArrayList<>();

		try {
			Connection connection = DbConnectionManager.getInstance(dbSchema)
					.getConnection();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Executing the query to fetch the user name for the access token \n"
						+ query);
			}
			resultSet = connection.createStatement().executeQuery(query);

			while (resultSet.next()) {
				response.add(resultSet.getString(resquestColumn));
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
