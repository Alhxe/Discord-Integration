package di.internal.utils;

import java.awt.Color;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

/**
 * General utilities.
 */
public class Utils {

	/**
	 * Prohibits instantiation of the class.
	 */
	private Utils() {
		throw new IllegalStateException();
	}

	/**
	 * @param colorStr hexadecimal color.
	 * @return Color.
	 */
	public static Color hex2Rgb(String colorStr) {
		return new Color(Integer.valueOf(colorStr.substring(1, 3), 16).intValue(),
				Integer.valueOf(colorStr.substring(3, 5), 16).intValue(),
				Integer.valueOf(colorStr.substring(5, 7), 16).intValue());
	}

	/**
	 * @param api Discord JDA api.
	 * @param id  Discord user id.
	 * @return Possible user based on their ID.
	 */
	public static Optional<User> getDiscordUserById(JDA api, long id) {
		Optional<User> cachedUserOpt = Optional.ofNullable(api.getUserById(id));
		if (cachedUserOpt.isPresent())
			return cachedUserOpt;

		try {
			Optional<User> userOpt = Optional.ofNullable(api.retrieveUserById(id).submit().get());
			if (userOpt.isPresent())
				return userOpt;

		} catch (InterruptedException | ExecutionException e) {
			System.out.println("Unable to get user with id " + id);
			Thread.currentThread().interrupt();
		}
		return Optional.empty();
	}

	public static Optional<User> getDiscordUserByUsernameAndTag(Guild guild, String string) {
		String name = string.substring(0, string.lastIndexOf('#'));
		String tag = string.substring(string.lastIndexOf('#') + 1, string.length());
		System.out.println("name: "+name + " - tag: "+tag);
		Optional<Member> cachedUserOpt = Optional.ofNullable(guild.getMemberByTag(name, tag));
		System.out.println("encontrado: "+cachedUserOpt);
		if (cachedUserOpt.isPresent())
			return Optional.of(cachedUserOpt.get().getUser());

		List<User> userOpt = new ArrayList<>();
		guild.retrieveMembersByPrefix(name, 100).onSuccess(members -> {
			Member member = members.stream().filter(m -> m.getUser().getAsTag().equals(tag)).findFirst().orElse(null);
			if (member != null) {
				userOpt.add(member.getUser());
			} else {
				System.out.println("More than 100 names have been found that begin with " + string);
				// there are either more than 100 users with the same name or the member is not
				// int he server
			}
		});
		if (!userOpt.isEmpty())
			return Optional.of(userOpt.get(0));

		return Optional.empty();
	}

	/**
	 * @param plugin     Bukkit plugin.
	 * @param playerName Bukkit player's name.
	 * @return Possible player based on their name.
	 */
	public static Optional<Player> getUserPlayerByName(Plugin plugin, String playerName) {
		return Optional.ofNullable(plugin.getServer().getPlayer(playerName));
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
				message.delete().queue();
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		});
	}

	/**
	 * @param fileName    File name.
	 * @param classLoader Loader.
	 * @return File from jar or resources.
	 */
	public static InputStream getFileFromResourceAsStream(ClassLoader classLoader, String fileName) {
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		if (inputStream == null) {
			throw new IllegalArgumentException("File not found! " + fileName);
		} else {
			return inputStream;
		}
	}
}