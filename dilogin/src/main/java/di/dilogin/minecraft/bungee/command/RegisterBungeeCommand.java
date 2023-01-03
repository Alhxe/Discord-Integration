package di.dilogin.minecraft.bungee.command;

import java.util.concurrent.Executors;

import di.dicore.api.DIApi;
import di.dilogin.controller.MainController;
import di.dilogin.controller.file.CommandAliasController;
import di.dilogin.controller.file.LangController;
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

/**
 * Command to register as a user.
 */
public class RegisterBungeeCommand extends Command {

	/**
	 * Main api.
	 */
	DIApi api = MainController.getDIApi();

	public RegisterBungeeCommand() {
		super(CommandAliasController.getAlias("register_command"), "", "diregister");
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
						LangController.getString(p.getName(), "register_request")
								.replace("%register_command%", command));
				tc.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, command));
				tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new Text(LangController.getString(p.getName(), "register_request_copy"))));
				p.sendMessage(tc);
			

			long seconds = api.getInternalController().getConfigManager()
					.getLong("register_time_until_kick") * 1000;

			Executors.newCachedThreadPool().submit(() -> {
				Thread.sleep(seconds);
				// In case the user has not finished completing the registration.
				if (TmpCache.containsRegister(p.getName())) {
					String message = LangController.getString(p.getName(), "register_kick_time");
					MainController.getDILoginController().kickPlayer(p.getName(), message);
				}
				return null;
			});
		}
		;
	}

}
