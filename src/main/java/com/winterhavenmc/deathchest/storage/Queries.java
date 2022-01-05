package com.winterhavenmc.deathchest.storage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;


/**
 * A utility class that contains static methods for retrieving database queries from the queries properties file.
 */
final class Queries {

	private static final String propFileName = "queries.properties";
	private static Properties properties;

	/**
	 * Private constructor to prevent instantiation of class
	 */
	private Queries() {
		throw new AssertionError();
	}


	private static Properties getQueries() throws SQLException {

		// singleton
		if (properties == null) {
			properties = new Properties();
			try {

				InputStream inputStream = Queries.class.getResourceAsStream("/" + propFileName);

				if (inputStream == null) {
					throw new SQLException("Unable to load property file: " + propFileName);
				}
				properties.load(inputStream);
			}
			catch (IOException e) {
				throw new SQLException("Unable to load property file: " + propFileName);
			}
		}

		return properties;
	}


	static String getQuery(final String query) throws SQLException {
		return getQueries().getProperty(query);
	}

}
