package di.dilogin.minecraft.command;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqliteImpl;
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
	private final DIUserDao userDao = new DIUserDaoSqliteImpl();

	/**
	 * Main api.
	 */
	private final DIApi api = BukkitApplication.getDIApi();

	/**
	 * Reactions emoji.
	 */
	private final String emoji = api.getInternalController().getConfigManager().getString("discord_embed_emoji");

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (userDao.contains(player.getName())) {
				player.sendMessage(LangManager.getString(player, "register_already_exists"));
				return false;
			}

			if (args.length == 0) {
				player.sendMessage(LangManager.getString(player, "register_arguments"));
				return false;
			}

			String nick = arrayToString(args);
			if (!nickIsDiscordUser(nick)) {
				player.sendMessage(LangManager.getString(player, "register_user_not_detected")
						.replace("%discriminated_discord_name%", nick.replace(" ", "")));
				return false;
			}

			Optional<User> userOpt = Optional
					.ofNullable(api.getCoreController().getDiscordApi().getUserByTag(nick.replace(" ", "")));
			if (!userOpt.isPresent()) {
				player.sendMessage(
						LangManager.getString(player, "Internal error.").replace("%discriminated_discord_name%", nick));
				return false;
			}

			User user = userOpt.get();

			player.sendMessage(LangManager.getString(user, player, "register_submit"));

			MessageEmbed messageEmbed = getEmbedMessage(player, user);

			user.openPrivateChannel().submit()
					.thenAccept(channel -> channel.sendMessage(messageEmbed).submit().thenAccept(message -> {
						message.addReaction(emoji).queue();
						TmpCache.addRegister(player.getName(), message);
					})).exceptionally(tw -> {
						try {
							TextChannel channel = api.getCoreController().getDiscordApi().getTextChannelById(
									api.getInternalController().getConfigManager().getLong("channel"));

							channel.sendMessage(user.getAsMention()).submit()
									.thenAccept(message -> deleteMessage(message, 5));

							Message message = channel.sendMessage(messageEmbed).submit().get();
							message.addReaction(emoji).complete();
							TmpCache.addRegister(player.getName(), message);

						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
							Thread.currentThread().interrupt();
						}
						return null;
					});
		}
		return true;
	}

	/**
	 * Create the log message according to the configuration.
	 * 
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 * @return Embed message configured.
	 */
	private MessageEmbed getEmbedMessage(Player player, User user) {
		EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(LangManager.getString(player, "register_discord_title"))
				.setDescription(LangManager.getString(user, player, "register_discord_desc")).setColor(
						Utils.hex2Rgb(api.getInternalController().getConfigManager().getString("discord_embed_color")));

		if (api.getInternalController().getConfigManager().getBoolean("discord_embed_server_image")) {
			Optional<Guild> optGuild = Optional.ofNullable(api.getCoreController().getDiscordApi()
					.getGuildById(api.getCoreController().getConfigManager().getLong("discord_server_id")));
			if (optGuild.isPresent()) {
				String url = optGuild.get().getIconUrl();
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
		String respuesta = "";
		for (int i = 0; i < string.length; i++) {
			if (i != string.length - 1) {
				respuesta = String.valueOf(respuesta) + string[i] + " ";
			} else {
				respuesta = String.valueOf(respuesta) + string[i];
			}
		}
		return respuesta;
	}

	/**
	 * Check if the user entered exists.
	 * 
	 * @param name Discord username with discriminator.
	 * @return True if user exists.
	 */
	private static boolean nickIsDiscordUser(String name) {
		try {
			BukkitApplication.getDIApi().getCoreController().getDiscordApi().getUserByTag(name);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Delete a message after a certain time.
	 * 
	 * @param message Message to be deleted.
	 * @param seconds Time in which it will be erased.
	 */
	public static void deleteMessage(Message message, int seconds) {
		Executors.newCachedThreadPool().submit(() -> {
			int millis = seconds * 1000;
			try {
				Thread.sleep(millis);
				message.delete().submit();
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		});
	}

}
