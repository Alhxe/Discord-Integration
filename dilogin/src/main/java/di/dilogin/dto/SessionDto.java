package di.dilogin.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains session information.
 */
@Getter
@Setter
@ToString
public class SessionDto {

	/**
	 * Minecraft player name.
	 */
	String playerName;

	/**
	 * User ip.
	 */
	String ip;

	/**
	 * True if session is valid.
	 */
	boolean isValid;
}
