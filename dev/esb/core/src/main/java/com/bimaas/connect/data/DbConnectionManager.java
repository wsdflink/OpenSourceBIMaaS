/**
 *
 */
package com.bimaas.connect.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bimaas.connect.exception.BimExchangeException;

/**
 * @author Isuru
 * 
 */
public class DbConnectionManager implements IDbConnectionManager {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(DbConnectionManager.class);

	/**
	 * Db Connection Manager
	 */
	// private static volatile DbConnectionManager INSTANCE;

	/**
	 * Sql Connection.
	 */
	private Connection connection;

	/**
	 * Driver name of the jdbc driver.
	 */
	private static String jdbcDriverName;

	/**
	 * Url of the db.
	 */
	private static String dbUrl;

	/**
	 * User name.
	 */
	private static String userName;

	/**
	 * Password.
	 */
	private static String password;

	/**
	 * private constructor
	 * 
	 * @throws BimExchangeException
	 *             Custom Exception.
	 */
	private DbConnectionManager(String schema) throws BimExchangeException {

		try {
			loadDbConfig();
			Class.forName(jdbcDriverName);
			connection = DriverManager.getConnection(dbUrl + schema, userName,
					password);

		} catch (SQLException e) {
			throw new BimExchangeException(
					"Error occurred in connecting to db.\n" + e);
		} catch (ClassNotFoundException e) {
			throw new BimExchangeException(jdbcDriverName
					+ " JDBC Driver not found.\n" + e);
		}
	}

	/**
	 * Singleton implementation
	 * 
	 * @return {@link DbConnectionManager}
	 * @throws BimExchangeException
	 *             Custom Exception.
	 */
	public static DbConnectionManager getInstance(String schema)
			throws BimExchangeException {
		/*
		 * if (INSTANCE == null) { synchronized (DbConnectionManager.class) { if
		 * (INSTANCE == null) INSTANCE = new DbConnectionManager(); } }
		 */

		return new DbConnectionManager(schema);
	}

	/**
	 * Return the sql connection.
	 */
	@Override
	public Connection getConnection() throws BimExchangeException {
		return connection;
	}

	/**
	 * Load the database configurations.
	 */
	private static void loadDbConfig() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Loading database configurations...");
		}
		// TODO should load from a file.
		jdbcDriverName = "com.mysql.jdbc.Driver";
		dbUrl = "jdbc:mysql://127.0.0.1:3306/";
		userName = "root";
		password = "1qaz2wsx@";
	}
}