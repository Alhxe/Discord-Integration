package di.dicore;

import di.dicore.bungee.controller.ChannelMessageController;
import di.dicore.event.BotStatusBungeeEvent;
import di.internal.controller.ChannelController;
import di.internal.controller.CoreController;
import di.internal.controller.impl.ChannelControllerBungeeImpl;
import di.internal.controller.impl.CoreControllerBungeeImpl;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeApplication extends Plugin {

    /**
     * The internal controller of the core plugin.
     */
    private static CoreController internalController;

    /**
     * The ChannelController.
     */
    private static ChannelController channelController;

    @Override
    public void onEnable() {
        Plugin plugin = this.getProxy().getPluginManager().getPlugin("DICore");
        internalController = new CoreControllerBungeeImpl(plugin, this.getClass().getClassLoader());
        internalController.startBot();
        channelController = new ChannelControllerBungeeImpl(this);

        plugin.getProxy().getPluginManager().registerListener(plugin, new ChannelMessageController());

        BotStatusBungeeEvent.init(plugin);

        getLogger().info("Plugin started");
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
     * Get the internal channel controller of the core plugin.
     *
     * @return The internal channel controller of the core plugin.
     */
    public static ChannelController getChannelController() {
        return channelController;
    }

    /**
     * @return the internal controller of the core plugin.
     */
    public static CoreController getInternalController() {
        return internalController;
    }

}
