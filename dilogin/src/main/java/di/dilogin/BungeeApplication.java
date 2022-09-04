package di.dilogin;

import di.dicore.api.DIApi;
import di.dicore.api.impl.DIApiBungeeImpl;
import di.dilogin.controller.DBController;
import di.dilogin.controller.MainController;
import di.dilogin.controller.impl.DILoginControllerBungee;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.exception.NoApiException;
import jdk.tools.jmod.Main;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Level;

/**
 * Main DILogin class for Bungee.
 */
public class BungeeApplication extends Plugin {

	/**
	 * Discord Integration Core Api.
	 */
	private static DIApi api;

	/**
	 * Main DILogin plugin.
	 */
	private static Plugin plugin;

	@Override
	public void onEnable() {
		plugin = this;

		connectWithCoreApi();
		MainController.setDIApi(api);
		MainController.setDILoginController(new DILoginControllerBungee());
		MainController.setBukkit(true);
		DBController.getConnect();

		initCommands();
		initEvents();
		initDiscordEvents();
		initDiscordCommands();

		getLogger().info("Plugin started");
	}

	@Override
	public void onDisable() {
		TmpCache.clearAll();
	}

	/**
	 * @return Discord Integration Api.
	 */
	public static DIApi getDIApi() {
		return api;
	}

	/**
	 * @return Get Main Bungee plugin.
	 */
	public static Plugin getPlugin() {
		return plugin;
	}

	/**
	 * Add the commands to bukkit.
	 */
	private void initCommands() {

	}

	/**
	 * Add to each command that the server must respond in case it does not have
	 * permissions.
	 *
	 * @param command  Bukkit command.
	 */
	private void initUniqueCommand(String command) {

	}

	/**
	 * Connect with DIApi.
	 */
	private void connectWithCoreApi() {
		if (plugin.getProxy().getPluginManager().getPlugin("DICore") != null) {
			try {
				api = new DIApiBungeeImpl(plugin, this.getClass().getClassLoader(), true, true);
			} catch (NoApiException e) {
				e.printStackTrace();
			}
		} else {
			plugin.getLogger().log(Level.SEVERE,
					"Failed to connect to DICore plugin. Check if it has been turned on correctly.");
			plugin.onDisable();
		}
	}

	/**
	 * Init Bukkit events.
	 */
	private void initEvents() {

	}

	/**
	 * Records Discord events.
	 */
	private void initDiscordEvents() {

	}

	/**
	 * Init discord commands.
	 */
	private void initDiscordCommands() {

	}

}
