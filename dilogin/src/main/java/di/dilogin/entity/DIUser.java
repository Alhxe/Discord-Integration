package di.dilogin.entity;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

/**
 * Represents a user registered in the system. Contains his discord and
 * minecraft information.
 */
@Getter
@Setter
@AllArgsConstructor
public class DIUser {

	/**
	 * Bukkit player name.
	 */
	private String playerName;

	/**
	 * Discord player user. Can be null if user left server.
	 */
	private Optional<User> playerDiscord;

}
