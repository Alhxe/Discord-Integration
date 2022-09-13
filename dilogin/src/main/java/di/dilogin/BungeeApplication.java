package di.dilogin;

import di.dicore.api.DIApi;
import di.dicore.api.impl.DIApiBungeeImpl;
import di.dilogin.controller.DBController;
import di.dilogin.controller.MainController;
import di.dilogin.controller.impl.DILoginControllerBungeeImpl;
import di.dilogin.minecraft.bungee.controller.ChannelMessageController;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.exception.NoApiException;
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
		MainController.setDILoginController(new DILoginControllerBungeeImpl());
		MainController.setBukkit(true);
		DBController.getConnect();

		// Events to get data from the DILogin database.
		plugin.getProxy().getPluginManager().registerListener(plugin, new ChannelMessageController());
		
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
}
