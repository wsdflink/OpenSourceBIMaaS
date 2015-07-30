/**
 *
 */
package com.bimaas.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * @author Isuru
 * 
 */
public class DBSessionManager implements IDBSessionManager {

	private static volatile DBSessionManager INSTANCE;
	private SqlSessionFactory sqlSessionFactory;

	/**
	 * private constructor
	 */
	private DBSessionManager() {
		String resource = "db/mybatis_configuration.xml";

		try {
			InputStream inputStream = Resources.getResourceAsStream(resource);
			Reader configReader = new InputStreamReader(inputStream);
			sqlSessionFactory = new SqlSessionFactoryBuilder()
			.build(configReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Singleton implementation
	 * 
	 * @return {@link DBSessionManager}
	 */
	public static DBSessionManager getInstance() {
		if (INSTANCE == null) {
			synchronized (DBSessionManager.class) {
				if (INSTANCE == null)
					INSTANCE = new DBSessionManager();
			}
		}
		return INSTANCE;
	}

	@Override
	public SqlSessionFactory getSessionFactory() throws IOException {
		return sqlSessionFactory;
	}
}