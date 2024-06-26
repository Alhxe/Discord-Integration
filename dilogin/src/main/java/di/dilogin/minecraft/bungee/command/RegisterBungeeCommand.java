package di.dilogin.minecraft.bungee.command;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import di.dicore.api.DIApi;
import di.dilogin.controller.MainController;
import di.dilogin.controller.file.CommandAliasController;
import di.dilogin.controller.file.LangController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command to register as a user.
 */
public class RegisterBungeeCommand extends Command {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main api.
	 */
	private final DIApi api = MainController.getDIApi();

	/**
	 * Reactions emoji.
	 */
	private final String emoji = api.getInternalController().getConfigManager().getString("discord_embed_emoji");

	public RegisterBungeeCommand() {
		super(CommandAliasController.getAlias("register_command"), "", "diregister");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer))
			return;
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		
		if (userDao.contains(player.getName())) {
			player.sendMessage(LangController.getString(player.getName(), "register_already_exists"));
			return;
		}

		if (args.length == 0) {
			player.sendMessage(LangController.getString(player.getName(), "register_arguments"));
			return;
		}

		Optional<User> userOpt = catchRegisterUserOption(args, player);
		if (!userOpt.isPresent())
			return;

		User user = userOpt.get();

		if (userDao.getDiscordUserAccounts(user.getIdLong()) >= api.getInternalController().getConfigManager()
				.getInt("register_max_discord_accounts")) {
			player.sendMessage(LangController.getString(player.getName(), "register_max_accounts")
					.replace("%user_discord_id%", arrayToString(args).replace(" ", "")));
			return;
		}

		player.sendMessage(LangController.getString(user, player.getName(), "register_submit"));

		MessageEmbed messageEmbed = getEmbedMessage(player, user);

		sendMessage(user, player, messageEmbed);

		return;
			
	}
	
	/**
	 * Find the user registration method (via his id or nametag).
	 * 
	 * @param args   Args from the command.
	 * @param player Minecraft player.
	 * @return Posible user.
	 */
	@SuppressWarnings("deprecation")
	private Optional<User> catchRegisterUserOption(String[] args, ProxiedPlayer player) {
		String string = arrayToString(args);
		
		Optional<User> userOpt = Optional.empty();

		if (MainController.getDILoginController().isRegisterByDiscordIdEnabled())
			userOpt = registerById(string);

		if (!userOpt.isPresent() && MainController.getDILoginController().isRegisterByNickNameEnabled())
			userOpt = registerByName(string);

		if (!userOpt.isPresent()) {
			player.sendMessage(LangController.getString(player.getName(), "register_user_not_detected")
					.replace("%user_discord_id%", string));
			return Optional.empty();
		}

		return userOpt;
	}
	
	/**
	 * Get the user if his registration method is by discord id.
	 * 
	 * @param string Args from the command.
	 * @return Posible user.
	 */
	private Optional<User> registerById(String string) {
		if (!idIsValid(string))
			return Optional.empty();

		return Util.getDiscordUserById(api.getCoreController().getDiscordApi().get(), Long.parseLong(string));
	}

	/**
	 * Get the user if his registration method is by discord username.
	 * 
	 * @param string Args from the command.
	 * @return Posible user.
	 */
	private Optional<User> registerByName(String string) {
		Guild guild = api.getCoreController().getGuild().get();
		return Util.getDiscordUserByUsername(guild, string);

	}

	/**
	 * Send message to user register.
	 * 
	 * @param user         Discord user.
	 * @param player       Bungee player.
	 * @param messageEmbed Embed message.
	 */
	private void sendMessage(User user, ProxiedPlayer player, MessageEmbed messageEmbed) {
		if (!TmpCache.getRegisterMessage(player.getName()).isPresent()) {
			api.getInternalController().getLogger().severe("Error while sending message to user register");
			return;
		}

		String code = TmpCache.getRegisterMessage(player.getName()).get().getCode();

		boolean hasMessagesOnlyChannel = api.getInternalController().getConfigManager()
				.contains("messages_only_channel");
		if (hasMessagesOnlyChannel)
			hasMessagesOnlyChannel = api.getInternalController().getConfigManager().getBoolean("messages_only_channel");

		if (hasMessagesOnlyChannel) {
			sendServerMessage(user, player, messageEmbed, code);
		} else {
			user.openPrivateChannel().submit()
					.thenAccept(channel -> channel.sendMessageEmbeds(messageEmbed).submit().thenAccept(message -> {
						message.addReaction(Emoji.fromFormatted(emoji)).queue();
						TmpCache.addRegister(player.getName(), new TmpMessage(player.getName(), user, message, code));
					}).whenComplete((message, error) -> {
						if (error == null)
							return;

						sendServerMessage(user, player, messageEmbed, code);
					}));
		}
	}

	/**
	 * Send embed message to the main discord channel.
	 * 
	 * @param player       Bungee player.
	 * @param user         Discord user.
	 * @param messageEmbed Embed message.
	 * @param code         The code to register.
	 */
	private void sendServerMessage(User user, ProxiedPlayer player, MessageEmbed messageEmbed, String code) {
		TextChannel serverchannel = api.getCoreController().getDiscordApi().get()
				.getTextChannelById(api.getInternalController().getConfigManager().getLong("channel"));

		assert serverchannel != null;
		serverchannel.sendMessage(user.getAsMention()).delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();

		Message servermessage = serverchannel.sendMessageEmbeds(messageEmbed).submit().join();
		servermessage.addReaction(Emoji.fromFormatted(emoji)).queue();
		TmpCache.addRegister(player.getName(), new TmpMessage(player.getName(), user, servermessage, code));
	}

	/**
	 * Create the log message according to the configuration.
	 * 
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 * @return Embed message configured.
	 */
	private MessageEmbed getEmbedMessage(ProxiedPlayer player, User user) {
		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle(LangController.getString(player.getName(), "register_discord_title"))
				.setDescription(LangController.getString(user, player.getName(), "register_discord_desc")).setColor(
						Util.hex2Rgb(api.getInternalController().getConfigManager().getString("discord_embed_color")));

		if (api.getInternalController().getConfigManager().getBoolean("discord_embed_server_image")) {
			Optional<Guild> optGuild = Optional.ofNullable(api.getCoreController().getDiscordApi().get()
					.getGuildById(api.getCoreController().getConfigManager().getLong("discord_server_id")));
			if (optGuild.isPresent()) {
				String url = optGuild.get().getIconUrl();
				if (url != null)
					embedBuilder.setThumbnail(url);
			}
		}

		if (api.getInternalController().getConfigManager().getBoolean("discord_embed_timestamp"))
			embedBuilder.setTimestamp(Instant.now());
		return embedBuilder.build();
	}

	/**
	 * @param string Array of string.
	 * @return Returns a string from array string.
	 */
	private static String arrayToString(String[] string) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < string.length; i++) {
			if (i != string.length - 1) {
				result.append(string[i]).append(" ");
			} else {
				result.append(string[i]);
			}
		}
		return result.toString();
	}

	/**
	 * Check if the user entered exists.
	 * 
	 * @param id Discord ID.
	 * @return True if id is valid.
	 */
	private static boolean idIsValid(String id) {
		try {
			Long.parseLong(id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
