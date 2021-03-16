package di.dilogin.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains the user's session information.
 */
@Getter @Setter @AllArgsConstructor
public class UserSession {
	
	/**
	 * Player's name
	 */
	private String name;
	
	/**
	 * Player's ip.
	 */
	private String ip;
}
