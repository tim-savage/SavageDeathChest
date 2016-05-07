package com.winterhaven_mc.deathchest;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

public class Queries {

	private static final String propFileName = "queries.properties";
	private static Properties properties;
	
	public static Properties getQueries() throws SQLException {
		
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

	public static String getQuery(String query) throws SQLException {
		return getQueries().getProperty(query);
	}
	
}
