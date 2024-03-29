package di.dilogin.discord.command;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import di.dicore.api.DIApi;
import di.dilogin.controller.MainController;
import di.dilogin.controller.file.CommandAliasController;
import di.dilogin.controller.file.LangController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.bungee.BungeeUtil;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.entity.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Command to register as a user.
 */
public class DiscordRegisterBungeeCommand implements DiscordCommand {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main api.
	 */
	private final DIApi api = MainController.getDIApi();

	/**
	 * Main command body.
	 *
	 * @param message It is the message that comes after the command.
	 * @param event   It is the object that includes the event information.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void execute(String message, MessageReceivedEvent event) {
		
		if (!MainController.getDILoginController().isRegisterByDiscordCommandEnabled())
			return;

		event.getMessage().delete().delay(Duration.ofSeconds(20)).queue();
		if (userDao.containsDiscordId(event.getAuthor().getIdLong())) {
			event.getChannel().sendMessage(LangController.getString("register_already_exists"))
					.delay(Duration.ofSeconds(20)).flatMap(Message::delete).queue();
			return;
		}

		// Check account limits.
		if (userDao.getDiscordUserAccounts(event.getAuthor().getIdLong()) >= api.getInternalController()
				.getConfigManager().getInt("register_max_discord_accounts")) {
			event.getChannel().sendMessage(LangController.getString("register_max_accounts"))
					.delay(Duration.ofSeconds(20)).flatMap(Message::delete).queue();
			return;
		}

		// Check arguments.
		if (message.isEmpty()) {
			event.getChannel().sendMessage(LangController.getString("register_discord_arguments"))
					.delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
			return;
		}

		Optional<ProxiedPlayer> playerOpt = catchRegister(message, event);

		if (!playerOpt.isPresent()) {
			event.getChannel().sendMessage(LangController.getString("register_code_not_found"))
					.delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();
			return;
		}

		ProxiedPlayer player = playerOpt.get();
		Member member = event.getMember();

		// Create password.
		String password = CodeGenerator.getCode(8, api);
		player.sendMessage(LangController.getString(event.getAuthor(), player.getName(), "register_success")
				.replace("%authme_password%", password));
		// Send message to discord.
		MessageEmbed messageEmbed = getEmbedMessage(player, event.getAuthor());
		event.getChannel().sendMessageEmbeds(messageEmbed).delay(Duration.ofSeconds(10)).flatMap(Message::delete)
				.queue();

		// Add user to data base.
		userDao.add(new DIUser(player.getName(), Optional.of(event.getAuthor())));

		// Check if user will get some discord role and give to him.
		if (MainController.getDILoginController().isRegisterGiveRoleEnabled()) {
			List<Long> roleList = MainController.getDIApi().getInternalController().getConfigManager()
					.getLongList("register_give_role_list");
			for (long roleId : roleList) {
				MainController.getDiscordController().giveRole(roleId + "", player.getName(), member,
						"by registering on the server.");
			}
		}

		// Check if is whitelisted to login.
		if (!MainController.getDiscordController().isWhiteListed(player.getName(), event.getMember())) {
			player.sendMessage(LangController.getString(player.getName(), "login_without_role_required"));
		} else {
			TmpCache.removeRegister(player.getName());
			MainController.getDILoginController().loginUser(player.getName(), member.getUser());
		}

	}

	/**
	 * Catch registration method.
	 *
	 * @param message Args from the message.
	 * @param event   Event of the message.
	 * @return Player if exits and is not registered.
	 */
	public Optional<ProxiedPlayer> catchRegister(String message, MessageReceivedEvent event) {
		Optional<ProxiedPlayer> player = BungeeUtil.getProxiedPlayer(registerByCode(message).get());

		if (!player.isPresent())
			player = registerByUserName(message, event);

		return player;
	}

	/**
	 * Register by code.
	 *
	 * @param message Args from the message.
	 * @return Player if exits and is not registered.
	 */
	public Optional<String> registerByCode(String message) {
		// Check code.
		Optional<TmpMessage> tmpMessageOpt = TmpCache.getRegisterMessageByCode(message);
		return tmpMessageOpt.map(TmpMessage::getPlayer);

	}

	/**
	 * Register by username.
	 *
	 * @param message Args from the message.
	 * @param event   Event of the message.
	 * @return Player if exits and is not registered.
	 */
	public Optional<ProxiedPlayer> registerByUserName(String message, MessageReceivedEvent event) {

		Optional<ProxiedPlayer> playerOpt = BungeeUtil.getProxiedPlayer(message);

		if (!playerOpt.isPresent())
			return Optional.empty();

		if (userDao.contains(message)) {
			event.getChannel().sendMessage(LangController.getString("register_already_exists"))
					.delay(Duration.ofSeconds(20)).flatMap(Message::delete).queue();
			return Optional.empty();
		}
		return playerOpt;
	}

	@Override
	public String getAlias() {
		return CommandAliasController.getAlias("register_discord_command");
	}

	/**
	 * Create the log message according to the configuration.
	 *
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 * @return Embed message configured.
	 */
	private MessageEmbed getEmbedMessage(ProxiedPlayer player, User user) {
		EmbedBuilder embedBuilder = MainController.getDILoginController().getEmbedBase()
				.setTitle(LangController.getString(player.getName(), "register_discord_title"))
				.setDescription(LangController.getString(user, player.getName(), "register_discord_success"));
		return embedBuilder.build();
	}

}
