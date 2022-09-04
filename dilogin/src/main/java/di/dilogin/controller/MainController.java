package di.dilogin.controller;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class used to save instantiation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MainController {

    /**
     * Main DILoginController.
     */
    private static DILoginController controller;

    /**
     * Information about whether the plugin is running in Bukkit.
     */
    private static boolean isBukkit = false;

    /**
     * Information about whether the plugin is running in BungeeCord.
     */
    private static boolean isBungee = false;

    /**
     * @return Main DILoginController.
     */
    public static DILoginController getDILoginController() {
        return controller;
    }

    /**
     * @param controller Main DILoginController.
     */
    public static void setDILoginController(DILoginController controller) {
        MainController.controller = controller;
    }

    /**
     * @return Information about whether the plugin is running in Bukkit.
     */
    public static boolean isBukkit() {
        return isBukkit;
    }

    /**
     * @param isBukkit Information about whether the plugin is running in Bukkit.
     */
    public static void setBukkit(boolean isBukkit) {
        MainController.isBukkit = isBukkit;
    }

    /**
     * @return Information about whether the plugin is running in BungeeCord.
     */
    public static boolean isBungee() {
        return isBungee;
    }

    /**
     * @param isBungee Information about whether the plugin is running in BungeeCord.
     */
    public static void setBungee(boolean isBungee) {
        MainController.isBungee = isBungee;
    }
}
