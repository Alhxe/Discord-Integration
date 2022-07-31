package di.dilogin.minecraft.util;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Server;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;
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
		Guild guild = getGuild();

		Optional<Long> optionalLong = api.getInternalController().getConfigManager()
				.getOptionalLong("register_required_role_id");

		if (!optionalLong.isPresent())
			return Optional.empty();

		Role role = guild.getRoleById(optionalLong.get());
		if (role == null)
			return Optional.empty();

		return Optional.of(role);
	}

	/**
	 * @param roleid Discord role id.
	 * @return True if the server contains the requested role.
	 */
	public static boolean serverHasRole(String roleid) {
		DIApi api = BukkitApplication.getDIApi();
		Guild guild = getGuild();
		Role role = guild.getRoleById(roleid);
		if (role == null) {
			String message = "Could not find ROL with id: " + roleid + ". Check the plugin settings to avoid problems.";
			api.getInternalController().getPlugin().getLogger().log(Level.SEVERE, message);
			return false;
		}
		return true;
	}

	/**
	 * @param roleid Discord role id.
	 * @param player Bukkit player name.
	 * @return True if the user has the role.
	 */
	public static boolean userHasRole(String roleid, String player) {
		Optional<Member> optMember = getMember(player);
		if (!optMember.isPresent())
			return false;

		Member member = optMember.get();
		List<Role> roles = member.getRoles();

		return roles.stream().anyMatch(role -> role.getId().equals(roleid));
	}

	/**
	 * Give a role to a discord user.
	 * 
	 * @param roleid Role id.
	 * @param player Bukkit player name.
	 * @param reason Reason for giving a role.
	 */
	public static void giveRole(String roleid, String player, String reason) {
		DIApi api = BukkitApplication.getDIApi();

		Optional<Member> optMember = getMember(player);
		if (!optMember.isPresent())
			return;

		Member member = optMember.get();
		Guild guild = getGuild();
		Role role = guild.getRoleById(roleid);

		try {
			guild.addRoleToMember(member, role).queue();
			api.getInternalController().getPlugin().getLogger().info(
					role.getName() + " role has been given to " + member.getUser().getAsTag() + ". Reason: " + reason);

		} catch (Exception e) {
			api.getInternalController().getPlugin().getLogger().log(Level.SEVERE,
					" Could not give " + role.getName() + " role to " + member.getUser().getAsTag()
							+ ". Reason:  Can't modify a role with higher or equal highest role than yourself");
		}
	}

	/**
	 * Remove a role to a discord user.
	 * 
	 * @param roleid Role id.
	 * @param player Bukkit player name.
	 * @param reason Reason for removing a role.
	 */
	public static void removeRole(String roleid, String player, String reason) {
		DIApi api = BukkitApplication.getDIApi();

		Optional<Member> optMember = getMember(player);
		if (!optMember.isPresent())
			return;

		Member member = optMember.get();
		Guild guild = getGuild();
		Role role = guild.getRoleById(roleid);

		try {
			guild.removeRoleFromMember(member, role).queue();
			api.getInternalController().getPlugin().getLogger().info(role.getName() + " role has been removed from "
					+ member.getUser().getAsTag() + ". Reason: " + reason);

		} catch (Exception e) {
			api.getInternalController().getPlugin().getLogger().log(Level.SEVERE,
					" Could not remove " + role.getName() + " role from " + member.getUser().getAsTag()
							+ ". Reason:  Can't modify a role with higher or equal highest role than yourself");
		}
	}

	/**
	 * Get a member from the discord server.
	 * 
	 * @param player Bukkit player name.
	 * @return Optional member.
	 */
	private static Optional<Member> getMember(String player) {
		DIUserDao dao = DILoginController.getDIUserDao();
		Guild guild = getGuild();

		Optional<DIUser> DIUserOpt = dao.get(player);
		if (!DIUserOpt.isPresent())
			return Optional.empty();

		List<Member> memberList = guild.findMembers(m -> m.getId().equals(DIUserOpt.get().getPlayerDiscord().get().getId()))
				.get();

		if (!memberList.isEmpty())
			return Optional.of(memberList.get(0));

		return Optional.empty();
	}

	/**
	 * @return the Discord server linked with the Minecraft server.
	 */
	private static Guild getGuild() {
		DIApi api = BukkitApplication.getDIApi();
		JDA jda = api.getCoreController().getDiscordApi();
		return jda.getGuildById(api.getCoreController().getBot().getServerid());
	}
}
