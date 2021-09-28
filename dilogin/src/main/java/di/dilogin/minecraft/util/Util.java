package di.dilogin.minecraft.util;

import java.util.Optional;

import org.bukkit.Server;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class Util {

	private Util() {
		throw new IllegalStateException();
	}

	/**
	 * Check if user is whitelisted and option is enabled.
	 * 
	 * @param user User to check.
	 * @return true if is whitelisted.
	 */
	public static boolean isWhiteListed(User user) {
		Optional<Role> optRole = requiredRole();
		if (optRole.isPresent()) {
			Role role = optRole.get();
			Member member = user.getJDA()
					.getGuildById(BukkitApplication.getDIApi().getCoreController().getBot().getServerid())
					.retrieveMember(user, true).complete();
			if (!member.getRoles().contains(role))
				return false;
		}
		return true;
	}

	/**
	 * Check if syncro option is enabled in cofig file.
	 * 
	 * @return true if its enabled.
	 */
	public static boolean isSyncronizeOptionEnabled() {
		try {
			return BukkitApplication.getDIApi().getInternalController().getConfigManager().getBoolean("syncro_enable");
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 
	 * @param server Active server.
	 * @return The number of the version of server.
	 */
	public static int getServerVersion(Server server) {
		String version = server.getVersion();
		if (version.contains("1.17"))
			return 17;
		if (version.contains("1.16"))
			return 16;
		if (version.contains("1.15"))
			return 15;
		if (version.contains("1.14"))
			return 14;
		if (version.contains("1.13"))
			return 13;
		if (version.contains("1.12"))
			return 12;
		if (version.contains("1.11"))
			return 11;
		if (version.contains("1.10"))
			return 10;
		if (version.contains("1.9"))
			return 9;
		if (version.contains("1.8"))
			return 8;
		if (version.contains("1.7"))
			return 7;
		return -1;
	}

	/**
	 * Check for required role to whitelist on config file.
	 * 
	 * @return optional role.
	 */
	private static Optional<Role> requiredRole() {
		DIApi api = BukkitApplication.getDIApi();
		JDA jda = BukkitApplication.getDIApi().getCoreController().getDiscordApi();
		Guild guild = jda.getGuildById(api.getCoreController().getBot().getServerid());

		Optional<Long> optionalLong = api.getInternalController().getConfigManager()
				.getOptionalLong("register_required_role_id");

		if (!optionalLong.isPresent())
			return Optional.empty();

		Role role = guild.getRoleById(optionalLong.get());
		if (role == null)
			return Optional.empty();

		return Optional.of(role);
	}

}
