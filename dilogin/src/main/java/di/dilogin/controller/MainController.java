package di.dilogin.controller;

import di.dicore.api.DIApi;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class used to save instantiation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MainController {

	/**
	 * Main DILoginController.
	 */
	private static DILoginController controller;

	/**
	 * Main Discord Controller.
	 */
	private static DiscordController discordController;

	/**
	 * Main DIApi.
	 */
	private static DIApi api;

	/**
	 * Information about whether the plugin is running in Bukkit.
	 */
	private static boolean isBukkit = false;

	/**
	 * Information about whether the plugin is running in BungeeCord.
	 */
	private static boolean isBungee = false;

	/**
	 * @return Main DILoginController.
	 */
	public static DILoginController getDILoginController() {
		return controller;
	}

	/**
	 * @param dilogincontroller Main DILoginController.
	 */
	public static void setDILoginController(DILoginController dilogincontroller) {
		MainController.controller = dilogincontroller;
	}

	/**
	 * @return Main DIApi.
	 */
	public static DIApi getDIApi() {
		return api;
	}

	/**
	 * @param diapi Main DIApi.
	 */
	public static void setDIApi(DIApi diapi) {
		MainController.api = diapi;
	}

	/**
	 * @return Information about whether the plugin is running in Bukkit.
	 */
	public static boolean isBukkit() {
		return isBukkit;
	}

	/**
	 * @param b Information about whether the plugin is running in Bukkit.
	 */
	public static void setBukkit(boolean b) {
		MainController.isBukkit = b;
	}

	/**
	 * @return Information about whether the plugin is running in BungeeCord.
	 */
	public static boolean isBungee() {
		return isBungee;
	}

	/**
	 * @param b Information about whether the plugin is running in BungeeCord.
	 */
	public static void setBungee(boolean b) {
		MainController.isBungee = b;
	}

	/**
	 * @return Main discord controller.
	 */
	public static DiscordController getDiscordController() {
		return discordController;
	}

	/**
	 * @param dc Discord Controller.
	 */
	public static void setDiscordController(DiscordController dc) {
		discordController = dc;
	}
}
