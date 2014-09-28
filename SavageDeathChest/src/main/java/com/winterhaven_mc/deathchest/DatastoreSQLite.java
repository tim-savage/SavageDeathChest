package com.winterhaven_mc.deathchest;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.Location;

/**
 * SQLite implementation of Datastore
 * for saving death chest item locations
 * @author Tim Savage
 *
 */

public class DatastoreSQLite extends Datastore {

	// static reference to main class
	private DeathChestMain plugin = DeathChestMain.plugin;

	// database connection object
	private Connection connection;


	/**
	 * initialize the database connection and
	 * create table if one doesn't already exist
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public void initializeDatastore() throws SQLException, ClassNotFoundException {

		// sql statement to create table if it doesn't already exist
		final String makeItemTable = "CREATE TABLE IF NOT EXISTS blocks (" +
				"blockid INTEGER PRIMARY KEY" +
				"ownerid VARCHAR(36) NOT NULL," +
				"killerid VARCHAR(36) NOT NULL," +
				"worldname VARCHAR(255) NOT NULL," +
				"x INTEGER, " +
				"y INTEGER, " +
				"z INTEGER, " +
				"expiration INTEGER, " +
				"UNIQUE (worldname,x,y,z) )";

		// register the driver 
		final String jdbcDriverName = "org.sqlite.JDBC";
		
		Class.forName(jdbcDriverName);

		// create database url
		String deathChestsDb = plugin.getDataFolder() + File.separator + "deathchests.db";
		String jdbc = "jdbc:sqlite";
		String dbUrl = jdbc + ":" + deathChestsDb;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);
		Statement statement = connection.createStatement();

		// execute table creation statement
		statement.executeUpdate(makeItemTable);

		// output status to log
		plugin.getLogger().info("SQLite database intialized.");
		
		// convert records from flat file if necessary
		convertFromFile("deathchests.yml");
	}

	
	/**
	 * Close database connection
	 */
	void closeDatastore() {
		
		try {
			connection.close();
			plugin.getLogger().info("SQLite database connection closed.");		
		}
		catch (SQLException e) {
			plugin.getLogger().warning("An error occured while closing the SQLite database connection.");
			plugin.getLogger().warning(e.getMessage());
		}

	}

	DeathChestBlock getRecord(Location location) {
		
		final String sqlGetDeathChestBlock = "SELECT * FROM blocks "
				+ "WHERE worldname = ? "
				+ "AND x = ? "
				+ "AND y = ? "
				+ "AND z = ?";
		
		DeathChestBlock deathChestBlock = new DeathChestBlock();
		
		try {
			
			PreparedStatement preparedStatement = connection.prepareStatement(sqlGetDeathChestBlock);
			
			preparedStatement.setString(1, location.getWorld().getName());
			preparedStatement.setInt(2, location.getBlockX());
			preparedStatement.setInt(3, location.getBlockY());
			preparedStatement.setInt(4, location.getBlockZ());

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();
			
			// only zero or one record can match the unique location
			if (rs.next()) {
				
				// try to convert owner uuid from stored string, or set to null if invalid uuid
				try {
					deathChestBlock.setOwnerUUID(UUID.fromString(rs.getString("ownerid")));
				}
				catch (Exception e) {
					deathChestBlock.setOwnerUUID(null);
				}
				
				// try to convert killer uuid from stored string, or set to null if invalid uuid
				try {
					deathChestBlock.setKillerUUID(UUID.fromString(rs.getString("killerid")));
				}
				catch (Exception e) {
					deathChestBlock.setOwnerUUID(null);
				}
				
				// set other fields in deathChestBlock from database
				deathChestBlock.setBlockId(rs.getInt("blockid"));
				deathChestBlock.setLocation(location);
				deathChestBlock.setExpiration(rs.getLong("expiration"));
			}
			
			// return null DeathChestBlock object if no matching location exists in database
			else {
				deathChestBlock = null;
			}
			
		}
		catch (SQLException e) {
			plugin.getLogger().warning("An error occured while fetching a death chest block from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}
		
		return deathChestBlock;
	}

	@Override
	void putRecord(DeathChestBlock deathChestBlock) {
		// TODO Auto-generated method stub

	}

	
	private void convertFromFile(String string) {
		// TODO Auto-generated method stub
		
	}


}
