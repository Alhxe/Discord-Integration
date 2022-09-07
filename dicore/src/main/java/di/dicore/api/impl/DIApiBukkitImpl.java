package di.dicore.api.impl;

import di.dicore.BukkitApplication;
import di.dicore.api.DIApi;
import di.internal.controller.impl.InternalControllerBukkitImpl;
import di.internal.entity.DiscordBot;
import org.bukkit.plugin.Plugin;

import di.internal.controller.CoreController;
import di.internal.controller.InternalController;
import di.internal.entity.DiscordCommand;
import di.internal.exception.NoApiException;
import lombok.Getter;

import java.util.Optional;

/**
 * Class used to communicate between the rest of the plugins.
 */
@Getter
public class DIApiBukkitImpl implements DIApi {

	/**
	 * Core controller.
	 */
	private final CoreController coreController;

	/**
	 * Contains the plugin driver.
	 */
	private final InternalController internalController;

	/**
	 * Main Discord Integration Api. When this class is instantiated, the internal
	 * controller of the core is obtained.
	 * 
	 * @param plugin      Plugin from where it is instantiated. The goal is the
	 *                    logger.
	 * @param classLoader Class loader.
	 * @param configFile  True if plugin has config file in DICore folder.
	 * @param langFile    True if plugin has lang file in DICore folder.
	 * @throws NoApiException In case the internal controller of the core is not
	 *                        instantiated, it will throw an error.
	 */
	public DIApiBukkitImpl(Plugin plugin, ClassLoader classLoader, boolean configFile, boolean langFile) throws NoApiException {
		if (BukkitApplication.getInternalController() == null) {
			throw new NoApiException(plugin.getLogger());
		}
		this.coreController = BukkitApplication.getInternalController();
		this.internalController = new InternalControllerBukkitImpl(plugin, coreController, classLoader, configFile, langFile);
		plugin.getLogger().info("DICore has successfully connected with " + plugin.getName());
	}


	/**
	 * Add a new event as listener.
	 * 
	 * @param listener Discord Listener.
	 */
	@Override
	public void registerDiscordEvent(Object listener) {
		this.coreController.getDiscordApi().addEventListener(listener);
	}

	/**
	 * Add a new command to command handler.
	 * 
	 * @param command Discord command.
	 */
	public void registerDiscordCommand(DiscordCommand command) {
		this.coreController.getBot().getCommandHandler().registerCommand(command);
	}
}
