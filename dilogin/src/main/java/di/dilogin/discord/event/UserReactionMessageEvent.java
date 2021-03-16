package di.dilogin.discord.event;

import java.time.Duration;
import java.util.Optional;

import org.bukkit.entity.Player;

import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqliteImpl;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
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
	private final DIUserDao userDao = new DIUserDaoSqliteImpl();

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
	 * @param event     Reaction event.
	 * @param tmpMssage Process message.
	 */
	private void registerUser(MessageReactionAddEvent event, TmpMessage tmpMessage) {
		Message message = tmpMessage.getMessage();
		Player player = tmpMessage.getPlayer();
		User user = tmpMessage.getUser();

		if (!event.getUser().equals(user))
			return;

		if (event.getMessageIdLong() != message.getIdLong())
			return;

		player.sendMessage(LangManager.getString(user, player, "register_success"));
		TmpCache.removeRegister(player.getName());
		message.editMessage(getRegisterEmbed(user, player)).delay(Duration.ofSeconds(60)).flatMap(Message::delete)
				.queue();
		userDao.add(new DIUser(player, user));
		DILoginController.loginUser(player);

	}

	/**
	 * In case of being present in a login process, this is carried out.
	 * 
	 * @param event     Reaction event.
	 * @param tmpMssage Process message.
	 */
	private void loginUser(MessageReactionAddEvent event, TmpMessage tmpMessage) {
		Message message = tmpMessage.getMessage();
		Player player = tmpMessage.getPlayer();
		User user = tmpMessage.getUser();

		if (!event.getUser().equals(user))
			return;

		if (event.getMessageIdLong() != message.getIdLong())
			return;

		message.editMessage(getLoginEmbed(user, player)).delay(Duration.ofSeconds(60)).flatMap(Message::delete).queue();
		DILoginController.loginUser(player);

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
