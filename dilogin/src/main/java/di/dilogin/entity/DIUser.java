package di.dilogin.entity;

import java.util.Optional;

import org.bukkit.entity.Player;

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
	 * Bukkit player. Can be null if player is offline.
	 */
	private Optional<Player> playerBukkit;

	/**
	 * Discord Player object.
	 */
	private Optional<User> playerDiscord;
}
