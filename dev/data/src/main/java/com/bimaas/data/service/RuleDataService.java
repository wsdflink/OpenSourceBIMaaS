/**
 *
 */
package com.bimaas.data.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

import com.bimaas.data.DBSessionManager;
import com.bimaas.data.mappers.RuleMapper;
import com.bimaas.dto.RuleDTO;
import com.bimaas.exception.BimaasException;
import com.bimaas.model.RuleProperty;

/**
 * All data operations for Rule.
 * 
 * @author isuru
 * 
 */
public class RuleDataService {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(RuleDataService.class);

	/**
	 * Create a rule.
	 * 
	 * @param ruleProperty
	 *            {@link RuleProperty} object.
	 * @return true if success.
	 * @throws BimaasException
	 *             custom exception.
	 */
	public boolean updateRuleValues(RuleProperty ruleProperty)
			throws BimaasException {
		SqlSession sqlSession = null;
		boolean response = false;

		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Updating rule...");
			}
			// Session with auto commit false;
			sqlSession = DBSessionManager.getInstance().getSessionFactory()
					.openSession(false);
			if (LOG.isDebugEnabled()) {
				LOG.debug("SqlSession opened for rule mapper");
			}
			RuleMapper ruleMapper = sqlSession.getMapper(RuleMapper.class);

			response = ruleMapper.updateRuleValues(ruleProperty) > 0;

			sqlSession.commit();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Rule update completed: " + response);
			}
		} catch (IOException e) {
			LOG.error("Error occurred in creating rule" + e);
			throw new BimaasException("Error occurred: " + e);

		} finally {
			if (sqlSession != null) {
				sqlSession.close();
				if (LOG.isDebugEnabled()) {
					LOG.debug("SqlSession closed");
				}
			} else {
				LOG.warn("SqlSession is null");
			}
		}
		return response;
	}

	/**
	 * Return the Rule properties for the given ruleId.
	 * 
	 * @param projectId
	 *            id of the project.
	 * @param ruleId
	 *            id of the rule.
	 * @return Rule Param object.
	 * @throws BimaasException
	 *             custom exception.
	 */
	public RuleDTO getRuleProperties(long projectId, long ruleId)
			throws BimaasException {
		SqlSession sqlSession = null;
		RuleDTO response = null;

		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Quering for rule params...");
			}

			// Session with auto commit false;
			sqlSession = DBSessionManager.getInstance().getSessionFactory()
					.openSession(false);
			if (LOG.isDebugEnabled()) {
				LOG.debug("SqlSession opened for rule mapper");
			}

			RuleMapper ruleMapper = sqlSession.getMapper(RuleMapper.class);

			Map<String, Long> params = new HashMap<String, Long>();
			params.put("projectId", projectId);
			params.put("ruleId", ruleId);
			response = ruleMapper.getRuleProperties(params);

			sqlSession.commit();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Requested ruleparam: " + response);
			}

		} catch (IOException e) {
			LOG.error("Error occurred in creating rule" + e);
			throw new BimaasException("Error occurred: " + e);

		} finally {
			if (sqlSession != null) {
				sqlSession.close();
				if (LOG.isDebugEnabled()) {
					LOG.debug("SqlSession closed");
				}
			} else {
				LOG.warn("SqlSession is null");
			}
		}
		return response;
	}
}
