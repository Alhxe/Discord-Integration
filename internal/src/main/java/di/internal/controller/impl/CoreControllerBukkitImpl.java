package di.internal.controller.impl;

import di.internal.controller.CoreController;
import di.internal.controller.PluginController;
import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;
import di.internal.entity.DiscordBot;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bukkit implementation for {@link CoreController}.
 */
@Getter
public class CoreControllerBukkitImpl implements PluginController, CoreController {

	/**
	 * The manager for the plugin configuration file.
	 */
	private final ConfigManager configManager;

	/**
	 * The manager for the plugin lang file.
	 */
	private final YamlManager langManager;

	/**
	 * The Bukkit plugin.
	 */
	private final Plugin plugin;

	/**
	 * Contains the bot config information.
	 */
	private DiscordBot bot;

	/**
	 * Path of the main plugin folder.
	 */
	private final File dataFolder;

	/**
	 * The classLoader.
	 */
	private final ClassLoader classLoader;

	public CoreControllerBukkitImpl(Plugin plugin, ClassLoader classLoader, boolean isDataInBungee) {
		this.plugin = plugin;
		this.classLoader = classLoader;
		this.dataFolder = plugin.getDataFolder();
		this.configManager = new ConfigManager(this, plugin.getDataFolder(), classLoader, isDataInBungee);
		this.langManager = new YamlManager(this, "lang.yml", plugin.getDataFolder(), classLoader, configManager.getBoolean("bungeecord"));
		if (!configManager.getBoolean("bungeecord")) {
			this.bot = initBot();
		}
	}

	@Override
	public YamlManager getFile(String file) {
		return new YamlManager(this, file + ".yml", dataFolder, classLoader, false);
	}

	@Override
	public Optional<JDA> getDiscordApi() {
		return this.bot.getApi();
	}

	public Optional<Guild> getGuild() {
		return Optional.ofNullable(bot.getApi().get().getGuildById(bot.getServerId()));
	}

	@Override
	public Logger getLogger() {
		return plugin.getLogger();
	}

	@Override
	public void disablePlugin() {
		plugin.getServer().getPluginManager().disablePlugin(plugin);
	}

	@Override
	public DiscordBot getBot() {
		return bot;
	}

	@Override
	public void setBotInfo(String prefix, long serverId) {
		this.bot = new DiscordBot(prefix, serverId);
	}

	/**
	 * Init Discord Bot.
	 *
	 * @return Finished bot object.
	 */
	private DiscordBot initBot() {
		long serverId = 0;

		String token = configManager.getString("bot_token");
		try {
			serverId = configManager.getLong("discord_server_id");
		} catch (Exception e) {
			getLogger().log(Level.SEVERE,
					"Failed to get server ID. Modify the config.yml file to be able to start the plugin.");
		}
		String prefix = configManager.getString("discord_server_prefix");

		if (token == null || prefix == null || serverId == 0L) {
			getLogger().log(Level.SEVERE,
					"Failed to load the data required to start the bot. Did you enter the server ID, token and prefix correctly?");
			disablePlugin();
			return null;
		}

		getLogger().info("Starting Bot");
		return new DiscordBot(prefix, serverId, token, this);
	}
}