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
 * This class is used to keep the status of checkout in bim_exchange.checkout
 * table.
 * <p>
 * This is used in checkin flow to lock the checkin again with out checking out
 * and to lock checkin if some other has checked out and lock the project.
 * </p>
 * 
 * @author isuru
 * 
 */
public class CheckoutStatusMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(CheckoutStatusMediator.class);

	/**
	 * Status of the file checkin.
	 */
	private String isLocked;

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

			String poid = new JSONObject(
					new JSONObject(originalJsonBody.getString("request"))
					.getString("parameters")).getString("poid");
			String userName = getUserName((String) context
					.getProperty("AUTHZ_USER_TOKEN"));

			if ("INSERT".equalsIgnoreCase(operation)) {
				// This is for checkout

				String revisionId = new JSONObject(
						new JSONObject(originalJsonBody.getString("request"))
						.getString("parameters")).getString("roid");

				if (isCheckoutAllowed(poid, revisionId, userName)) {
					LOG.info("Revision requested is allowed to checkout");

					if (doesAnyRecordExist(poid, revisionId, userName)) {
						LOG.info("Updating the checkout details...");
						query = "UPDATE checkout SET is_locked='1' WHERE poid='"
								+ poid
								+ "' AND revision_id='"
								+ revisionId
								+ "' AND checked_out_by='" + userName + "'";
						if (executeInsertOrUpdate(query)) {
							return true;
						} else {
							try {
								// Setting the stop flow flag to 1.
								context.setProperty(BimExConstants.STOP_FLOW,
										"1");
								setResponse(
										context,
										ErrorBuilder
										.getErrorResponse(
												"Managing checkout failed, internal error")
												.toString());
								LOG.error("Managing checkout failed, internal error");
								return true;
							} catch (AxisFault e) {
								LOG.error(e);
								return false;
							}
						}
					} else {
						LOG.info("Inserting the checkout details...");
						query = "INSERT INTO checkout (poid, revision_id, checked_out_by, is_locked) VALUES ('"
								+ poid
								+ "', '"
								+ revisionId
								+ "', '"
								+ userName + "', '" + isLocked + "')";
						if (executeInsertOrUpdate(query)) {
							return true;

						} else {
							try {
								// Setting the stop flow flag to 1.
								context.setProperty(BimExConstants.STOP_FLOW,
										"1");
								setResponse(
										context,
										ErrorBuilder
										.getErrorResponse(
												"Managing checkout failed, internal error")
												.toString());
								LOG.error("Managing checkout failed, internal error");
								return true;
							} catch (AxisFault e) {
								LOG.error(e);
								return false;
							}
						}
					}
				} else {
					// Get the check out use name
					String getCheckoutUser = "SELECT checked_out_by FROM checkout WHERE poid='"
							+ poid + "' AND revision_id='" + revisionId + "'";
					List<String> checkedOutUser = executeSelect(
							getCheckoutUser, BimExConstants.BIMEX_DB_SCHEMA,
							"checked_out_by");

					String message = "";
					if (checkedOutUser.size() > 0) {
						message = "Sorry revision is not allowed to checkout, "
								+ checkedOutUser
								+ " is currently working on this revision.";
					} else {
						message = "Revision request is not allowed to checkout";
					}

					try {
						// Setting the stop flow flag to 1.
						context.setProperty(BimExConstants.STOP_FLOW, "1");
						setResponse(context,
								ErrorBuilder.getWarningResponse(message)
								.toString());
						LOG.info("Revision requested is NOT allowed to checkout");
						return true;
					} catch (AxisFault e) {
						LOG.error(e);
						return false;
					}
				}
			}

			if ("UPDATE".equalsIgnoreCase(operation)) {
				// This is for checkin
				// It updates if the same user has checked this out and make
				// lock free.

				query = "UPDATE checkout SET is_locked='" + isLocked
						+ "' WHERE poid='" + poid + "' AND checked_out_by='"
						+ userName + "'";
				if (executeInsertOrUpdate(query)) {
					if (LOG.isDebugEnabled())
						LOG.debug("Successfylly updated checkout status");
					return true;
				} else {
					if (LOG.isDebugEnabled())
						LOG.debug("First time checkin");
					return true;
				}
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Creating or updating checkin status related to referenceId: "
						+ userName);
			}

		} catch (BimExchangeException e) {
			LOG.error("Error occurred\n" + e);
			try {
				// Setting the stop flow flag to 1.
				context.setProperty(BimExConstants.STOP_FLOW, "1");
				setResponse(context,
						ErrorBuilder.getErrorResponse("Error occurred: " + e)
						.toString());
			} catch (AxisFault e1) {
				LOG.error(e1);
			}
			return false;
		}
		return true;
	}

	/**
	 * Check is there already a record for this user this revision, if so it
	 * just update the record instead of inserting.
	 * 
	 * @param poid
	 *            project id.
	 * @param revisionId
	 *            revision id.
	 * @param userName
	 *            name of the user.
	 * @return true if any record exists.
	 * @throws BimExchangeException
	 */
	private boolean doesAnyRecordExist(String poid, String revisionId,
			String userName) throws BimExchangeException {
		String query = "SELECT poid FROM checkout WHERE poid='" + poid
				+ "' AND revision_id='" + revisionId + "' AND checked_out_by='"
				+ userName + "'";
		try {
			List<String> poids = executeSelect(query,
					BimExConstants.BIMEX_DB_SCHEMA, "poid");
			return (poids.size() > 0);
		} catch (BimExchangeException e) {
			LOG.error("Error occurred: " + e);
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
			return executeSelect(query, BimExConstants.APIM_DB_SCHEMA,
					"AUTHZ_USER").get(0);

		} catch (BimExchangeException e) {
			LOG.error("Error occurred in selecting the user name for the user access token: \n"
					+ e);
			throw e;
		}

	}

	/**
	 * This check whether the file is available for checkout. Any other user
	 * should not be keeping the lock.
	 * 
	 * @param poid
	 *            id of the project.
	 * @param revision_id
	 *            id of the revision.
	 * @param userName
	 *            name of the user.
	 * @return true if allowed.
	 * @throws BimExchangeException
	 */
	private boolean isCheckoutAllowed(String poid, String revisionId,
			String userName) throws BimExchangeException {
		String query = "SELECT poid FROM checkout WHERE poid='" + poid
				+ "' AND revision_id='" + revisionId
				+ "' AND is_locked='1' AND checked_out_by <> '" + userName
				+ "'";
		try {
			List<String> poids = executeSelect(query,
					BimExConstants.BIMEX_DB_SCHEMA, "poid");
			return (poids.size() == 0);
		} catch (BimExchangeException e) {
			LOG.error("Error occurred: " + e);
			throw e;
		}
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
	private boolean executeInsertOrUpdate(String query)
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

	/**
	 * @return the isLocked
	 */
	public final String getIsLocked() {
		return isLocked;
	}

	/**
	 * @param isLocked
	 *            the isLocked to set
	 */
	public final void setIsLocked(String isLocked) {
		this.isLocked = isLocked;
	}

	/**
	 * @return the operation
	 */
	public final String getOperation() {
		return operation;
	}

	/**
	 * @param operation
	 *            the operation to set
	 */
	public final void setOperation(String operation) {
		this.operation = operation;
	}

}
