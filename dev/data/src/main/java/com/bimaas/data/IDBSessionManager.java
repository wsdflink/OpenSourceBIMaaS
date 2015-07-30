/**
 *
 */
package com.bimaas.data;

import java.io.IOException;

import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @author Isuru
 * 
 */
public interface IDBSessionManager {

	/**
	 * Returns a session factory.
	 * 
	 * @return {@link SqlSessionFactory}
	 * @throws IOException
	 *             io exception
	 */
	SqlSessionFactory getSessionFactory() throws IOException;

}
