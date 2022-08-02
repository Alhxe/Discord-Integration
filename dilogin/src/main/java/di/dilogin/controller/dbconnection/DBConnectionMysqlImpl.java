package di.dilogin.controller.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import lombok.NoArgsConstructor;
import org.bukkit.plugin.Plugin;

import di.dilogin.BukkitApplication;
import di.internal.controller.file.ConfigManager;

/**
 * MySQL controller class.
 */
@NoArgsConstructor
public class DBConnectionMysqlImpl implements DBConnection {

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
        try {
            BukkitApplication.getPlugin().getLogger().info("Database connection type: MYSQL");
            ConfigManager cm = BukkitApplication.getDIApi().getInternalController().getConfigManager();
            String host = cm.getString("database_host");
            String port = cm.getString("database_port");
            String user = cm.getString("database_username");
            String password = cm.getString("database_password");
            String table = cm.getString("database_table");
            String url = host + ":" + port + "/" + table + "?characterEncoding=utf8";
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + url, user, password);
            initTables();
        } catch (SQLException | ClassNotFoundException e) {
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
        sql.add("CREATE TABLE IF NOT EXISTS user(username varchar(17) primary key, discord_id varchar(30));");
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