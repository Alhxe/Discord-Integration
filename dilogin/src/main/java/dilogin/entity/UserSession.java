package dilogin.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Contains the user's session information.
 */
@Getter @Setter @RequiredArgsConstructor
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
