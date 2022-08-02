package di.dilogin.controller.dbconnection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import lombok.NoArgsConstructor;
import org.bukkit.plugin.Plugin;

import di.dilogin.BukkitApplication;

/**
 * SQLite controller class.
 */
@NoArgsConstructor
public class DBConnectionSqliteImpl implements DBConnection {

	/**
	 * Main connection.
	 */
	private static Connection connection = null;

	/**
	 * @return Connection to the database. If it does not exist, it creates it.
	 */
	public Connection getConnect() {
		if (connection == null)
			initDB();
		return connection;
	}

	/**
	 * Init DataBase.
	 */
	private void initDB() {
		BukkitApplication.getPlugin().getLogger().info("Database connection type: SQLITE");
		try {
			File dataFolder = new File(
					BukkitApplication.getDIApi().getInternalController().getDataFolder().getAbsolutePath(), "users.db");
			if (!dataFolder.exists()) {
				boolean success = dataFolder.createNewFile();
				if (!success) {
					BukkitApplication.getPlugin().getLogger().severe("Failed to create database file");
					Plugin plugin = BukkitApplication.getPlugin();
					plugin.getPluginLoader().disablePlugin(plugin);
				}
			}
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
			initTables();
		} catch (SQLException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
			Plugin plugin = BukkitApplication.getPlugin();
			plugin.getPluginLoader().disablePlugin(plugin);
		}

	}

	/**
	 * @return Tables required for the database.
	 */
	private static ArrayList<String> createTables() {
		ArrayList<String> sql = new ArrayList<>();
		sql.add("CREATE TABLE IF NOT EXISTS user(username text primary key, discord_id varchar(30));");
		return sql;
	}

	/**
	 * Processes the tables in the database.
	 */
	private static void initTables() {
		try (Statement stmt = connection.createStatement()) {
			ArrayList<String> sql = createTables();
			for (String s : sql)
				stmt.execute(s);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
