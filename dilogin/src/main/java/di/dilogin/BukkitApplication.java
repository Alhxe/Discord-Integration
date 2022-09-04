package di.dilogin;

import java.util.Objects;
import java.util.logging.Level;

import di.dicore.api.impl.DIApiBukkitImpl;
import di.dilogin.controller.MainController;
import di.dilogin.controller.impl.DILoginControllerBukkit;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import di.dicore.api.DIApi;
import di.dilogin.controller.DBController;
import di.dilogin.controller.DILoginController;
import di.dilogin.discord.command.DiscordRegisterCommand;
import di.dilogin.minecraft.ext.luckperms.GuildMemberRoleEvent;
import di.dilogin.discord.event.UserReactionMessageEvent;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.bukkit.command.ForceLoginCommand;
import di.dilogin.minecraft.bukkit.command.RegisterCommand;
import di.dilogin.minecraft.bukkit.command.UnregisterCommand;
import di.dilogin.minecraft.bukkit.event.UserBlockEvents;
import di.dilogin.minecraft.bukkit.event.UserLeaveEvent;
import di.dilogin.minecraft.bukkit.event.UserLoginEventImpl;
import di.dilogin.minecraft.bukkit.event.UserPreLoginEvent;
import di.dilogin.minecraft.bukkit.event.UserTeleportEvents;
import di.dilogin.minecraft.bukkit.ext.authme.AuthmeEvents;
import di.dilogin.minecraft.bukkit.ext.authme.UserLoginEventAuthmeImpl;
import di.dilogin.minecraft.bukkit.ext.luckperms.LuckPermsEvents;
import di.dilogin.minecraft.bukkit.ext.nlogin.UnregisterNLoginEvents;
import di.dilogin.minecraft.bukkit.ext.nlogin.UserLoginEventNLoginImpl;
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
        getLogger().info("Plugin started");
        plugin = getPlugin(getClass());

        connectWithCoreApi();
        DBController.getConnect();
        MainController.setDILoginController(new DILoginControllerBukkit());
        MainController.setBukkit(true);

        initCommands();
        initEvents();
        initDiscordEvents();
        initDiscordCommands();
    }

    @Override
    public void onDisable() {
        TmpCache.clearAll();
    }

    /**
     * @return Discord Integration Api.
     */
    public static DIApi getDIApi() {
        return api;
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
    private void initCommands() {
        initUniqueCommand("diregister", new RegisterCommand());
        initUniqueCommand("forcelogin", new ForceLoginCommand());
        initUniqueCommand("unregister", new UnregisterCommand());
    }

    /**
     * Add to each command that the server must respond in case it does not have
     * permissions.
     *
     * @param command  Bukkit command.
     * @param executor CommandExecutor.
     */
    private void initUniqueCommand(String command, CommandExecutor executor) {
        Objects.requireNonNull(getCommand(command)).setExecutor(executor);
        Objects.requireNonNull(getCommand(command)).setPermissionMessage(api.getCoreController().getLangManager().getString("no_permission"));
    }


    /**
     * Connect with DIApi.
     */
    private void connectWithCoreApi() {
        if (Objects.requireNonNull(plugin.getServer().getPluginManager().getPlugin("DICore")).isEnabled()) {
            try {
                api = new DIApiBukkitImpl(plugin, this.getClassLoader(), true, true);
            } catch (NoApiException e) {
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().log(Level.SEVERE,
                    "Failed to connect to DICore plugin. Check if it has been turned on correctly.");
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }


    /**
     * Init Bukkit events.
     */
    private void initEvents() {
        if (MainController.getDILoginController().isLuckPermsEnabled()) {
            getPlugin().getLogger().info("LuckPerms detected, starting plugin compatibility.");
            new LuckPermsEvents();
        }
        if (MainController.getDILoginController().isAuthmeEnabled()) {
            initAuthmeEvents();
        } else if (MainController.getDILoginController().isNLoginEnabled()) {
            initNLoginEvents();
        } else {
            initDILoginEvents();
        }
        getServer().getPluginManager().registerEvents(new UserLeaveEvent(), plugin);
        getServer().getPluginManager().registerEvents(new UserTeleportEvents(), plugin);
        getServer().getPluginManager().registerEvents(new UserPreLoginEvent(), plugin);
    }

    /**
     * Records Discord events.
     */
    private void initDiscordEvents() {
        api.registerDiscordEvent(new UserReactionMessageEvent());
        if (MainController.getDILoginController().isSyncroRolEnabled()) {
            api.registerDiscordEvent(new GuildMemberRoleEvent());
        }
    }

    /**
     * Init discord commands.
     */
    private void initDiscordCommands() {
        api.registerDiscordCommand(new DiscordRegisterCommand());
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

	private void initLuckPermsEvents(){
		getPlugin().getLogger().info("LuckPerms detected, starting plugin compatibility.");
		new LuckPermsEvents();
		getServer().getPluginManager().registerEvents(new LuckPermsLoginEvent(), plugin);
	}

    /**
     * Init events with compatibility with Authme.
     */
    private void initAuthmeEvents() {
        getPlugin().getLogger().info("Authme detected, starting plugin compatibility.");
        getServer().getPluginManager().registerEvents(new UserLoginEventAuthmeImpl(), plugin);
        getServer().getPluginManager().registerEvents(new AuthmeEvents(), plugin);
    }

    /**
     * Init the plugin's default events.
     */
    private void initDILoginEvents() {
        getServer().getPluginManager().registerEvents(new UserLoginEventImpl(), plugin);
        getServer().getPluginManager().registerEvents(new UserBlockEvents(), plugin);
    }

}
