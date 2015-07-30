package com.bimaas.connect;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONObject;

/**
 * This class adds the authorization header to the request which is to be sent
 * to the bim server.
 * 
 * @author isuru
 * 
 */
public class BimServerAuthorizationMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory
			.getLog(BimServerAuthorizationMediator.class);

	/**
	 * Holds the bim Server token.
	 */
	private String bimServerToken;

	/**
	 * Token tag.
	 */
	private final String TOKEN = "token";

	/**
	 * Mediate overridden method to set the token property.
	 */
	@Override
	public boolean mediate(MessageContext context) {

		String jsonPayloadToString = JsonUtil
				.jsonPayloadToString(((Axis2MessageContext) context)
						.getAxis2MessageContext());
		JSONObject jsonBody = new JSONObject(jsonPayloadToString);

		bimServerToken = ((String) context.getProperty("BIM_SERVER_TOKEN"))
				.trim();

		if (LOG.isDebugEnabled()) {
			LOG.debug("BIM SERVER TOKEN FOUND: " + bimServerToken);
		}

		// Adding the token:value tag.
		jsonBody.put(TOKEN, getBimServerToken());

		try {

			String transformedJson = jsonBody.toString();
			setResponse(context, transformedJson);
			String logString = "token is set to json body in Class Mediator";
			LOG.info(logString);
			System.out.println(logString);

		} catch (AxisFault e) {
			LOG.error("Error occurred in DataHandler custom class meditor when setting bim server token");
			return false;
		}

		return true;
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
	 * @return the bimServerToken
	 */
	public final String getBimServerToken() {
		return bimServerToken;
	}

}
