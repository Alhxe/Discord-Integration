package di.dicore;

import org.bukkit.plugin.Plugin;

import di.internal.controller.CoreController;
import di.internal.controller.InternalController;
import di.internal.entity.DiscordCommand;
import di.internal.exception.NoApiException;
import lombok.Getter;

/**
 * Class used to communicate between the rest of the plugins.
 */
@Getter
public class DIApi {

	/**
	 * Core controller.
	 */
	private CoreController coreController;

	/**
	 * Contains the plugin driver.
	 */
	private InternalController internalController;

	/**
	 * Main Discord Integration Api. When this class is instantiated, the internal
	 * controller of the core is obtained.
	 * 
	 * @param plugin Plugin from where it is instantiated. The goal is the logger.
	 * @throws NoApiException In case the internal controller of the core is not
	 *                        instantiated, it will throw an error.
	 */
	public DIApi(Plugin plugin) throws NoApiException {
		if (BukkitApplication.getInternalController() == null) {
			throw new NoApiException(plugin);
		}
		this.coreController = BukkitApplication.getInternalController();
		this.internalController = new InternalController(plugin, coreController);
	}

	/**
	 * Add a new event as listener.
	 * 
	 * @param listener Discord Listener.
	 */
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
