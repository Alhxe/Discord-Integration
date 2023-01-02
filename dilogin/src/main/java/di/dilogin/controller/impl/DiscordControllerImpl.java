package di.dilogin.controller.impl;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import di.dicore.api.DIApi;
import di.dilogin.controller.DiscordController;
import di.dilogin.controller.MainController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

/**
 * Controller used to manage Discord.
 */
public class DiscordControllerImpl implements DiscordController {

	@Override
	public boolean userHasRole(String roleid, String player) {
		Optional<Member> optMember = getMember(player);
		if (!optMember.isPresent())
			return false;

		Member member = optMember.get();
		List<Role> roles = member.getRoles();

		return roles.stream().anyMatch(role -> role.getId().equals(roleid));
	}

	@Override
	public boolean serverHasRole(String roleid) {
		DIApi api = MainController.getDIApi();
		Guild guild = getGuild();
		Role role = guild.getRoleById(roleid);
		if (role == null) {
			String message = "Could not find ROL with id: " + roleid + ". Check the plugin settings to avoid problems.";
			api.getInternalController().getLogger().log(Level.SEVERE, message);
			return false;
		}
		return true;
	}

	@Override
	public void giveRole(String roleid, String player, String reason) {
		DIApi api = MainController.getDIApi();

		Optional<Member> optMember = getMember(player);
		if (!optMember.isPresent())
			return;

		Member member = optMember.get();
		Guild guild = getGuild();
		Role role = guild.getRoleById(roleid);

		try {
			guild.addRoleToMember(member, role).queue();
			api.getInternalController().getLogger().info(
					role.getName() + " role has been given to " + member.getUser().getAsTag() + ". Reason: " + reason);

		} catch (Exception e) {
			api.getInternalController().getLogger().log(Level.SEVERE,
					" Could not give " + role.getName() + " role to " + member.getUser().getAsTag()
							+ ". Reason:  Can't modify a role with higher or equal highest role than yourself");
		}
	}

	@Override
	public void removeRole(String roleid, String player, String reason) {
		DIApi api = MainController.getDIApi();

		Optional<Member> optMember = getMember(player);
		if (!optMember.isPresent())
			return;

		Member member = optMember.get();
		Guild guild = getGuild();
		Role role = guild.getRoleById(roleid);

		try {
			assert role != null;
			guild.removeRoleFromMember(member, role).queue();
			api.getInternalController().getLogger().info(role.getName() + " role has been removed from "
					+ member.getUser().getAsTag() + ". Reason: " + reason);

		} catch (Exception e) {
			api.getInternalController().getLogger().log(Level.SEVERE,
					" Could not remove " + role.getName() + " role from " + member.getUser().getAsTag()
							+ ". Reason:  Can't modify a role with higher or equal highest role than yourself");
		}
	}

	@Override
	public boolean isWhiteListed(String player) {
		Optional<Role> optRole = requiredRole();
		if (optRole.isPresent()) {
			Role role = optRole.get();
			Optional<Member> member = getMember(player);
			if (member.isPresent()) {
				return member.get().getRoles().contains(role);
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isWhiteListed(String player, User user) {
		Optional<Role> optRole = requiredRole();
		if (optRole.isPresent()) {
			Role role = optRole.get();
			Member member = getGuild().getMember(user);
			return member.getRoles().contains(role);
		}
		return true;	
	}

	/**
	 * Check for required role to whitelist on config file.
	 *
	 * @return optional role.
	 */
	private Optional<Role> requiredRole() {
		DIApi api = MainController.getDIApi();
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
	 * Get Discord Member from Minecraft name.
	 * 
	 * @param player Minecraft name.
	 * @return The member if is present on JDA.
	 */
	private Optional<Member> getMember(String player) {
		DIUserDao dao = MainController.getDILoginController().getDIUserDao();
		Guild guild = getGuild();

		Optional<DIUser> DIUserOpt = dao.get(player);
		if (!DIUserOpt.isPresent())
			return Optional.empty();

		List<Member> memberList = guild
				.findMembers(m -> m.getId().equals(DIUserOpt.get().getPlayerDiscord().get().getId())).get();

		if (!memberList.isEmpty())
			return Optional.of(memberList.get(0));

		return Optional.empty();
	}

	/**
	 * 
	 * @return Discord main guild.
	 */
	private Guild getGuild() {
		DIApi api = MainController.getDIApi();
		JDA jda = api.getCoreController().getDiscordApi().get();
		return jda.getGuildById(api.getCoreController().getBot().getServerId());
	}
}
