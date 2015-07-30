/**
 * 
 */
package com.bimaas.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author isuru
 * 
 */
@XmlRootElement(name = "ruleProperty")
public class RuleProperty {

	/**
	 * Rule parameter id.
	 */
	private long rulePropertyId;

	/**
	 * Rule id.
	 */
	private long ruleId;

	/**
	 * Property name.
	 */
	private String propertyName;

	/**
	 * Property value.
	 */
	private String propertyValue;

	/**
	 * Last updated timestamp.
	 */
	private Date propertyUpdatedTimestamp;

	/**
	 * @return the rulePropertyId
	 */
	public final long getRulePropertyId() {
		return rulePropertyId;
	}

	/**
	 * @param rulePropertyId
	 *            the rulePropertyId to set
	 */
	public final void setRulePropertyId(long rulePropertyId) {
		this.rulePropertyId = rulePropertyId;
	}

	/**
	 * @return the ruleId
	 */
	public final long getRuleId() {
		return ruleId;
	}

	/**
	 * @param ruleId
	 *            the ruleId to set
	 */
	public final void setRuleId(long ruleId) {
		this.ruleId = ruleId;
	}

	/**
	 * @return the propertyName
	 */
	public final String getPropertyName() {
		return propertyName;
	}

	/**
	 * @param propertyName
	 *            the propertyName to set
	 */
	public final void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * @return the propertyValue
	 */
	public final String getPropertyValue() {
		return propertyValue;
	}

	/**
	 * @param propertyValue
	 *            the propertyValue to set
	 */
	public final void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	/**
	 * @return the propertyUpdatedTimestamp
	 */
	public final Date getPropertyUpdatedTimestamp() {
		return propertyUpdatedTimestamp;
	}

	/**
	 * @param propertyUpdatedTimestamp
	 *            the propertyUpdatedTimestamp to set
	 */
	public final void setPropertyUpdatedTimestamp(Date propertyUpdatedTimestamp) {
		this.propertyUpdatedTimestamp = propertyUpdatedTimestamp;
	}

}
