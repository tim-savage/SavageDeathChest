package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.PluginMain;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * SQLite implementation of Datastore
 * for saving death chest block locations
 * @author Tim Savage
 *
 */


final class DataStoreSQLite extends DataStore {

	// reference to main class
	private final PluginMain plugin;

	// database connection object
	private Connection connection;


	/**
	 * Class constructor
	 * @param plugin reference to main class
	 */
	DataStoreSQLite (final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// set datastore type
		this.type = DataStoreType.SQLITE;

		// set filename
		this.filename = "deathchests.db";
	}


	/**
	 * initialize the database connection and
	 * create table if one doesn't already exist
	 */
	@Override
	final void initialize() throws SQLException,ClassNotFoundException {

		// register the driver
		final String jdbcDriverName = "org.sqlite.JDBC";

		Class.forName(jdbcDriverName);

		// create database url
		String deathChestsDb = plugin.getDataFolder() + File.separator + filename;
		String jdbc = "jdbc:sqlite";
		String dbUrl = jdbc + ":" + deathChestsDb;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		final Statement statement = connection.createStatement();

		// execute table creation statement
		statement.executeUpdate(Queries.getQuery("CreateBlockTable"));

		// set initialized true
		setInitialized(true);

		// output log message
		plugin.getLogger().info(this.getName() + " datastore initialized.");

	}


	@Override
	final DeathRecord getRecord(final Location location) {

		// if location is null, return null object
		if (location == null) {
			return null;
		}

		// create new DeathChestBlock object for return
		final DeathRecord deathRecord = new DeathRecord();

		try {

			PreparedStatement preparedStatement = 
					connection.prepareStatement(Queries.getQuery("SelectDeathRecord"));

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
					deathRecord.setOwnerUUID(UUID.fromString(rs.getString("ownerid")));
				}
				catch (Exception e) {
					deathRecord.setOwnerUUID(null);
				}

				// try to convert killer uuid from stored string, or set to null if invalid uuid
				try {
					deathRecord.setKillerUUID(UUID.fromString(rs.getString("killerid")));
				}
				catch (Exception e) {
					deathRecord.setOwnerUUID(null);
				}

				// set other fields in deathChestBlock from database
				deathRecord.setLocation(location);
				deathRecord.setExpiration(rs.getLong("expiration"));
			}

			// return null if no matching location exists in database
			else {
				return null;
			}

		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occured while fetching a death chest block from the SQLite database.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.printStackTrace();
			}
		}

		return deathRecord;
	}


	@Override
	public final Collection<DeathRecord> getAllRecords() {

		final Collection<DeathRecord> results = new ArrayList<>();

		try {

			PreparedStatement preparedStatement = 
					connection.prepareStatement(Queries.getQuery("SelectAllDeathRecords"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				// create empty DeathChestBlock object
				DeathRecord deathRecord = new DeathRecord();

				// try to convert owner uuid from stored string
				try {
					deathRecord.setOwnerUUID(UUID.fromString(rs.getString("ownerid")));
				}
				catch (Exception e) {
					plugin.getLogger().warning("[SQLite getAllRecords] An error occured while trying to set ownerUUID.");
					plugin.getLogger().warning("[SQLite getAllRecords] ownerid string: " + rs.getString("ownerid"));
					plugin.getLogger().warning(e.getLocalizedMessage());
					continue;
				}

				// try to convert killer uuid from stored string, or set to null if invalid uuid
				try {
					deathRecord.setKillerUUID(UUID.fromString(rs.getString("killerid")));
				}
				catch (Exception e) {
					deathRecord.setKillerUUID(null);
				}

				String worldName = rs.getString("worldname");

				// check that world is valid
				if (plugin.getServer().getWorld(worldName) == null) {

					// world does not exist, so output log message and continue to next record
					// plugin.getLogger().warning("Saved deathchest world '" + worldName + "' does not exist.");

					// delete all expired records in database that have this invalid world
					deleteExpiredRecords(worldName);
					continue;
				}

				// create Location object from database fields
				Location location = new Location(plugin.getServer().getWorld(worldName),
						rs.getInt("x"),
						rs.getInt("y"),
						rs.getInt("z"));

				// set other fields in deathChestBlock from database fields
				deathRecord.setLocation(location);
				deathRecord.setExpiration(rs.getLong("expiration"));

				// add DeathChestObject to results ArrayList
				results.add(deathRecord);
			}
		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to fetch all records from the SQLite database.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.printStackTrace();
			}
		}
		if (plugin.debug) {
			plugin.getLogger().info(results.size() + " records fetched from SQLite datastore.");
		}
		return results;
	}

	@Override
	public synchronized final void putRecord(final DeathRecord deathRecord) {

		// if passed deathChestBlock is null, do nothing and return
		if (deathRecord == null) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				
				// catch invalid player uuid exception
				String ownerid;
				try {
					ownerid = deathRecord.getOwnerUUID().toString();
				}
				catch (Exception e) {
					plugin.getLogger().warning("DeathRecord owner UUID is invalid.");
					return;
				}

				// catch invalid killer uuid exception
				String killerid;
				try {
					killerid = deathRecord.getKillerUUID().toString();
				}
				catch (Exception e) {
					killerid = null;
				}

				try {
					// create prepared statement
					PreparedStatement preparedStatement =
							connection.prepareStatement(Queries.getQuery("InsertDeathRecord"));

					preparedStatement.setString(1, ownerid);
					preparedStatement.setString(2, killerid);
					preparedStatement.setString(3, deathRecord.getLocation().getWorld().getName());
					preparedStatement.setInt(4, deathRecord.getLocation().getBlockX());
					preparedStatement.setInt(5, deathRecord.getLocation().getBlockY());
					preparedStatement.setInt(6, deathRecord.getLocation().getBlockZ());
					preparedStatement.setLong(7, deathRecord.getExpiration());

					// execute prepared statement
					int rowsAffected = preparedStatement.executeUpdate();

					// output debugging information
					if (plugin.debug) {
						plugin.getLogger().info(rowsAffected + " rows affected.");
					}
				}
				catch (SQLException e) {

					// output simple error message
					plugin.getLogger().warning("An error occured while inserting a deathchest block into the SQLite database.");
					plugin.getLogger().warning(e.getMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.printStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(plugin);
	}


	//	/**
	//	 * Delete a death chest block record
	//	 */
	//	@Override
	//	public final void deleteRecord(final Location location) {
	//
	//		try {
	//			// create prepared statement
	//			PreparedStatement preparedStatement = 
	//					connection.prepareStatement(Queries.getQuery("DeleteDeathChestBlock"));
	//
	//			preparedStatement.setString(1, location.getWorld().getName());
	//			preparedStatement.setInt(2, location.getBlockX());
	//			preparedStatement.setInt(3, location.getBlockY());
	//			preparedStatement.setInt(4, location.getBlockZ());
	//
	//			// execute prepared statement
	//			int rowsAffected = preparedStatement.executeUpdate();
	//
	//			// output debugging information
	//			if (plugin.debug) {
	//				plugin.getLogger().info(rowsAffected + " rows deleted.");
	//			}
	//		}
	//		catch (SQLException e) {
	//
	//			// output simple error message
	//			plugin.getLogger().warning("An error occurred while attempting to "
	//					+ "delete a record from the " + toString() + " datastore.");
	//			plugin.getLogger().warning(e.getMessage());
	//
	//			// if debugging is enabled, output stack trace
	//			if (plugin.debug) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}


	@Override
	synchronized public final void deleteRecord(final Location location) {

		// if passed location is null, do nothing and return
		if (location == null) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					// create prepared statement
					PreparedStatement preparedStatement =
							connection.prepareStatement(Queries.getQuery("DeleteDeathRecord"));

					preparedStatement.setString(1, location.getWorld().getName());
					preparedStatement.setInt(2, location.getBlockX());
					preparedStatement.setInt(3, location.getBlockY());
					preparedStatement.setInt(4, location.getBlockZ());

					// execute prepared statement
					int rowsAffected = preparedStatement.executeUpdate();

					// output debugging information
					if (plugin.debug) {
						plugin.getLogger().info(rowsAffected + " rows deleted.");
					}
				}
				catch (SQLException e) {

					// output simple error message
					plugin.getLogger().warning("An error occurred while attempting to "
							+ "delete a record from the " + toString() + " datastore.");
					plugin.getLogger().warning(e.getMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.printStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(plugin);
	}


	/**
	 * Delete expired records in world <i>worldName</i>
	 * @param worldName the world name of expired records to delete
	 */
	private void deleteExpiredRecords(final String worldName) {

		// pastDueTime = current time in milliseconds - 30 days
		final long pastDueTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);

		try {
			// create prepared statement
			PreparedStatement preparedStatement = 
					connection.prepareStatement(Queries.getQuery("DeleteExpiredDeathRecord"));

			preparedStatement.setString(1, worldName);
			preparedStatement.setLong(2, pastDueTime);

			// execute prepared statement
			int rowsAffected = preparedStatement.executeUpdate();

			// output debugging information
			if (plugin.debug) {
				plugin.getLogger().info(rowsAffected + " rows deleted.");
			}
		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while attempting to delete expired records from the SQLite database.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Close database connection
	 */
	@Override
	public final void close() {

		if (isInitialized()) {
			try {
				connection.close();
				plugin.getLogger().info(this.getName() + " datastore connection closed.");		
			}
			catch (SQLException e) {

				// output simple error message
				plugin.getLogger().warning("An error occured while closing the SQLite database connection.");
				plugin.getLogger().warning(e.getMessage());

				// if debugging is enabled, output stack trace
				if (plugin.debug) {
					e.printStackTrace();
				}
			}
			setInitialized(false);
		}
	}


	@Override
	final void sync() {
		// no action necessary for this storage type
	}


	@Override
	final boolean delete() {

		boolean result = false;
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		if (dataStoreFile.exists()) {
			result = dataStoreFile.delete();
		}
		return result;
	}


	@Override
	final boolean exists() {
		// get path name to old data store file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		return dataStoreFile.exists();
	}

}
