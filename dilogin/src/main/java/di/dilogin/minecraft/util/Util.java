package di.dilogin.minecraft.util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import di.dilogin.controller.MainController;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import di.dicore.api.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.DIUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

/**
 * Util class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Util {

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
            Member member = Objects.requireNonNull(user.getJDA()
                            .getGuildById(BukkitApplication.getDIApi().getCoreController().getBot().getServerId()))
                    .retrieveMember(user, true).complete();
            return member.getRoles().contains(role);
        }
        return true;
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
            api.getInternalController().getLogger().log(Level.SEVERE, message);
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
            api.getInternalController().getLogger().info(
                    role.getName() + " role has been given to " + member.getUser().getAsTag() + ". Reason: " + reason);

        } catch (Exception e) {
            api.getInternalController().getLogger().log(Level.SEVERE,
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

    /**
     * Get a member from the discord server.
     *
     * @param player Bukkit player name.
     * @return Optional member.
     */
    private static Optional<Member> getMember(String player) {
        DIUserDao dao = MainController.getDILoginController().getDIUserDao();
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
        JDA jda = api.getCoreController().getDiscordApi().get();
        return jda.getGuildById(api.getCoreController().getBot().getServerId());
    }
}
