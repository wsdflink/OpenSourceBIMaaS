/**
 *
 */
package com.bimaas.data.mappers;

import java.util.Map;

import com.bimaas.dto.RuleDTO;
import com.bimaas.model.RuleProperty;

/**
 * Mapper class for the operations defined in the corresponding mapper xml.
 * 
 * @author isuru
 * 
 */
public interface RuleMapper {

	/**
	 * Update rule data values.
	 * 
	 * @param ruleProperty
	 *            rule to be persisted.
	 * @return positive if success.
	 */
	public int updateRuleValues(RuleProperty ruleProperty);

	/**
	 * Return {@link RuleProperty} for the given ruleId.
	 * 
	 * @param parameters
	 *            map.
	 * @return RuleParam objects.
	 */
	public RuleDTO getRuleProperties(Map<String, Long> parameters);

	/**
	 * Get rules by rule id.
	 * 
	 * @param ruleId
	 *            id of the rule
	 * @return rule instance.
	 */
	// public Rule getRule(long ruleId);

	/**
	 * Returns the rule for the given project id.
	 * 
	 * @param projectId
	 *            project id.
	 * @return list of rules.
	 */
	// public List<Rule> getrulesByProjectId(@Param("projectId") String
	// projectId);

	/**
	 * Update the rule.
	 * 
	 * @param ruleParam
	 *            updated instance.
	 * @return true if success.
	 */
	// public boolean updaterule(Rule ruleParam);

}
