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
@XmlRootElement(name = "rule")
public class Rule {

	/**
	 * Rule id.
	 */
	private long ruleId;

	/**
	 * Project id.
	 */
	private String projectId;

	/**
	 * Rule name.
	 */
	private String ruleName;

	/**
	 * Rule description.
	 */
	private String ruleDescription;

	/**
	 * Last updated timestamp.
	 */
	private Date lastUpdatedTimestamp;

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
	 * @return the projectId
	 */
	public final String getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId
	 *            the projectId to set
	 */
	public final void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * @return the ruleName
	 */
	public final String getRuleName() {
		return ruleName;
	}

	/**
	 * @param ruleName
	 *            the ruleName to set
	 */
	public final void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	/**
	 * @return the ruleDescription
	 */
	public final String getRuleDescription() {
		return ruleDescription;
	}

	/**
	 * @param ruleDescription
	 *            the ruleDescription to set
	 */
	public final void setRuleDescription(String ruleDescription) {
		this.ruleDescription = ruleDescription;
	}

	/**
	 * @return the lastUpdatedTimestamp
	 */
	public final Date getLastUpdatedTimestamp() {
		return lastUpdatedTimestamp;
	}

	/**
	 * @param lastUpdatedTimestamp
	 *            the lastUpdatedTimestamp to set
	 */
	public final void setLastUpdatedTimestamp(Date lastUpdatedTimestamp) {
		this.lastUpdatedTimestamp = lastUpdatedTimestamp;
	}

}
