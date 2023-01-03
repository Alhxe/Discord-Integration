package di.dilogin.minecraft.bungee;

import di.dilogin.BungeeApplication;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Optional;

/**
 * Bungee Util class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BungeeUtil {

	/**
	 * @param playerName Minecraft player name.
	 * @return {@ProxiedPlayer} if player is online.
	 */
	public static Optional<ProxiedPlayer> getProxiedPlayer(String playerName) {
		return Optional.ofNullable(BungeeApplication.getPlugin().getProxy().getPlayer(playerName));
	}
}
