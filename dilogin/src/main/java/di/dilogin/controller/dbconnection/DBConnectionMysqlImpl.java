package di.dilogin.controller.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;

import di.dilogin.controller.MainController;
import lombok.NoArgsConstructor;

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
            MainController.getDIApi().getInternalController().getLogger().info("Database connection type: MYSQL");
            ConfigManager cm = MainController.getDIApi().getInternalController().getConfigManager();
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
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DBConnectionMysqlImpl - initDB",e);
            MainController.getDIApi().getInternalController().disablePlugin();
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
            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"DBConnectionMysqlImpl - initTables",e);
        }
    }
}