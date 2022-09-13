package di.dilogin.controller.impl;

import di.dicore.api.DIApi;
import di.dilogin.BungeeApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqlImpl;
import di.dilogin.entity.DIUser;
import di.dilogin.minecraft.bungee.BungeeUtil;
import di.internal.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.time.Instant;
import java.util.Optional;

/**
 * DILogin plugin control.
 */
public class DILoginControllerBungeeImpl implements DILoginController {

    /**
     * Starts the implementation of the class that gets data from the users.
     */
    private static final DIUserDao userDao = new DIUserDaoSqlImpl();

    /**
     * Get the main plugin api.
     */
    private static final DIApi api = BungeeApplication.getDIApi();

    @Override
    public DIUserDao getDIUserDao() {
        return userDao;
    }

    @Override
    public EmbedBuilder getEmbedBase() {
        DIApi api = BungeeApplication.getDIApi();
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(
                Util.hex2Rgb(api.getInternalController().getConfigManager().getString("discord_embed_color")));
        if (api.getInternalController().getConfigManager().getBoolean("discord_embed_server_image")) {
            Optional<Guild> optGuild = Optional.ofNullable(api.getCoreController().getDiscordApi()
                    .get().getGuildById(api.getCoreController().getConfigManager().getLong("discord_server_id")));
            if (optGuild.isPresent()) {
                String url = optGuild.get().getIconUrl();
                if (url != null)
                    embedBuilder.setThumbnail(url);
            }
        }
        if (api.getInternalController().getConfigManager().getBoolean("discord_embed_timestamp"))
            embedBuilder.setTimestamp(Instant.now());
        return embedBuilder;
    }

    @Override
    public boolean isSessionEnabled() {
        return BungeeApplication.getDIApi().getInternalController().getConfigManager().getBoolean("sessions");
    }

    @Override
    public boolean isSyncroRolEnabled() {
        return BungeeApplication.getDIApi().getInternalController().getConfigManager().getBoolean("syncro_rol_enable");
    }

    @Override
    public boolean isSyncronizeOptionEnabled() {
        return BungeeApplication.getDIApi().getInternalController().getConfigManager().getBoolean("syncro_enable");
    }

    @Override
    public boolean isAuthmeEnabled() {
        return BungeeApplication.getPlugin().getProxy().getPluginManager().getPlugin("AuthMe") != null;
    }

    @Override
    public boolean isNLoginEnabled() {
        return BungeeApplication.getPlugin().getProxy().getPluginManager().getPlugin("nLogin") != null;
    }

    @Override
    public boolean isLuckPermsEnabled() {
        return BungeeApplication.getPlugin().getProxy().getPluginManager().getPlugin("LuckPerms") != null
                && api.getInternalController().getConfigManager().getBoolean("syncro_rol_enable");
    }

    @Override
    public void loginUser(String playerName, User user) {

    }

    @Override
    public void kickPlayer(String playerName, String message) {
        Optional<ProxiedPlayer> optionalPlayer = BungeeUtil.getProxiedPlayer(playerName);

        if (!optionalPlayer.isPresent())
            return;

        optionalPlayer.get().disconnect(new TextComponent(message));
    }

    /**
     * Syncro player's name.
     *
     * @param player Minecraft player.
     */
    private void syncUserName(ProxiedPlayer player, User user) {
        Optional<DIUser> optDIUser = userDao.get(player.getName());

        if (!optDIUser.isPresent())
            return;

        DIApi api = BungeeApplication.getDIApi();
        JDA jda = BungeeApplication.getDIApi().getCoreController().getDiscordApi().get();
        Guild guild = api.getCoreController().getGuild().get();

        Member member = guild.retrieveMember(user, true).complete();
        Member bot = guild.retrieveMember(jda.getSelfUser(), true).complete();

        if (bot.canInteract(member)) {
            member.modifyNickname(player.getName()).queue();
        } else {
            api.getInternalController().getLogger()
                    .info("Cannot change the nickname of " + player.getName() + ". Insufficient permissions.");
        }
    }
}
