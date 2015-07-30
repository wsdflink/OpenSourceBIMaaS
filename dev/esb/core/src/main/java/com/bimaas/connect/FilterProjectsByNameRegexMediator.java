/**
 * 
 */
package com.bimaas.connect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * This class is used to filter projects by the given project name.
 * <p>
 * When access projects by name request is made this mediator filters the
 * projects in the response (get all projects) as treating the given name as the
 * regex for the project name. First this mediator get the all projects from the
 * back end and filter by the user's rights then by the regex.
 * </p>
 * 
 * @author isuru
 * 
 */
public class FilterProjectsByNameRegexMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(FilterProjectsByNameRegexMediator.class);

	/**
	 * Mediate overridden method.
	 */
	@Override
	public boolean mediate(MessageContext context) {
		try {
			String regexFix = ".*";

			String userAccessToken = (String) context
					.getProperty("AUTHZ_USER_TOKEN");
			String projectNameRegex = regexFix
					+ ((String) context.getProperty("PROJECT_PARTIAL_NAME"))
					.toLowerCase() + regexFix;
			if (LOG.isDebugEnabled()) {
				LOG.debug("Searching for the projects which maps the regex: "
						+ projectNameRegex);
			}
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

					Pattern pattern = Pattern.compile(projectNameRegex);
					Matcher matcher = pattern.matcher(String.valueOf(
							project.get("name")).toLowerCase());

					if (matcher.matches()) {
						allowedProjects.put(project);
						if (LOG.isDebugEnabled()) {
							LOG.debug("Matched project found, poid: "
									+ project.get("oid") + ", name: "
									+ project.get("name"));
						}
					}
				}
			}

			JSONObject response = new JSONObject();
			JSONObject result = new JSONObject();

			result.put("result", (Object) allowedProjects);
			response.put("response", result);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Filtered by the given regex: " + projectNameRegex);
				LOG.debug("Constructed response to be sent: " + response);
			}
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
			String error = "Error occurred \n" + e;
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
		return execute(query, "poid", BimExConstants.BIMEX_DB_SCHEMA);
	}

	/**
	 * Select the requested columns.
	 * 
	 * @param query
	 *            query to be executed.
	 * @param resquestColumn
	 *            requested columns.
	 * @param dbSchema
	 *            db.
	 * @return list of
	 * @throws BimExchangeException
	 */
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
