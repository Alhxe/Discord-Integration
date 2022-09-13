package di.dicore;

import di.dicore.event.BotStatusBungeeEvent;
import di.internal.controller.CoreController;
import di.internal.controller.impl.CoreControllerBungeeImpl;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeApplication extends Plugin {

    /**
     * The internal controller of the core plugin.
     */
    private static CoreController internalController;

    @Override
    public void onEnable() {
        Plugin plugin = this.getProxy().getPluginManager().getPlugin("DICore");
        internalController = new CoreControllerBungeeImpl(plugin, this.getClass().getClassLoader());

        BotStatusBungeeEvent.init(plugin);

        getLogger().info("Plugin started");
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

}
