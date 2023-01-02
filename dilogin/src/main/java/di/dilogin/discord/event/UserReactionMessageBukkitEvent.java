package di.dilogin.discord.event;

import java.time.Duration;
import java.util.Optional;

import org.bukkit.entity.Player;

import di.dicore.api.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.LangManager;
import di.dilogin.controller.MainController;
import di.dilogin.dao.DIUserDao;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.bukkit.ext.authme.AuthmeHook;
import di.dilogin.minecraft.cache.TmpCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Class for handling discord login or registration events.
 */
public class UserReactionMessageBukkitEvent extends ListenerAdapter {

    /**
     * Database user DAO.
     */
    private final DIUserDao userDao = MainController.getDILoginController().getDIUserDao();

    /**
     * Main api.
     */
    private final DIApi api = BukkitApplication.getDIApi();

    /**
     * Main event body.
     *
     * @param event It is the object that includes the event information.
     */
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {

        if (event.getUser().isBot())
            return;

        Optional<TmpMessage> registerOpt = TmpCache.getRegisterMessage(event.getMessageIdLong());
        if (registerOpt.isPresent()) {
            registerUser(event, registerOpt.get());
            return;
        }

        Optional<TmpMessage> loginOpt = TmpCache.getLoginMessage(event.getMessageIdLong());
        if (loginOpt.isPresent())
            loginUser(event, loginOpt.get());
    }

    /**
     * In case of being present in a registration process, this is carried out.
     *
     * @param event      Reaction event.
     * @param tmpMessage Process message.
     */
    private void registerUser(MessageReactionAddEvent event, TmpMessage tmpMessage) {
        Message message = tmpMessage.getMessage();
        Player player = BukkitApplication.getPlugin().getServer().getPlayer(tmpMessage.getPlayer());
        User user = tmpMessage.getUser();

        if (!event.getUser().equals(user))
            return;

        if (event.getMessageIdLong() != message.getIdLong())
            return;

        String password = CodeGenerator.getCode(8, api);
        player.sendMessage(
                LangManager.getString(user, player.getName(), "register_success").replace("%authme_password%", password));
        TmpCache.removeRegister(player.getName());
        message.editMessageEmbeds(getRegisterEmbed(user, player)).delay(Duration.ofSeconds(60)).flatMap(Message::delete)
                .queue();
        userDao.add(new DIUser(player.getName(), Optional.of(user)));

        if (MainController.getDILoginController().isAuthmeEnabled()) {
            AuthmeHook.register(player, password);
        } else {
            if (!MainController.getDiscordController().isWhiteListed(player.getName())) {
                player.sendMessage(LangManager.getString(player.getName(), "login_without_role_required"));
            } else {
                MainController.getDILoginController().loginUser(player.getName(), user);
            }
        }

    }

    /**
     * In case of being present in a login process, this is carried out.
     *
     * @param event      Reaction event.
     * @param tmpMessage Process message.
     */
    private void loginUser(MessageReactionAddEvent event, TmpMessage tmpMessage) {
        Message message = tmpMessage.getMessage();
        Player player = BukkitApplication.getPlugin().getServer().getPlayer(tmpMessage.getPlayer());
        User user = tmpMessage.getUser();

        if (!event.getUser().equals(user))
            return;

        if (event.getMessageIdLong() != message.getIdLong())
            return;

        message.editMessageEmbeds(getLoginEmbed(user, player)).delay(Duration.ofSeconds(60)).flatMap(Message::delete).queue();
        MainController.getDILoginController().loginUser(player.getName(), user);

    }

    /**
     * @param user   Discord user.
     * @param player Bukkit player.
     * @return Registration completed message.
     */
    private MessageEmbed getRegisterEmbed(User user, Player player) {
        return MainController.getDILoginController().getEmbedBase().setTitle(LangManager.getString(user, player.getName(), "register_discord_title"))
                .setDescription(LangManager.getString(user, player.getName(), "register_discord_success")).build();
    }

    /**
     * @param user   Discord user.
     * @param player Bukkit player.
     * @return Login completed message.
     */
    private MessageEmbed getLoginEmbed(User user, Player player) {
        return MainController.getDILoginController().getEmbedBase().setTitle(LangManager.getString(user, player.getName(), "login_discord_title"))
                .setDescription(LangManager.getString(user, player.getName(), "login_discord_success")).build();
    }

}
