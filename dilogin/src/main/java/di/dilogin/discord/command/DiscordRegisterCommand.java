package di.dilogin.discord.command;

import java.time.Duration;
import java.util.Optional;

import org.bukkit.entity.Player;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.AuthmeHook;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.entity.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Command to register as a user.
 */
public class DiscordRegisterCommand implements DiscordCommand {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = DILoginController.getDIUserDao();

	/**
	 * Main api.
	 */
	private final DIApi api = BukkitApplication.getDIApi();

	@Override
	public void execute(String message, MessageReceivedEvent event) {

		event.getMessage().delete().delay(Duration.ofSeconds(20)).queue();
		if (userDao.containsDiscordId(event.getAuthor().getIdLong())) {
			event.getChannel().sendMessage(LangManager.getString("register_already_exists"))
					.delay(Duration.ofSeconds(20)).flatMap(Message::delete).queue();
			return;
		}

		// Check account limits.
		if (userDao.getDiscordUserAccounts(event.getAuthor()) >= api.getInternalController().getConfigManager()
				.getInt("register_max_discord_accounts")) {
			event.getChannel().sendMessage(LangManager.getString("register_max_accounts")).delay(Duration.ofSeconds(20))
					.flatMap(Message::delete).queue();
			return;
		}

		// Check arguments.
		if (message.equals("") || message.isEmpty()) {
			event.getChannel().sendMessage(LangManager.getString("register_discord_arguments"))
					.delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
			return;
		}

		Optional<Player> playerOpt = catchRegister(message, event);

		if (!playerOpt.isPresent()) {
			event.getChannel().sendMessage(LangManager.getString("register_code_not_found"))
			.delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
			return;
		}

		Player player = playerOpt.get();

		// Create password.
		String password = CodeGenerator.getCode(8, api);
		player.sendMessage(LangManager.getString(event.getAuthor(), player, "register_success")
				.replace("%authme_password%", password));
		// Send message to discord.
		MessageEmbed messageEmbed = getEmbedMessage(player, event.getAuthor());
		event.getChannel().sendMessageEmbeds(messageEmbed).delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
		// Remove user from register cache.
		TmpCache.removeRegister(player.getName());
		// Add user to data base.
		userDao.add(new DIUser(Optional.of(player), Optional.of(event.getAuthor())));

		if (DILoginController.isAuthmeEnabled()) {
			AuthmeHook.register(player, password);
		} else {
			DILoginController.loginUser(player, event.getAuthor());
		}

	}

	/**
	 * Catch registration method.
	 * 
	 * @param message Args from the message.
	 * @param event   Event of the message.
	 * @return Player if exits and is not registered.
	 */
	public Optional<Player> catchRegister(String message, MessageReceivedEvent event) {
		Optional<Player> player = Optional.empty();

		player = registerByCode(message, event);

		if (!player.isPresent())
			player = registerByUserName(message, event);

		return player;
	}

	/**
	 * Register by code.
	 * 
	 * @param message Args from the message.
	 * @param event   Event of the message.
	 * @return Player if exits and is not registered.
	 */
	public Optional<Player> registerByCode(String message, MessageReceivedEvent event) {
		// Check code.
		Optional<TmpMessage> tmpMessageOpt = TmpCache.getRegisterMessageByCode(message);
		if (!tmpMessageOpt.isPresent()) {
			return Optional.empty();
		}

		return Optional.ofNullable(tmpMessageOpt.get().getPlayer());
	}

	/**
	 * Register by username.
	 * 
	 * @param message Args from the message.
	 * @param event   Event of the message.
	 * @return Player if exits and is not registered.
	 */
	public Optional<Player> registerByUserName(String message, MessageReceivedEvent event) {

		Optional<Player> playerOpt = Optional.ofNullable(api.getCoreController().getPlugin().getServer().getPlayer(message));

		if (!playerOpt.isPresent())
			return Optional.empty();
		
		if(userDao.contains(message)) {
			event.getChannel().sendMessage(LangManager.getString("register_already_exists"))
			.delay(Duration.ofSeconds(20)).flatMap(Message::delete).queue();
			return Optional.empty();
		}
		return playerOpt;
	}

	@Override
	public String getAlias() {

		return api.getInternalController().getConfigManager().getString("register_command");
	}

	/**
	 * Create the log message according to the configuration.
	 * 
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 * @return Embed message configured.
	 */
	private MessageEmbed getEmbedMessage(Player player, User user) {
		EmbedBuilder embedBuilder = DILoginController.getEmbedBase()
				.setTitle(LangManager.getString(player, "register_discord_title"))
				.setDescription(LangManager.getString(user, player, "register_discord_success"));
		return embedBuilder.build();
	}

}
