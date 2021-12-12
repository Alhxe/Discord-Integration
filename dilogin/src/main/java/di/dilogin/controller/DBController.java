package di.dilogin.controller;

import java.sql.Connection;

import di.dilogin.BukkitApplication;
import di.dilogin.controller.dbconnection.DBConnection;
import di.dilogin.controller.dbconnection.DBConnectionMysqlImpl;
import di.dilogin.controller.dbconnection.DBConnectionSqliteImpl;

public class DBController {
	
	private DBController() {
		throw new IllegalStateException();
	}
	
	private static DBConnection conn;
	
	public static Connection getConnect() {
		if (conn==null)
			initConnection();
		return conn.getConnect();
	}
	
	public static void initConnection() {
		String db = BukkitApplication.getDIApi().getInternalController().getConfigManager().getString("database");
		if(db.equalsIgnoreCase("mysql")) {
			conn = new DBConnectionMysqlImpl();
		} else  {
			conn = new DBConnectionSqliteImpl();
		} 
	}

}
