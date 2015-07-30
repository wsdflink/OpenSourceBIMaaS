/**
 *
 */
package com.bimaas.connect.data;

import java.sql.Connection;

import com.bimaas.connect.exception.BimExchangeException;

/**
 * @author Isuru
 * 
 */
public interface IDbConnectionManager {

	/**
	 * Returns a session factory.
	 * 
	 * @return {@link Connection}
	 * @throws BimExchangeException
	 *             custom exception.
	 */
	Connection getConnection() throws BimExchangeException;

}
