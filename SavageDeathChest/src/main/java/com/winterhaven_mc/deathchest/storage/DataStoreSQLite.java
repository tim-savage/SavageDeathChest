package com.winterhaven_mc.deathchest.storage;

import com.winterhaven_mc.deathchest.PluginMain;
import com.winterhaven_mc.deathchest.chests.ChestBlock;
import com.winterhaven_mc.deathchest.chests.DeathChest;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.winterhaven_mc.deathchest.storage.Queries.getQuery;


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

		// enable foreign keys
		statement.executeUpdate(getQuery("EnableForeignKeys"));

		// execute table creation statements
		statement.executeUpdate(getQuery("CreateDeathChestTable"));
		statement.executeUpdate(getQuery("CreateDeathBlockTable"));

		// set initialized true
		setInitialized(true);

		// output log message
		plugin.getLogger().info(this.getName() + " datastore initialized.");
	}


	@Override
	public final List<ChestBlock> getAllBlockRecords() {

		final List<ChestBlock> results = new ArrayList<>();

		try {

			PreparedStatement preparedStatement =
					connection.prepareStatement(getQuery("SelectAllBlocks"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				// create empty DeathChestBlock object
				ChestBlock chestBlock = new ChestBlock();

				// try to convert chest uuid from stored string
				try {
					chestBlock.setChestUUID(UUID.fromString(rs.getString("ChestUUID")));
				}
				catch (Exception e) {
					plugin.getLogger().warning("[SQLite getAllBlockRecords] An error occurred while trying to set chestUUID.");
					plugin.getLogger().warning("[SQLite getAllBlockRecords] chestUUID string: " + rs.getString("ChestUUID"));
					plugin.getLogger().warning(e.getLocalizedMessage());
					continue;
				}

				String worldName = rs.getString("WorldName");

				// check that world is valid
				if (plugin.getServer().getWorld(worldName) == null) {

					// world does not exist, so output log message and continue to next record
					// plugin.getLogger().warning("Saved deathchest world '" + worldName + "' does not exist.");

					// delete all records expired more than 30 days in database that have this invalid world
					deleteOrphanedChests(worldName);
					continue;
				}

				// create Location object from database fields
				Location location = new Location(plugin.getServer().getWorld(worldName),
						rs.getInt("X"),
						rs.getInt("Y"),
						rs.getInt("Z"));

				// set other fields in deathChestBlock from database fields
				chestBlock.setLocation(location);

				// add DeathChestObject to results ArrayList
				results.add(chestBlock);
			}
		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch all block records from the SQLite database.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.printStackTrace();
			}
		}
		if (plugin.debug) {
			plugin.getLogger().info(results.size() + " block records fetched from SQLite datastore.");
		}
		return results;
	}


	@Override
	public final List<DeathChest> getAllChestRecords() {

		final List<DeathChest> results = new ArrayList<>();

		try {

			PreparedStatement preparedStatement =
					connection.prepareStatement(getQuery("SelectAllChests"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				// create empty DeathChestBlock object
				DeathChest deathChest = new DeathChest();

				// try to convert chest uuid from stored string
				try {
					deathChest.setChestUUID(UUID.fromString(rs.getString("ChestUUID")));
				}
				catch (Exception e) {
					plugin.getLogger().warning("[SQLite getAllChestRecords] An error occurred while trying to set chestUUID.");
					plugin.getLogger().warning("[SQLite getAllChestRecords] chestUUID string: " + rs.getString("ChestUUID"));
					plugin.getLogger().warning(e.getLocalizedMessage());
					continue;
				}

				// try to convert owner uuid from stored string
				try {
					deathChest.setOwnerUUID(UUID.fromString(rs.getString("OwnerUUID")));
				}
				catch (Exception e) {
					plugin.getLogger().warning("[SQLite getAllChestRecords] An error occurred while trying to set ownerUUID.");
					plugin.getLogger().warning("[SQLite getAllChestRecords] ownerUUID string: " + rs.getString("OwnerUUID"));
					plugin.getLogger().warning(e.getLocalizedMessage());
					continue;
				}

				// try to convert killer uuid from stored string, or set to null if invalid uuid
				try {
					deathChest.setKillerUUID(UUID.fromString(rs.getString("KillerUUID")));
				}
				catch (Exception e) {
					deathChest.setKillerUUID(null);
				}

				// set other fields in deathChestBlock from database fields
				deathChest.setItemCount(rs.getInt("ItemCount"));
				deathChest.setPlacementTime(rs.getLong("PlacementTime"));
				deathChest.setExpirationTime(rs.getLong("ExpirationTime"));

				// add DeathChestObject to results ArrayList
				results.add(deathChest);
			}
		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to fetch all chest records from the SQLite database.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.printStackTrace();
			}
		}
		if (plugin.debug) {
			plugin.getLogger().info(results.size() + " chest records fetched from SQLite datastore.");
		}
		return results;
	}


	@Override
	public synchronized final void putChestRecord(final DeathChest deathChest) {

		// if passed deathChestBlock is null, do nothing and return
		if (deathChest == null) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {

				// catch invalid chest uuid exception
				String chestUUID;
				try {
					chestUUID = deathChest.getChestUUID().toString();
				}
				catch (Exception e) {
					plugin.getLogger().warning("DeathChest chest UUID is invalid.");
					return;
				}

				// catch invalid player uuid exception
				String ownerUUID;
				try {
					ownerUUID = deathChest.getOwnerUUID().toString();
				}
				catch (Exception e) {
					plugin.getLogger().warning("DeathChest owner UUID is invalid.");
					return;
				}

				// catch invalid killer uuid exception
				String killerUUID;
				try {
					killerUUID = deathChest.getKillerUUID().toString();
				}
				catch (Exception e) {
					killerUUID = null;
				}

				try {
					// create prepared statement
					PreparedStatement preparedStatement =
							connection.prepareStatement(getQuery("InsertChestRecord"));

					preparedStatement.setString(1, chestUUID);
					preparedStatement.setString(2, ownerUUID);
					preparedStatement.setString(3, killerUUID);
					preparedStatement.setInt(4, deathChest.getItemCount());
					preparedStatement.setLong(5, deathChest.getPlacementTime());
					preparedStatement.setLong(6, deathChest.getExpirationTime());

					// execute prepared statement
					int rowsAffected = preparedStatement.executeUpdate();

					// output debugging information
					if (plugin.debug) {
						plugin.getLogger().info(rowsAffected + " rows affected.");
					}
				}
				catch (SQLException e) {

					// output simple error message
					plugin.getLogger().warning("An error occurred while inserting a DeathChest into the SQLite database.");
					plugin.getLogger().warning(e.getMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.printStackTrace();
					}
				}

				// insert each chest block into datastore
				for (ChestBlock chestBlock : deathChest.getChestBlocks()) {
					putBlockRecord(chestBlock);
				}

			}
		}.runTaskAsynchronously(plugin);
	}


	@Override
	public synchronized final void putBlockRecord(final ChestBlock chestBlock) {

		// if passed deathChestBlock is null, do nothing and return
		if (chestBlock == null) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {

				// catch invalid player uuid exception
				String chestUUID;
				try {
					chestUUID = chestBlock.getChestUUID().toString();
				}
				catch (Exception e) {
					plugin.getLogger().warning("ChestBlock chest UUID is invalid.");
					return;
				}

				try {
					// create prepared statement
					PreparedStatement preparedStatement =
							connection.prepareStatement(getQuery("InsertBlockRecord"));

					preparedStatement.setString(1, chestUUID);
					preparedStatement.setString(2, chestBlock.getLocation().getWorld().getName());
					preparedStatement.setInt(3, chestBlock.getLocation().getBlockX());
					preparedStatement.setInt(4, chestBlock.getLocation().getBlockY());
					preparedStatement.setInt(5, chestBlock.getLocation().getBlockZ());

					// execute prepared statement
					int rowsAffected = preparedStatement.executeUpdate();

					// output debugging information
					if (plugin.debug) {
						plugin.getLogger().info(rowsAffected + " rows inserted.");
					}
				}
				catch (SQLException e) {

					// output simple error message
					plugin.getLogger().warning("An error occurred while "
							+ "inserting a death chest block into the SQLite database.");
					plugin.getLogger().warning(e.getMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.printStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(plugin);
	}


	@Override
	synchronized public final void deleteChestRecord(final DeathChest deathChest) {

		// if passed deathChest is null, do nothing and return
		if (deathChest == null) {
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					// create prepared statement
					PreparedStatement preparedStatement =
							connection.prepareStatement(getQuery("DeleteChestByUUID"));

					preparedStatement.setString(1, deathChest.getChestUUID().toString());

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
							+ "delete a chest record from the " + toString() + " datastore.");
					plugin.getLogger().warning(e.getMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.printStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(plugin);

	}


	@Override
	synchronized public final void deleteBlockRecord(final ChestBlock chestBlock) {

		// if passed chestBlock is null, do nothing and return
		if (chestBlock == null) {
			return;
		}

		// get chest block location
		final Location location = chestBlock.getLocation();

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					// create prepared statement
					PreparedStatement preparedStatement =
							connection.prepareStatement(getQuery("DeleteBlockByLocation"));

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
	 * Delete orphaned chests in nonexistent world {@code worldName}
	 * @param worldName the world name of orphaned chests to delete
	 */
	private void deleteOrphanedChests(final String worldName) {

		// pastDueTime = current time in milliseconds - 30 days
		final long pastDueTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);

		try {
			// create prepared statement
			PreparedStatement preparedStatement = 
					connection.prepareStatement(getQuery("DeleteOrphanedChests"));

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
			plugin.getLogger().warning("An error occurred while attempting to delete orphaned chests from the SQLite database.");
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
				plugin.getLogger().warning("An error occurred while closing the SQLite database connection.");
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
