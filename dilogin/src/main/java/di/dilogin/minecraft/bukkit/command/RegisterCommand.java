package di.dilogin.minecraft.bukkit.command;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import di.dilogin.controller.MainController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import di.dicore.api.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Command to register as a user.
 */
public class RegisterCommand implements CommandExecutor {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main api.
	 */
	private final DIApi api = BukkitApplication.getDIApi();

	/**
	 * Reactions emoji.
	 */
	private final String emoji = api.getInternalController().getConfigManager().getString("discord_embed_emoji");

	/**
	 * Main command body.
	 * @param sender The sender of the command.
	 * @param command The command.
	 * @param label The label of the command.
	 * @param args The arguments of the command.
	 * @return True if the command was executed.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (userDao.contains(player.getName())) {
				player.sendMessage(LangManager.getString(player.getName(), "register_already_exists"));
				return false;
			}

			if (args.length == 0) {
				player.sendMessage(LangManager.getString(player.getName(), "register_arguments"));
				return false;
			}

			Optional<User> userOpt = catchRegisterUserOption(args, player);
			if (!userOpt.isPresent())
				return false;

			User user = userOpt.get();

			if (userDao.getDiscordUserAccounts(user) >= api.getInternalController().getConfigManager()
					.getInt("register_max_discord_accounts")) {
				player.sendMessage(LangManager.getString(player.getName(), "register_max_accounts").replace("%user_discord_id%",
						arrayToString(args).replace(" ", "")));
				return false;
			}

			player.sendMessage(LangManager.getString(user, player.getName(), "register_submit"));

			MessageEmbed messageEmbed = getEmbedMessage(player, user);

			sendMessage(user, player, messageEmbed);

		}
		return true;
	}

	/**
	 * Find the user registration method (via his id or nametag).
	 * 
	 * @param args   Args from the command.
	 * @param player Minecraft player.
	 * @return Posible user.
	 */
	private Optional<User> catchRegisterUserOption(String[] args, Player player) {
		String string = arrayToString(args);

		Optional<User> userOpt = registerById(string);

		if (!userOpt.isPresent())
			userOpt = registerByName(string);

		if (!userOpt.isPresent()) {
			player.sendMessage(
					LangManager.getString(player.getName(), "register_user_not_detected").replace("%user_discord_id%", string));
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

		return Utils.getDiscordUserById(api.getCoreController().getDiscordApi(), Long.parseLong(string));
	}

	/**
	 * Get the user if his registration method is by discord username.
	 * 
	 * @param string Args from the command.
	 * @return Posible user.
	 */
	private Optional<User> registerByName(String string) {
		if (!usernameAndTagIsValid(string))
			return Optional.empty();

		Guild guild = api.getCoreController().getGuild();
		return Utils.getDiscordUserByUsernameAndTag(guild, string);

	}

	/**
	 * Send message to user register.
	 * 
	 * @param user         Discord user.
	 * @param player       Bukkit player.
	 * @param messageEmbed Embed message.
	 */
	private void sendMessage(User user, Player player, MessageEmbed messageEmbed) {
		if (!TmpCache.getRegisterMessage(player.getName()).isPresent()){
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
						message.addReaction(emoji).queue();
						TmpCache.addRegister(player.getName(), new TmpMessage(player, user, message, code));
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
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 * @param messageEmbed  Embed message.
	 * @param code   The code to register.
	 */
	private void sendServerMessage(User user, Player player, MessageEmbed messageEmbed, String code) {
		TextChannel serverchannel = api.getCoreController().getDiscordApi()
				.getTextChannelById(api.getInternalController().getConfigManager().getLong("channel"));

		assert serverchannel != null;
		serverchannel.sendMessage(user.getAsMention()).delay(Duration.ofSeconds(10)).flatMap(Message::delete).queue();

		Message servermessage = serverchannel.sendMessageEmbeds(messageEmbed).submit().join();
		servermessage.addReaction(emoji).queue();
		TmpCache.addRegister(player.getName(), new TmpMessage(player, user, servermessage, code));
	}

	/**
	 * Create the log message according to the configuration.
	 * 
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 * @return Embed message configured.
	 */
	private MessageEmbed getEmbedMessage(Player player, User user) {
		EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(LangManager.getString(player.getName(), "register_discord_title"))
				.setDescription(LangManager.getString(user, player.getName(), "register_discord_desc")).setColor(
						Utils.hex2Rgb(api.getInternalController().getConfigManager().getString("discord_embed_color")));

		if (api.getInternalController().getConfigManager().getBoolean("discord_embed_server_image")) {
			Optional<Guild> optGuild = Optional.ofNullable(api.getCoreController().getDiscordApi()
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

	/**
	 * Check if the discord user name and tag is valid.
	 * 
	 * @param string Discord username with discriminator.
	 * @return True if username with tag is valid.
	 */
	private static boolean usernameAndTagIsValid(String string) {
		if (!string.contains("#"))
			return false;

		String name = string.substring(0, string.lastIndexOf('#'));
		String tag = string.substring(string.lastIndexOf('#') + 1);

		if (name.length() < 1)
			return false;

		if (tag.length() != 4)
			return false;

		try {
			Integer.parseInt(tag);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}