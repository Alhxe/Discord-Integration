package di.dilogin.controller.dbconnection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;

import di.dilogin.controller.MainController;
import di.internal.controller.InternalController;
import lombok.NoArgsConstructor;

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
	 * Main plugin controller.
	 */
	private static final InternalController controller = MainController.getDIApi().getInternalController();

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
		controller.getLogger().info("Database connection type: SQLITE");
		try {
			File dataFolder = new File(
					controller.getDataFolder().getAbsolutePath(), "users.db");
			if (!dataFolder.exists()) {
				boolean success = dataFolder.createNewFile();
				if (!success) {
					controller.getLogger().severe("Failed to create database file");
					controller.disablePlugin();
				}
			}
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
			initTables();
		} catch (SQLException | ClassNotFoundException | IOException e) {
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DBConnectionSlqiteImpl - initTables",e);
			controller.disablePlugin();
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
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DBConnectionSqliteImpl - initTables",e);
		}
	}
}
