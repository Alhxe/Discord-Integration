package di.dilogin.controller.dbconnection;

import java.sql.Connection;

/**
 * Interface used to define the database used.
 */
public interface DBConnection {

	/**
	 * @return the connection to the database.
	 */
	public Connection getConnect();

}
