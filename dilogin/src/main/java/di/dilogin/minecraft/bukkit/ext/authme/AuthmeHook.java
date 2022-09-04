package di.dilogin.minecraft.bukkit.ext.authme;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import fr.xephi.authme.api.v3.AuthMeApi;

/**
 * Class that interacts with the authme api.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthmeHook {

	/**
	 * Authme api.
	 */
	private static final AuthMeApi authmeApi = AuthMeApi.getInstance();

	/**
	 * Start the player session
	 * 
	 * @param player Bukkit player.
	 */
	public static void login(Player player) {
		if (authmeApi.isRegistered(player.getName()))
			authmeApi.forceLogin(player);
	}

	/**
	 * Register a player
	 * 
	 * @param player   Bukkit player.
	 * @param password Default password.
	 */
	public static void register(Player player, String password) {
		if (!authmeApi.isRegistered(player.getName()))
			authmeApi.forceRegister(player, password, true);
	}
}
