package di.dilogin.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

/**
 * This class represents a temporary registration / login message.
 */
@Getter
@Setter
@AllArgsConstructor
public class TmpMessage {

	/**
	 * Player name.
	 */
	private String player;
	
	/**
	 * Discord user.
	 */
	private User user;
	
	/**
	 * Registration or login request message.
	 */
	private Message message;
	
	/**
	 * Internal message code.
	 */
	private String code;

}
