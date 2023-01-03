package di.dilogin.controller.file;

import di.dilogin.controller.MainController;
import di.dilogin.dto.DIUserDto;
import di.dilogin.entity.DIUser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.User;

/**
 * This class transforms the elements received from the lang file into
 * variables. (Placeholders)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LangController {

	/**
	 * @param path Variable searched.
	 * @return Original message.
	 */
	public static String getString(String path) {
		return MainController.getDIApi().getInternalController().getLangManager().getString(path).replace(
				"%minecraft_servername%",
				MainController.getDIApi().getCoreController().getConfigManager().getString("server_name"));
	}

	/**
	 * @param playerName Bukkit player.
	 * @param path       Variable searched.
	 * @return Message by changing the placeholders.
	 */
	public static String getString(String playerName, String path) {
		return getString(path).replace("%minecraft_username%", playerName);
	}

	/**
	 * @param user Discord user.
	 * @param path Variable searched.
	 * @return Message by changing the placeholders.
	 */
	public static String getString(DIUser user, String path) {
		return getString(user.getPlayerName(), path).replace("%discriminated_discord_name%",
				user.getPlayerDiscord().get().getName() + "#" + user.getPlayerDiscord().get().getDiscriminator());
	}

	/**
	 * @param user Discord user.
	 * @param path Variable searched.
	 * @return Message by changing the placeholders.
	 */
	public static String getString(DIUserDto user, String path) {
		return getString(user.getPlayerName(), path);
	}

	/**
	 * @param user       Discord user.
	 * @param playerName Bukkit player.
	 * @param path       Variable searched.
	 * @return Message by changing the placeholders.
	 */
	public static String getString(User user, String playerName, String path) {
		return getString(playerName, path).replace("%discriminated_discord_name%",
				user.getName() + "#" + user.getDiscriminator());
	}
}
