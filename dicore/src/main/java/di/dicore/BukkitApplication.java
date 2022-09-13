package di.dicore;

import di.dicore.event.BotStatusBukkitEvent;
import di.internal.controller.impl.CoreControllerBukkitImpl;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import di.internal.controller.CoreController;

/**
 * Main class of the plugin.
 */
public class BukkitApplication extends JavaPlugin {

    /**
     * The internal controller of the core plugin.
     */
    private static CoreController internalController;

    /**
     * The plugin instance.
     */
    private static Plugin plugin;

    /**
     * Runs when the plugin is being powered on.
     */
    @Override
    public void onEnable() {
        plugin = getPlugin(getClass());
        internalController = new CoreControllerBukkitImpl(plugin, this.getClassLoader(), isBungeeDetected());
        if (!isBungeeDetected()) {
            BotStatusBukkitEvent.init(plugin);
        }
        getLogger().info("Plugin started");
    }

    /**
     * @return The bukkit plugin.
     */
    public static Plugin getPlugin() {
        return plugin;
    }

    /**
     * It is executed when the plugin is being shut down.
     */
    @Override
    public void onDisable() {
        if (internalController != null)
            internalController.getBot().getApi().get().shutdownNow();

        getLogger().info("Plugin disabled");
    }

    /**
     * @return the internal controller of the core plugin.
     */
    public static CoreController getInternalController() {
        return internalController;
    }

    /**
     * @return true if BungeeCord setting is enabled.
     */
    private boolean isBungeeDetected() {
        return plugin.getServer().spigot().getConfig().getBoolean("settings.bungeecord");
    }
}
