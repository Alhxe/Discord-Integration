package di.internal.entity;

import java.util.Optional;

import di.internal.controller.CoreController;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
	private Optional<JDA> api;

	/**
	 * Bot prefix.
	 */
	private String prefix;

	/**
	 * Main server id.
	 */
	private long serverId;

	/**
	 * Bot CommandHandler.
	 */
	private CommandHandler commandHandler;
	
	/**
	 * Bot SlashCommandHandler.
	 */
	private SlashCommandHandler slashCommandHandler;

	/**
	 * CoreController of plugin.
	 */
	private CoreController controller;

	/**
	 * Main Controller.
	 * 
	 * @param prefix   Bot prefix.
	 * @param serverId Main server id.
	 * @param token    Bot token.
	 * @param controller  CoreController of plugin.
	 */
	public DiscordBot(String prefix, long serverId, String token, CoreController controller) {
		this.prefix = prefix;
		this.serverId = serverId;
		this.controller = controller;
		initBot(token);
	}

	/**
	 * Constructor for when the bot is not hosted on the server.
	 *
	 * @param prefix  Bot prefix.
	 * @param serverId Main server id.
	 */
	public DiscordBot(String prefix, long serverId){
		this.prefix = prefix;
		this.serverId = serverId;
	}

	/**
	 * Init bot.
	 * 
	 * @param token  Bot token.
	 */
	public void initBot(String token) {
		try {
			JDA api = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_PRESENCES,
					GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT).build();
			api.awaitReady();
			this.api = Optional.of(api);
			onConnectToDiscord();
			checkPermissions();
		} catch (InterruptedException e) {
			controller.getLogger().warning("The Bot failed to start. Reason:");
			e.printStackTrace();
			controller.disablePlugin();
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			controller.getLogger().warning("The Bot failed to start. You have not entered a valid token.");
			controller.disablePlugin();
		}
	}

	/**
	 * When the bot starts successfully it passes here.
	 */
	private void onConnectToDiscord() {
		controller.getLogger().info("Bot started");
		this.commandHandler = new CommandHandler(prefix);
		this.slashCommandHandler = new SlashCommandHandler();
		api.get().addEventListener(this.commandHandler);
		api.get().addEventListener(this.slashCommandHandler);
	}

	/**
	 * Check discord server permissions.
	 */
	private void checkPermissions() {
		Optional<Guild> guildOpt = Optional.ofNullable(api.get().getGuildById(serverId));

		if (!guildOpt.isPresent()) {
			controller.getLogger().warning("I could not find the server with ID " + serverId);
			return;
		}

		Guild guild = guildOpt.get();
		Member bot = guild.getSelfMember();

		if (bot.hasPermission(Permission.ADMINISTRATOR))
			return;

		if (!bot.hasPermission(Permission.MESSAGE_SEND))
			controller.getLogger()
					.warning("The bot does not have writes permission on the server, this could cause a conflict!");

		if (!bot.hasPermission(Permission.MESSAGE_MANAGE))
			controller.getLogger()
					.warning("The bot is not allowed to handle messages on the server, this could lead to conflict!");

		if (!bot.hasPermission(Permission.MESSAGE_ADD_REACTION))
			controller.getLogger().warning(
					"The bot does not have permission to add reactions on the server, this could cause conflict!");
	}
}