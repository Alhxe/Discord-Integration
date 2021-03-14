package dicore;

import org.bukkit.plugin.Plugin;

import lombok.Getter;
import utils.controller.InternalController;
import utils.exception.NoApiException;

/**
 * Class used to communicate between the rest of the plugins.
 */
@Getter
public class DIApi {

	/**
	 * Core controller
	 */
	private InternalController internalController;

	/**
	 * Main Discord Integration Api. When this class is instantiated, the internal controller of the core is obtained.
	 * @param plugin Plugin from where it is instantiated. The goal is the logger.
	 * @throws NoApiException In case the internal controller of the core is not instantiated, it will throw an error.
	 */
	public DIApi(Plugin plugin) throws NoApiException {
		if (BukkitApplication.getInternalController() == null) {
			throw new NoApiException(plugin);
		}
		this.internalController = BukkitApplication.getInternalController();
	}

}
