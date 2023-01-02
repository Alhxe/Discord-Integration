package di.dilogin;

import java.util.logging.Level;

import di.dicore.api.DIApi;
import di.dicore.api.impl.DIApiBungeeImpl;
import di.dilogin.controller.DBController;
import di.dilogin.controller.MainController;
import di.dilogin.controller.impl.DILoginControllerBungeeImpl;
import di.dilogin.controller.impl.DiscordControllerImpl;
import di.dilogin.discord.command.DiscordRegisterBungeeCommand;
import di.dilogin.discord.event.UserReactionMessageBungeeEvent;
import di.dilogin.minecraft.bungee.command.ForceLoginBungeeCommand;
import di.dilogin.minecraft.bungee.command.RegisterBungeeCommand;
import di.dilogin.minecraft.bungee.command.UnregisterBungeeCommand;
import di.dilogin.minecraft.bungee.controller.ChannelMessageController;
import di.dilogin.minecraft.bungee.event.UserLeaveBungeeEvent;
import di.dilogin.minecraft.bungee.event.UserLoginBungeeEvent;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.exception.NoApiException;
import net.md_5.bungee.api.plugin.Plugin;

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
		MainController.setDiscordController(new DiscordControllerImpl());
		MainController.setBukkit(true);
		DBController.getConnect();
		initDiscordEvents();
		initDiscordCommands();
		initCommands();
		initEvents();

		// Events to get data from the DILogin database.
		plugin.getProxy().getPluginManager().registerListener(plugin, new ChannelMessageController());

		getLogger().info("Plugin started");
	}

	@Override
	public void onDisable() {
		TmpCache.clearAll();
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
	            MainController.getDIApi().getInternalController().getLogger().log(Level.SEVERE,"BungeeApplication - connectWithCoreApi",e);
			}
		} else {
			plugin.getLogger().log(Level.SEVERE,
					"Failed to connect to DICore plugin. Check if it has been turned on correctly.");
			plugin.onDisable();
		}
	}

	private void initCommands() {
		this.getProxy().getPluginManager().registerCommand(this, new RegisterBungeeCommand());
		this.getProxy().getPluginManager().registerCommand(this, new UnregisterBungeeCommand());
		this.getProxy().getPluginManager().registerCommand(this, new ForceLoginBungeeCommand());
	}

	private void initEvents() {
		this.getProxy().getPluginManager().registerListener(this, new UserLoginBungeeEvent());
		this.getProxy().getPluginManager().registerListener(this, new UserLeaveBungeeEvent());
	}

	/**
	 * Records Discord events.
	 */
	private void initDiscordEvents() {
		api.registerDiscordEvent(new UserReactionMessageBungeeEvent());
		if (MainController.getDILoginController().isSyncroRolEnabled()) {

		}
	}

	/**
	 * Init discord commands.
	 */
	private void initDiscordCommands() {
		api.registerDiscordCommand(new DiscordRegisterBungeeCommand());
	}
}
