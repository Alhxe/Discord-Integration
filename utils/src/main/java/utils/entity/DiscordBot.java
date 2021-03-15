package utils.entity;

import javax.security.auth.login.LoginException;

import org.bukkit.plugin.Plugin;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * Contains the main information of the bot.
 **/
@Getter
@Setter
public class DiscordBot {

	/**
	 * Javacord Api.
	 */
	private JDA api;

	/**
	 * Bot prefix.
	 */
	private String prefix;

	/**
	 * Main server id.
	 */
	private long serverid;

	/**
	 * Bot CommandHandler.
	 */
	private CommandHandler commandHandler;

	/**
	 * Main Controller.
	 * 
	 * @param prefix   Bot prefix.
	 * @param serverid Main server id.
	 * @param token    Bot token.
	 * @param plugin   Bukkit plugin.
	 */
	public DiscordBot(String prefix, long serverid, String token, Plugin plugin) {
		this.prefix = prefix;
		this.serverid = serverid;
		initBot(token, plugin);
	}

	/**
	 * Init bot.
	 * 
	 * @param token  Bot token.
	 * @param plugin Bukkit plugin.
	 */
	public void initBot(String token, Plugin plugin) {
		try {
			this.api = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.DIRECT_MESSAGES).build();
			api.awaitReady();
			onConnectToDiscord(plugin);
		} catch (LoginException e) {
			plugin.getLogger().warning("The Bot failed to start. You have not entered a valid token.");
			plugin.getPluginLoader().disablePlugin(plugin);
		} catch (InterruptedException e) {
			plugin.getLogger().warning("The Bot failed to start. Reason:");
			e.printStackTrace();
			plugin.getPluginLoader().disablePlugin(plugin);
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * When the bot starts successfully it passes here.
	 * 
	 * @param plugin Bukkit plugin.
	 */
	private void onConnectToDiscord(Plugin plugin) {
		plugin.getLogger().info("Bot started");
		this.commandHandler = new CommandHandler(prefix);
		api.addEventListener(this.commandHandler);
	}
}