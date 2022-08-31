package di.dilogin.discord.event;

import java.time.Duration;
import java.util.Optional;

import org.bukkit.entity.Player;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.minecraft.ext.authme.AuthmeHook;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Class for handling discord login or registration events.
 */
public class UserReactionMessageEvent extends ListenerAdapter {

	/**
	 * Database user DAO.
	 */
	private final DIUserDao userDao = DILoginController.getDIUserDao();

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
		loginOpt.ifPresent(tmpMessage -> loginUser(event, tmpMessage));
	}

	/**
	 * In case of being present in a registration process, this is carried out.
	 * 
	 * @param event     Reaction event.
	 * @param tmpMessage Process message.
	 */
	private void registerUser(MessageReactionAddEvent event, TmpMessage tmpMessage) {
		Message message = tmpMessage.getMessage();
		Player player = tmpMessage.getPlayer();
		User user = tmpMessage.getUser();

		if (!event.getUser().equals(user))
			return;

		if (event.getMessageIdLong() != message.getIdLong())
			return;

		String password = CodeGenerator.getCode(8, api);
		player.sendMessage(
				LangManager.getString(user, player, "register_success").replace("%authme_password%", password));
		TmpCache.removeRegister(player.getName());
		message.editMessageEmbeds(getRegisterEmbed(user, player)).delay(Duration.ofSeconds(60)).flatMap(Message::delete)
				.queue();
		userDao.add(new DIUser(Optional.of(player), Optional.of(user)));

		if (DILoginController.isAuthmeEnabled()) {
			AuthmeHook.register(player, password);
		} else {
			if (!Util.isWhiteListed(user)) {
				player.sendMessage(LangManager.getString(player, "login_without_role_required"));
			} else {
				DILoginController.loginUser(player, user);
			}
		}

	}

	/**
	 * In case of being present in a login process, this is carried out.
	 * 
	 * @param event     Reaction event.
	 * @param tmpMessage Process message.
	 */
	private void loginUser(MessageReactionAddEvent event, TmpMessage tmpMessage) {
		Message message = tmpMessage.getMessage();
		Player player = tmpMessage.getPlayer();
		User user = tmpMessage.getUser();

		if (!event.getUser().equals(user))
			return;

		if (event.getMessageIdLong() != message.getIdLong())
			return;

		message.editMessageEmbeds(getLoginEmbed(user, player)).delay(Duration.ofSeconds(60)).flatMap(Message::delete).queue();
		DILoginController.loginUser(player, user);

	}

	/**
	 * @param user   Discord user.
	 * @param player Bukkit player.
	 * @return Registration completed message.
	 */
	private MessageEmbed getRegisterEmbed(User user, Player player) {
		return DILoginController.getEmbedBase().setTitle(LangManager.getString(user, player, "register_discord_title"))
				.setDescription(LangManager.getString(user, player, "register_discord_success")).build();
	}

	/**
	 * @param user   Discord user.
	 * @param player Bukkit player.
	 * @return Login completed message.
	 */
	private MessageEmbed getLoginEmbed(User user, Player player) {
		return DILoginController.getEmbedBase().setTitle(LangManager.getString(user, player, "login_discord_title"))
				.setDescription(LangManager.getString(user, player, "login_discord_success")).build();
	}

}
