package di.internal.controller;

import di.internal.controller.file.ConfigManager;

import java.util.logging.Logger;

/**
 * Interace of internal controllers.
 */
public interface PluginController {
	
	/**
	 * @return Plugin configuration controller.
	 */
	ConfigManager getConfigManager();

	/**
	 * @return Plugin logger.
	 */
	Logger getLogger();

	/**
	 * Disable the plugin.
	 */
	void disablePlugin();

}
