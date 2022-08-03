package di.dibungee;

import net.md_5.bungee.api.plugin.Plugin;

/**
 * Main class of the plugin.
 */
public class BungeeApplication extends Plugin {

    @Override
    public void onEnable() {
        getLogger().info("Basic Bungee plugin enabled!");
    }
}