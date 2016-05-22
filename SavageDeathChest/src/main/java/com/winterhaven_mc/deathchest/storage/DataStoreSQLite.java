package com.winterhaven_mc.deathchest.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.winterhaven_mc.deathchest.DeathChestBlock;
import com.winterhaven_mc.deathchest.PluginMain;


/**
 * SQLite implementation of Datastore
 * for saving death chest block locations
 * @author Tim Savage
 *
 */

public final class DataStoreSQLite extends DataStore {

	// reference to main class
	private final PluginMain plugin;

	// database connection object
	private Connection connection;


	/**
	 * Class constructor
	 * @param plugin
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
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	@Override
	final void initialize() throws SQLException, ClassNotFoundException {

		// if data store is already initialized, do nothing and return
		if (this.isInitialized()) {
			plugin.getLogger().info(this.getName() + " datastore already initialized.");
			return;
		}

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
	final DeathChestBlock getRecord(final Location location) {

		// if location is null, return null object
		if (location == null) {
			return null;
		}

		// create new DeathChestBlock object for return
		final DeathChestBlock deathChestBlock = new DeathChestBlock();

		try {

			PreparedStatement preparedStatement = 
					connection.prepareStatement(Queries.getQuery("SelectDeathChestBlock"));

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
					deathChestBlock.setOwnerUUID((UUID)null);
				}

				// try to convert killer uuid from stored string, or set to null if invalid uuid
				try {
					deathChestBlock.setKillerUUID(UUID.fromString(rs.getString("killerid")));
				}
				catch (Exception e) {
					deathChestBlock.setOwnerUUID((UUID)null);
				}

				// set other fields in deathChestBlock from database
				deathChestBlock.setLocation(location);
				deathChestBlock.setExpiration(rs.getLong("expiration"));
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

		return deathChestBlock;
	}

	@Override
	final ArrayList<DeathChestBlock> getAllRecords() {

		final ArrayList<DeathChestBlock> results = new ArrayList<DeathChestBlock>();

		try {

			PreparedStatement preparedStatement = 
					connection.prepareStatement(Queries.getQuery("SelectAllDeathChestBlocks"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				// create empty DeathChestBlock object
				DeathChestBlock deathChestBlock = new DeathChestBlock();

				// try to convert owner uuid from stored string
				try {
					deathChestBlock.setOwnerUUID(UUID.fromString(rs.getString("ownerid")));
				}
				catch (Exception e) {
					plugin.getLogger().warning("[SQLite getAllRecords] An error occured while trying to set ownerUUID.");
					plugin.getLogger().warning("[SQLite getAllRecords] ownerid string: " + rs.getString("ownerid"));
					plugin.getLogger().warning(e.getLocalizedMessage());
					continue;
				}

				// try to convert killer uuid from stored string, or set to null if invalid uuid
				try {
					deathChestBlock.setKillerUUID(UUID.fromString(rs.getString("killerid")));
				}
				catch (Exception e) {
					deathChestBlock.setKillerUUID(null);
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
				deathChestBlock.setLocation(location);
				deathChestBlock.setExpiration(rs.getLong("expiration"));

				// add DeathChestObject to results ArrayList
				results.add(deathChestBlock);
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
	final void putRecord(final DeathChestBlock deathChestBlock) {

		// if passed deathChestBlock is null, do nothing and return
		if (deathChestBlock == null) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				
				// catch invalid player uuid exception
				String ownerid = null;
				try {
					ownerid = deathChestBlock.getOwnerUUID().toString();
				}
				catch (Exception e) {
					plugin.getLogger().warning("DeathChestBlock owner UUID is invalid.");
					return;
				}

				// catch invalid killer uuid exception
				String killerid = null;
				try {
					killerid = deathChestBlock.getKillerUUID().toString();
				}
				catch (Exception e) {
					killerid = null;
				}

				try {
					// synchronize on database connection
					synchronized(connection) {

						// create prepared statement
						PreparedStatement preparedStatement = 
								connection.prepareStatement(Queries.getQuery("InsertDeathChestBlock"));

						preparedStatement.setString(1, ownerid);
						preparedStatement.setString(2, killerid);
						preparedStatement.setString(3, deathChestBlock.getLocation().getWorld().getName());
						preparedStatement.setInt(4, deathChestBlock.getLocation().getBlockX());
						preparedStatement.setInt(5, deathChestBlock.getLocation().getBlockY());
						preparedStatement.setInt(6, deathChestBlock.getLocation().getBlockZ());
						preparedStatement.setLong(7, deathChestBlock.getExpiration());

						// execute prepared statement
						int rowsAffected = preparedStatement.executeUpdate();

						// output debugging information
						if (plugin.debug) {
							plugin.getLogger().info(rowsAffected + " rows affected.");
						}
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
	public final void deleteRecord(final Location location) {

		// if passed location is null, do nothing and return
		if (location == null) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					// synchronize on database connection
					synchronized(connection) {

						// create prepared statement
						PreparedStatement preparedStatement = 
								connection.prepareStatement(Queries.getQuery("DeleteDeathChestBlock"));

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
	 * @param worldName
	 */
	final void deleteExpiredRecords(final String worldName) {

		// pastDueTime = current time in milliseconds - 30 days
		final Long pastDueTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);

		try {
			// create prepared statement
			PreparedStatement preparedStatement = 
					connection.prepareStatement(Queries.getQuery("DeleteExpiredDeathChestBlock"));

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
	final void delete() {
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		if (dataStoreFile.exists()) {
			dataStoreFile.delete();
		}
	}


	@Override
	final boolean exists() {
		// get path name to old data store file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		return dataStoreFile.exists();
	}

}
