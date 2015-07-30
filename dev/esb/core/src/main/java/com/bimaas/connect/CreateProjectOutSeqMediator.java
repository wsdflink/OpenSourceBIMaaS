/**
 * 
 */
package com.bimaas.connect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

/**
 * This class is used to add a row to project_rights table with the poid and the
 * admin user name.
 * <p>
 * The data in the project_rights table can be used to manage the rights of the
 * projects by users. By default the project creator gets the access to the
 * project, and he should be able to grant access to others from the bim stats.
 * </p>
 * 
 * @author isuru
 * 
 */
public class CreateProjectOutSeqMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(CreateProjectOutSeqMediator.class);

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

			String poid = new JSONObject(new JSONObject(
					originalJsonBody.getString("response")).getString("result"))
			.getString("oid");

			String userAccessToken = (String) context
					.getProperty("AUTHZ_USER_TOKEN");
			String projectName = (String) context.getProperty("PROJECT_NAME");

			String tempParentId = (String) context.getProperty("PARENT_ID");
			String parentId = tempParentId == null ? "-1" : tempParentId;

			String strLat = (String) context.getProperty("LATITUDE");
			String strLon = (String) context.getProperty("LONGITUDE");

			Double latitude = strLat.isEmpty() ? null : Double.valueOf(strLat);
			Double longitude = strLon.isEmpty() ? null : Double.valueOf(strLon);

			String strGeoFence = (String) context.getProperty("GEO_FENCE");
			String geoFence = (strGeoFence.isEmpty() ? null : strGeoFence);

			String userName = getUserName(userAccessToken);

			insertProject(poid, projectName, parentId, latitude, longitude,
					geoFence, userName);

			insertProjectRigtsRecord(poid, userName);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Successfully set the project rights for the user access token: "
						+ userAccessToken
						+ ", user name: "
						+ userName
						+ ", projectOID: " + poid);
			}

			setResponse(context, jsonPayloadToString);
		} catch (JSONException e) {
			LOG.error("Unexpected response from the backend due to some error \n"
					+ e);
			return true;
		} catch (BimExchangeException e) {
			LOG.error("Error occurred\n" + e);
			return false;

		} catch (Exception e) {
			LOG.error("Error occurred\n" + e);
			return false;
		}
		return true;
	}

	/**
	 * Insert the details to the project table of bim_exchange.
	 * 
	 * @param poid
	 *            project Id.
	 * @param projectName
	 *            name of the project.
	 * @param parentId
	 *            id of the parent if sub project.
	 * @param latitude
	 *            latitude of the project.
	 * @param longitude
	 *            longitude of the project.
	 * @param geoFence
	 *            geo Fence.
	 * @param userName
	 *            name of the user created.
	 * @return true if success.
	 * @throws BimExchangeException
	 *             custom exception.
	 */
	private boolean insertProject(String poid, String projectName,
			String parentId, Double latitude, Double longitude,
			Object geoFence, String userName) throws BimExchangeException {
		String query = "INSERT INTO project (poid, project_name, parent_id, latitude, longitude, geo_fence, created_by) "
				+ "VALUES ('"
				+ poid
				+ "', '"
				+ projectName
				+ "', '"
				+ parentId
				+ "', "
				+ latitude
				+ ","
				+ longitude
				+ ", '"
				+ geoFence
				+ "', '" + userName + "')";
		return executeInsert(BimExConstants.BIMEX_DB_SCHEMA, query);
	}

	/**
	 * Insert the record to project_rights table with user name retrieved from
	 * the user access token.
	 * 
	 * @param userName
	 *            name of the user.
	 * @return true if success.
	 * @throws BimExchangeException
	 *             custom exception.
	 */
	private boolean insertProjectRigtsRecord(String poid, String userName)
			throws BimExchangeException {

		String query = "INSERT INTO project_rights (poid, authz_user) VALUES ('"
				+ poid + "', '" + userName + "')";
		return executeInsert(BimExConstants.BIMEX_DB_SCHEMA, query);
	}

	/**
	 * Execute insert query.
	 * 
	 * @param query
	 *            query to be executed.
	 * @return
	 */
	private boolean executeInsert(String dbSchema, String query)
			throws BimExchangeException {

		Connection connection = null;
		Statement statement = null;

		try {
			connection = DbConnectionManager.getInstance(dbSchema)
					.getConnection();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Executing query...\n" + query);
			}
			statement = connection.createStatement();
			return statement.executeUpdate(query) > 0;

		} catch (BimExchangeException e) {
			LOG.error("Error occurred in executing: " + query + "\n" + e);
			throw e;
		} catch (SQLException e) {
			LOG.error("Error occurred in executing: " + query + "\n" + e);
			throw new BimExchangeException("SQL error occurred", e);

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

		ResultSet resultSet = null;

		String query = "SELECT AUTHZ_USER FROM IDN_OAUTH2_ACCESS_TOKEN WHERE ACCESS_TOKEN='"
				+ userAccessToken.split(" ")[1]
						+ "' AND USER_TYPE = 'APPLICATION_USER' AND TOKEN_STATE='ACTIVE'";
		try {
			Connection connection = DbConnectionManager.getInstance(
					BimExConstants.APIM_DB_SCHEMA).getConnection();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Executing the query to fetch the user name for the access token \n"
						+ query);
			}
			resultSet = connection.createStatement().executeQuery(query);

			// Move to the first result item.
			resultSet.next();
			return resultSet.getString("AUTHZ_USER");

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
