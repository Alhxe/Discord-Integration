package di.dilogin.minecraft.bungee.command;

import java.util.concurrent.Executors;

import di.dicore.api.DIApi;
import di.dilogin.BungeeApplication;
import di.dilogin.controller.LangManager;
import di.dilogin.controller.MainController;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class RegisterBungeeCommand extends Command {

	/**
	 * Main api.
	 */
	DIApi api = BungeeApplication.getDIApi();

	public RegisterBungeeCommand() {
		super("Register", "", "diregister");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if ((sender instanceof ProxiedPlayer)) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			String code = CodeGenerator
					.getCode(api.getInternalController().getConfigManager().getInt("register_code_length"), api);
			String command = api.getCoreController().getBot().getPrefix()
					+ api.getInternalController().getConfigManager().getString("register_command") + " " + code;
			TmpCache.addRegister(sender.getName(), new TmpMessage(p.getName(), null, null, code));

				TextComponent tc = new TextComponent(
						LangManager.getString(p.getName(), "register_request")
								.replace("%register_command%", command));
				tc.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, command));
				tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new Text(LangManager.getString(p.getName(), "register_request_copy"))));
				p.sendMessage(tc);
			

			long seconds = BungeeApplication.getDIApi().getInternalController().getConfigManager()
					.getLong("register_time_until_kick") * 1000;

			Executors.newCachedThreadPool().submit(() -> {
				Thread.sleep(seconds);
				// In case the user has not finished completing the registration.
				if (TmpCache.containsRegister(p.getName())) {
					String message = LangManager.getString(p.getName(), "register_kick_time");
					MainController.getDILoginController().kickPlayer(p.getName(), message);
				}
				return null;
			});
		}
		;
	}

}
