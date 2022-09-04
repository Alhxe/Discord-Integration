package di.dilogin.minecraft.ext.luckperms;

import java.util.Optional;

import di.dilogin.controller.MainController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;
import di.dilogin.minecraft.bukkit.ext.luckperms.LuckPermsController;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Class for handling role add to member events.
 */
public class GuildMemberRoleEvent extends ListenerAdapter {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao dao = MainController.getDILoginController().getDIUserDao();

	/**
	 * Main event body.
	 * @param event It is the object that includes the event information.
	 */
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		Optional<DIUser> optDIUser = dao.get(event.getUser().getIdLong());
		if (!optDIUser.isPresent())
			return;

		DIUser user = optDIUser.get();

		for (Role role : event.getRoles()) {
			for (String group : LuckPermsController.getMinecraftRoleFromDiscordRole(role.getId())) {
					if (!LuckPermsController.isUserInGroup(user.getPlayerName(), group))
						LuckPermsController.addGroup(user.getPlayerName(), group,
								"Get " + role + " role in discord.");
			}
		}
	}

	/**
	 * Main event body.
	 * @param event It is the object that includes the event information.
	 */
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		Optional<DIUser> optDIUser = dao.get(event.getUser().getIdLong());
		if (!optDIUser.isPresent())
			return;

		DIUser user = optDIUser.get();

		for (Role role : event.getRoles()) {
			for (String group : LuckPermsController.getMinecraftRoleFromDiscordRole(role.getId())) {
					if (LuckPermsController.isUserInGroup(user.getPlayerName(), group))
						LuckPermsController.removeGroup(user.getPlayerName(), group,
								"Removed " + role + " role in discord.");
			}
		}
	}
}
