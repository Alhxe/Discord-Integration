package di.dilogin;

import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import di.dicore.api.DIApi;
import di.dicore.api.impl.DIApiBukkitImpl;
import di.dilogin.controller.DBController;
import di.dilogin.controller.MainController;
import di.dilogin.controller.impl.DILoginControllerBukkitImpl;
import di.dilogin.controller.impl.DiscordControllerImpl;
import di.dilogin.discord.command.DiscordRegisterBukkitCommand;
import di.dilogin.discord.event.UserReactionMessageBukkitEvent;
import di.dilogin.minecraft.bukkit.command.RegisterOtherBukkitCommand;
import di.dilogin.minecraft.bukkit.command.ForceLoginBukkitCommand;
import di.dilogin.minecraft.bukkit.command.RegisterBukkitCommand;
import di.dilogin.minecraft.bukkit.command.UnregisterBukkitCommand;
import di.dilogin.minecraft.bukkit.event.UserBlockEvents;
import di.dilogin.minecraft.bukkit.event.UserLeaveEvent;
import di.dilogin.minecraft.bukkit.event.UserPreLoginEvent;
import di.dilogin.minecraft.bukkit.event.UserTeleportEvents;
import di.dilogin.minecraft.bukkit.event.impl.UserLoginExternEventImpl;
import di.dilogin.minecraft.bukkit.event.impl.UserLoginInternEventImpl;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.ext.authme.AuthmeEvents;
import di.dilogin.minecraft.ext.authme.UserLoginEventAuthmeImpl;
import di.dilogin.minecraft.ext.luckperms.GuildMemberRoleEvent;
import di.dilogin.minecraft.ext.luckperms.LuckPermsEvents;
import di.dilogin.minecraft.ext.luckperms.LuckPermsLoginBukkitEvent;
import di.dilogin.minecraft.ext.nlogin.UnregisterNLoginEvents;
import di.dilogin.minecraft.ext.nlogin.UserLoginEventNLoginImpl;
import di.internal.exception.NoApiException;

/**
 * Main DILogin class for Bukkit.
 */
public class BukkitApplication extends JavaPlugin {

	/**
	 * Discord Integration Core Api.
	 */
	private static DIApi api;

	/**
	 * Main DILogin plugin.
	 */
	private static Plugin plugin;

	@Override
	public void onEnable() {
		plugin = getPlugin(getClass());

		connectWithCoreApi();

		MainController.setDIApi(api);
		MainController.setDILoginController(new DILoginControllerBukkitImpl());
		MainController.setBukkit(true);

		if (!api.isBungeeDetected()) {
			// If api is in own server.
			MainController.setDiscordController(new DiscordControllerImpl());
			DBController.getConnect();
			initInternCommands();
			initInternEvents();
			initDiscordEvents();
			initDiscordCommands();
		} else {
			// If api is in proxy.
			initExtEvents();
			api.getInternalController().initConnectionWithBungee();
		}
		getLogger().info("Plugin started");
	}

	@Override
	public void onDisable() {
		TmpCache.clearAll();
	}

	/**
	 * @return Get Main Bukkit plugin.
	 */
	public static Plugin getPlugin() {
		return plugin;
	}

	/**
	 * Add the commands to bukkit.
	 */
	private void initInternCommands() {
		initUniqueCommand("diregisterother", new RegisterOtherBukkitCommand());
		initUniqueCommand("diregister", new RegisterBukkitCommand());
		initUniqueCommand("forcelogin", new ForceLoginBukkitCommand());
		initUniqueCommand("unregister", new UnregisterBukkitCommand());
	}

	/**
	 * @@ -103,10 +114,28 @@ private void initInternCommands() {
	 * @param command  Bukkit command.
	 * @param executor CommandExecutor.
	 */
	private void initUniqueCommand(String command, CommandExecutor executor) {
		Objects.requireNonNull(getCommand(command)).setExecutor(executor);
		Objects.requireNonNull(getCommand(command))
				.setPermissionMessage(api.getCoreController().getLangManager().getString("no_permission"));
	}

//	/**
//	 * Add the commands to bukkit.
//	 */
//	private void initInternCommands() {
//		registerCommand("diregister", CommandAliasController.getAlias("register_command"),
//				new RegisterBukkitCommand());
//		registerCommand("forcelogin", CommandAliasController.getAlias("forcelogin_command"),
//				new ForceLoginBukkitCommand());
//		registerCommand("unregister", CommandAliasController.getAlias("unregister_command"),
//				new UnregisterBukkitCommand());
//	}
//
//	/**
//	 * Add to each command that the server must respond in case it does not have
//	 * permissions.
//	 *
//	 * @param command  Bukkit command.
//	 * @param executor CommandExecutor.
//	 */
//	public void registerCommand(String command, String alias, CommandExecutor executor) {
//		try {
//			List<String> aliases = new ArrayList<>();
//			aliases.add(alias);
//			PluginCommand plc = null;
//			Class<?> cl = PluginCommand.class;
//			Constructor<?> cons = null;
//			cons = cl.getDeclaredConstructor(String.class, Plugin.class);
//			cons.setAccessible(true);
//			plc = (PluginCommand) cons.newInstance(command, this);
//			plc.setAliases(aliases);
//			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
//
//			bukkitCommandMap.setAccessible(true);
//			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
//			commandMap.register(command, plc);
//			plc.register(commandMap);
//			plc.setPermissionMessage(api.getCoreController().getLangManager().getString("no_permission"));
//			plc.setExecutor(executor);
//		} catch (Exception e) {
//			getServer().getLogger().log(Level.SEVERE, "registerCommand", e);
//		}
//	}

	/**
	 * Connect with DIApi.
	 */
	private void connectWithCoreApi() {
		try {
			if (Objects.requireNonNull(plugin.getServer().getPluginManager().getPlugin("DICore")).isEnabled()) {
				api = new DIApiBukkitImpl(plugin, this.getClassLoader(), true, true);
			} else {
				plugin.getLogger().log(Level.SEVERE,
						"Failed to connect to DICore plugin. Check if it has been turned on correctly.");
				plugin.getPluginLoader().disablePlugin(plugin);
			}
		} catch (NoApiException e) {
			getLogger().log(Level.SEVERE, "BukkitApplication - connectWithCoreApi", e);
		}
	}

	/**
	 * Init Bukkit events.
	 */
	private void initInternEvents() {
		if (MainController.getDILoginController().isLuckPermsEnabled()) {
			initLuckPermsEvents();
		}
		if (MainController.getDILoginController().isAuthmeEnabled()) {
			initAuthmeEvents();
		} else if (MainController.getDILoginController().isNLoginEnabled()) {
			initNLoginEvents();
		} else {
			getServer().getPluginManager().registerEvents(new UserLoginInternEventImpl(), plugin);
		}
		getServer().getPluginManager().registerEvents(new UserBlockEvents(), plugin);
		getServer().getPluginManager().registerEvents(new UserLeaveEvent(), plugin);
		getServer().getPluginManager().registerEvents(new UserTeleportEvents(), plugin);
		getServer().getPluginManager().registerEvents(new UserPreLoginEvent(), plugin);
	}

	/**
	 * Init Bukkit events when api is in Proxy.
	 */
	private void initExtEvents() {
		getServer().getPluginManager().registerEvents(new UserBlockEvents(), plugin);
		getServer().getPluginManager().registerEvents(new UserLoginExternEventImpl(plugin), plugin);
	}

	/**
	 * Records Discord events.
	 */
	private void initDiscordEvents() {
		api.registerDiscordEvent(new UserReactionMessageBukkitEvent());
		if (MainController.getDILoginController().isSyncroRolEnabled()) {
			api.registerDiscordEvent(new GuildMemberRoleEvent());
		}
	}

	/**
	 * Init discord commands.
	 */
	private void initDiscordCommands() {
		api.registerDiscordCommand(new DiscordRegisterBukkitCommand());
	}

	/**
	 * Init events with compatibility with nLogin.
	 */
	private void initNLoginEvents() {
		getPlugin().getLogger().info("nLogin detected, starting plugin compatibility.");
		try {
			Class.forName("com.nickuc.login.api.nLoginAPIHolder");
			getServer().getPluginManager().registerEvents(new UnregisterNLoginEvents(), plugin);
			getServer().getPluginManager().registerEvents(new UserLoginEventNLoginImpl(), plugin);
		} catch (ClassNotFoundException e) {
			getLogger().severe("You are using the old version of nLogin.");
			getLogger().severe("Please upgrade to version 10.");
		}
	}

	/**
	 * Init events with compatibility with LuckPerms.
	 */
	private void initLuckPermsEvents() {
		if (MainController.getDILoginController().isSyncroRolEnabled()) {
			getPlugin().getLogger().info("LuckPerms detected, starting plugin compatibility.");
			new LuckPermsEvents(getPlugin());
			getServer().getPluginManager().registerEvents(new LuckPermsLoginBukkitEvent(), plugin);
		}
	}

	/**
	 * Init events with compatibility with Authme.
	 */
	private void initAuthmeEvents() {
		getPlugin().getLogger().info("Authme detected, starting plugin compatibility.");
		getServer().getPluginManager().registerEvents(new UserLoginEventAuthmeImpl(), plugin);
		getServer().getPluginManager().registerEvents(new AuthmeEvents(), plugin);
	}

}
