package di.dilogin.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains the user's session information.
 */
@Getter
@Setter
@AllArgsConstructor
public class UserSession implements Serializable{

	/**
	 * Default serial ID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Player's name
	 */
	private String name;

	/**
	 * Player's ip.
	 */
	private String ip;
}
