package di.dilogin.controller;

import java.sql.Connection;

import di.dilogin.BukkitApplication;
import di.dilogin.controller.dbconnection.DBConnection;
import di.dilogin.controller.dbconnection.DBConnectionMysqlImpl;
import di.dilogin.controller.dbconnection.DBConnectionSqliteImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Driver used for managing connections to the database.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DBController {

    /**
     * The connection object to the database.
     */
    private static DBConnection conn;

    /**
     * Get the connection to the database.
     * If the connection is not yet established, it will be established.
     *
     * @return the connection object to the database.
     */
    public static Connection getConnect() {
        if (conn == null)
            initConnection();
        return conn.getConnect();
    }

    /**
     * Initializes the connection to the database.
     */
    public static void initConnection() {
        String db = BukkitApplication.getDIApi().getInternalController().getConfigManager().getString("database");
        if (db.equalsIgnoreCase("mysql")) {
            conn = new DBConnectionMysqlImpl();
        } else {
            conn = new DBConnectionSqliteImpl();
        }
    }

}
