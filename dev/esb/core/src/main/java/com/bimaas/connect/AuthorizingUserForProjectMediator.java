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
import org.json.JSONException;
import org.json.JSONObject;

import com.bimaas.connect.constants.BimExConstants;
import com.bimaas.connect.data.DbConnectionManager;
import com.bimaas.connect.exception.BimExchangeException;
import com.bimaas.connect.exception.ErrorBuilder;

/**
 * This class is used to check whether the user has access to the given project.
 * <p>
 * This mediator checks the bim_exchange.project_rights table and decide whether
 * the user has rights to access the given project.
 * </p>
 * 
 * @author isuru
 * 
 */
public class AuthorizingUserForProjectMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(AuthorizingUserForProjectMediator.class);

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

			String poid = new JSONObject(originalJsonBody.getString("request"))
			.getString("projectId");

			if (poids.contains(poid)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("poid: " + poid + " is an accessible for user: "
							+ userName);

				}
				return true;
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("User " + userName
							+ " has no rights to access project: " + poid);
				}
				JSONObject response = new JSONObject();
				JSONObject result = new JSONObject();

				// Setting an exception.
				result.put("exception",
						"You are not authorized to access the given project: "
								+ poid);
				response.put("response", result);
				// STOP THE FLOW.
				context.setProperty(BimExConstants.STOP_FLOW, "1");

				setResponse(context, response.toString());

				return true;
			}

		} catch (JSONException | BimExchangeException e) {

			try {
				LOG.error("Error occurred \n" + e);
				// STOP THE FLOW.
				context.setProperty(BimExConstants.STOP_FLOW, "1");
				setResponse(context,
						ErrorBuilder.getErrorResponse("Error occurred \n" + e)
						.toString());
				return true;
			} catch (AxisFault e1) {
				LOG.error("Error occurred in Setting the project rights after creation \n"
						+ e);
				return false;
			}

		} catch (Exception e) {
			LOG.error("Error occurred in Setting the project rights after creation \n"
					+ e);
			return false;
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
