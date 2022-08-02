package di.dicore;

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
     * Runs when the plugin is being powered on.
     */
    @Override
    public void onEnable() {
        getLogger().info("Plugin started");
        Plugin plugin = getPlugin(getClass());
        internalController = new CoreController(plugin, this.getClassLoader());
        BotStatus.init();
    }

    /**
     * It is executed when the plugin is being shut down.
     */
    @Override
    public void onDisable() {
        if (internalController != null)
            internalController.getBot().getApi().shutdownNow();
        getLogger().info("Plugin disabled");
    }

    /**
     * @return the internal controller of the core plugin.
     */
    public static CoreController getInternalController() {
        return internalController;
    }

}
