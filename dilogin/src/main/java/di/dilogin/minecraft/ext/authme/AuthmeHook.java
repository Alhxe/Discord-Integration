package di.dilogin.minecraft.ext.authme;

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

	/**
	 * Check if player is authenticated on Authme.
	 * 
	 * @param player Bukkit player.
	 * @return True if player is authenticated.
	 */
	public static boolean isLogged(Player player) {
		return authmeApi.isAuthenticated(player);
	}
	
	/**
	 * Check if player is registered on Authme.
	 * 
	 * @param player Bukkit player.
	 * @return True if player is registered.
	 */
	public static boolean isRegistered(Player player) {
		return authmeApi.isRegistered(player.getName());
	}
}
