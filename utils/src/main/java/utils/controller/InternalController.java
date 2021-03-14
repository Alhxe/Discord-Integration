package utils.controller;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import utils.entity.DiscordBot;
import utils.exception.NoApiException;

/**
 * Internal Controller of Plugin.
 */
@Getter
@Setter
public class InternalController {

	/**
	 * The driver for the plugin configuration file.
	 */
	private ConfigManager configManager;

	/**
	 * The bukkit plugin.
	 */
	private Plugin plugin;

	/**
	 * The driver for the plugin lang file.
	 */
	private LangManager langManager;

	/**
	 * Contains the bot config information.
	 */
	private DiscordBot bot;

	/**
	 * Path of the main plugin folder.
	 */
	private File dataFolder;

	/**
	 * Main constructor;
	 * 
	 * @param plugin Bukkit plugin.
	 * @throws NoApiException 
	 */
	public InternalController(Plugin plugin) {
		this.plugin = plugin;
		this.dataFolder = plugin.getDataFolder();
		this.configManager = new ConfigManager(this, "config.yml", plugin.getDataFolder());
		this.langManager = new LangManager(this, "lang.yml", plugin.getDataFolder());
		this.bot = initBot();
	}

	/**
	 * Save resource in plugin folder.
	 * 
	 * @param name Name of file.
	 */
	public void saveResource(String name) {
		plugin.saveResource(name, false);
	}

	/**
	 * Init Discord Bot.
	 * 
	 * @return Finished bot object.
	 */
	private DiscordBot initBot() {

		String token = configManager.getString("bot_token");
		long serverid = configManager.getLong("discord_server_id");
		String prefix = configManager.getString("discord_server_prefix");

		if (token == null || prefix == null || serverid == 0L) {
			this.plugin.getLogger().log(Level.SEVERE,
					"Failed to load the data required to start the bot. Did you enter the server ID, token and prefix correctly?");
			this.plugin.getPluginLoader().disablePlugin((Plugin) this);
		}

		this.plugin.getLogger().info("Starting Bot");
		return new DiscordBot(prefix, serverid, token, plugin);
	}

	/**
	 * @return Discord Api.
	 */
	public JDA getDiscordApi() {
		return this.bot.getApi();
	}

}