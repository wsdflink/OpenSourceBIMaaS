/**
 * 
 */
package com.bimaas.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.bimaas.model.RuleProperty;

/**
 * @author isuru
 * 
 */
@XmlRootElement(name = "ruleDTO")
public class RuleDTO {

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
	 * State
	 */
	private boolean isActive;

	/**
	 * List of rule properties.
	 */
	private List<RuleProperty> listOfRuleProperties;

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
	 * @return the isActive
	 */
	public final boolean isActive() {
		return isActive;
	}

	/**
	 * @param isActive
	 *            the isActive to set
	 */
	public final void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * @return the listOfRuleProperties
	 */
	@XmlElementWrapper
	@XmlElement(name = "property")
	public final List<RuleProperty> getListOfRuleProperties() {
		return listOfRuleProperties;
	}

	/**
	 * @param listOfRuleProperties
	 *            the listOfRuleProperties to set
	 */
	public final void setListOfRuleProperties(
			List<RuleProperty> listOfRuleProperties) {
		this.listOfRuleProperties = listOfRuleProperties;
	}

}
