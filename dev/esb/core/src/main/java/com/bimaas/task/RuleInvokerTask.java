/**
 * 
 */
package com.bimaas.task;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.task.Task;
import org.apache.synapse.util.PayloadHelper;

/**
 * @author isuru
 * 
 */
public class RuleInvokerTask implements Task, ManagedLifecycle {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(RuleInvokerTask.class);

	/**
	 * Synapse Environment which is passed in init() method.
	 */
	private SynapseEnvironment synapseEnvironment;

	/**
	 * ProxyName to be invoked.
	 */
	private String proxyName;

	/**
	 * Rule id
	 */
	private int ruleId;

	/**
	 * Project Id.
	 */
	private long projectId;

	@Override
	public void init(SynapseEnvironment synapseEnvironment) {
		this.synapseEnvironment = synapseEnvironment;
	}

	@Override
	public void execute() {

		LOG.info("Executing Rule Invoker Task...");

		if (this.synapseEnvironment == null | this.ruleId < 1) {
			String msg = "Synapse Environment or Rule Id not set";
			LOG.error(msg);
			throw new SynapseException(msg);
		}

		// TODO Check the status of the rule from db whether the rule to be
		// invoked or not and take the project Id.
		projectId = 131073;
		// TODO get all the projects where the particular rule is active and do
		// the following in a for loop or delegate the task to threads

		org.apache.axis2.context.MessageContext axis2MsgCtx = new org.apache.axis2.context.MessageContext();

		ConfigurationContext configurationContext = ((Axis2SynapseEnvironment) this.synapseEnvironment)
				.getAxis2ConfigurationContext();

		axis2MsgCtx.setConfigurationContext(configurationContext);
		axis2MsgCtx.setIncomingTransportName("local");
		axis2MsgCtx.setServerSide(true);
		axis2MsgCtx.setSoapAction("urn:mediate");

		try {
			AxisService axisService = configurationContext
					.getAxisConfiguration().getService(this.proxyName);
			if (axisService == null) {
				throw new SynapseException("Proxy Service: " + this.proxyName
						+ " not found");
			}
			axis2MsgCtx.setAxisService(axisService);

		} catch (AxisFault axisFault) {
			throw new SynapseException(
					"Error occurred while attempting to find the Proxy Service");
		}

		SOAPEnvelope envelope = null;
		envelope = OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope();
		invokeProxy(axis2MsgCtx, envelope);

	}

	@Override
	public void destroy() {
	}

	/**
	 * Invoke proxy.
	 * <p>
	 * Invoke the for to execute the rule with the rule id.
	 * </p>
	 * 
	 * @param axis2MsgCtx
	 *            axis2 Message Context.
	 * @param envelope
	 *            Soap Envelope.
	 */
	private void invokeProxy(MessageContext axis2MsgCtx, SOAPEnvelope envelope) {

		LOG.info("Invoking rule for ruleId: " + ruleId);

		OMElement ruleIdElement = generateBody();

		try {
			PayloadHelper.setXMLPayload(envelope,
					ruleIdElement.cloneOMElement());
			axis2MsgCtx.setEnvelope(envelope);

		} catch (AxisFault axisFault) {
			throw new SynapseException(
					"Error in setting the message payload : " + axisFault);
		}

		try {
			if (LOG.isDebugEnabled()) {

				LOG.debug("Injecting message to proxy service: " + proxyName
						+ " for ruleId: " + ruleId
						+ " with the follwoing body content\n"
						+ ruleIdElement.toString());
			}

			LOG.info("Injecting message to proxy service: " + proxyName
					+ " for ruleId: " + ruleId);

			AxisEngine.receive(axis2MsgCtx);

		} catch (AxisFault axisFault) {
			throw new SynapseException(
					"Error occurred while invoking proxy service : "
							+ this.proxyName);
		}
	}

	/**
	 * This method generated the {@link OMElement} which goes under the soapbody
	 * element. The element is as follows,
	 * 
	 * <pre>
	 * &lt;bim:rule xmlns:bim="http://bimaas.com"&gt;
	 * 	  &lt;bim:projectId&gt;999&lt;/bim:projectId&gt;
	 *    &lt;bim:ruleId&gt;777&lt;/bim:ruleId&gt;
	 *    &lt;bim:ruleParams&gt;maxHeight=2.1;minHeight=1.9&lt;/bim:ruleParams&gt;
	 * &lt;/bim:rule&gt;
	 * </pre>
	 * 
	 * @return body.
	 */
	private OMElement generateBody() {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Generating soap body content");
		}

		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace ns = factory.createOMNamespace("http://bimaas.com", "bim");
		OMElement rule = factory.createOMElement("rule", ns);

		// Setting projectId
		OMElement projectIdElement = factory.createOMElement("projectId", ns);
		OMText projectIdText = factory.createOMText(projectIdElement,
				Long.toString(projectId));
		projectIdElement.addChild(projectIdText);

		// Setting ruleId
		OMElement ruleIdElement = factory.createOMElement("ruleId", ns);
		OMText ruleIdText = factory.createOMText(ruleIdElement,
				Integer.toString(ruleId));
		ruleIdElement.addChild(ruleIdText);

		// Setting ruleParams
		OMElement ruleParamsElement = factory.createOMElement("ruleParams", ns);

		// TODO read the parameters from database for particular ruleId and
		// append them in ruleParams tag as key=value;key2=value2;...
		OMText ruleParamsText = factory.createOMText(ruleParamsElement,
				"maxHeight=1.9;minHeight=2.5");

		ruleParamsElement.addChild(ruleParamsText);

		rule.addChild(projectIdElement);
		rule.addChild(ruleIdElement);
		rule.addChild(ruleParamsElement);

		return rule;
	}

	/**
	 * @return the proxyName
	 */
	public final String getProxyName() {
		return proxyName;
	}

	/**
	 * @param proxyName
	 *            the proxyName to set
	 */
	public final void setProxyName(String proxyName) {
		this.proxyName = proxyName;
	}

	/**
	 * @return the ruleId
	 */
	public final int getRuleId() {
		return ruleId;
	}

	/**
	 * @param ruleId
	 *            the ruleId to set
	 */
	public final void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}

}
