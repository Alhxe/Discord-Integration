package dilogin.entity;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

/**
 * Represents a user registered in the system. Contains his discord and
 * minecraft information.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class DIUser {

	/**
	 * Bukkit Player object.
	 */
	private Player playerBukkit;

	/**
	 * Discord Player object.
	 */
	private User playerDiscord;
}
