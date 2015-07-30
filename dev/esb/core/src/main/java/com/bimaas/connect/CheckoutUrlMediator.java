/**
 * 
 */
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
 * This class is used to return the download url.
 * 
 * @author isuru
 * 
 */
public class CheckoutUrlMediator extends AbstractMediator {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(CheckoutUrlMediator.class);

	/**
	 * Ip of the bimserver.
	 */
	private String bimServerIp;

	/**
	 * Port which bimserver can be accessed.
	 */
	private String bimServerPort;

	/**
	 * Bim Server token.
	 */
	private String bimServerToken;

	/**
	 * Mediate overridden method.
	 */
	@Override
	public boolean mediate(MessageContext context) {
		try {

			String topicId = (String) context.getProperty("topicId");
			String serializerOid = (String) context
					.getProperty("serializerOid");

			bimServerToken = ((String) context.getProperty("BIM_SERVER_TOKEN"))
					.trim();

			JSONObject statusObj = buildJson(topicId, serializerOid);

			JSONObject responseBody = new JSONObject();
			responseBody.put("response", statusObj);

			if (LOG.isDebugEnabled()) {
				LOG.debug("topicId: " + topicId);
				LOG.debug("serializerOid: " + serializerOid);
				LOG.debug("Constructed the response body :" + responseBody);
			}

			setResponse(context, responseBody.toString());

		} catch (Exception e) {
			LOG.error("Error occurred in updating status for Checkin Ifc \n"
					+ e);
			return false;
		}
		return true;
	}

	/**
	 * Build the json body for download url.
	 * <p>
	 * http://ip:port/download?token=token&longActionId=6&serializerOid=3080230&
	 * topicId=6
	 * </p>
	 * 
	 * @return json body with download url.
	 */
	private JSONObject buildJson(String topicId, String serializerOid) {
		String url = "http://" + bimServerIp + ":" + bimServerPort
				+ "/bimserver/download?token=" + bimServerToken
				+ "&longActionId=" + topicId + "&serializerOid="
				+ serializerOid + "&topicId=" + topicId;
		JSONObject response = new JSONObject();
		response.put("downloadUrl", url);
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
	 * @return the bimServerIp
	 */
	public final String getBimServerIp() {
		return bimServerIp;
	}

	/**
	 * @param bimServerIp
	 *            the bimServerIp to set
	 */
	public final void setBimServerIp(String bimServerIp) {
		this.bimServerIp = bimServerIp;
	}

	/**
	 * @return the bimServerPort
	 */
	public final String getBimServerPort() {
		return bimServerPort;
	}

	/**
	 * @param bimServerPort
	 *            the bimServerPort to set
	 */
	public final void setBimServerPort(String bimServerPort) {
		this.bimServerPort = bimServerPort;
	}

	/**
	 * @return the bimServerToken
	 */
	public final String getBimServerToken() {
		return bimServerToken;
	}

}
