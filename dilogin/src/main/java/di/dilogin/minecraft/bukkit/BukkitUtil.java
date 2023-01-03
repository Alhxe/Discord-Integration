package di.dilogin.minecraft.bukkit;

import java.util.Optional;

import org.bukkit.entity.Player;

import di.dilogin.BukkitApplication;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Bukkit Util class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BukkitUtil {

    /**
     * @param server Active server.
     * @return The number of the version of server.
     */
    public static int getServerVersion(String version) {
        if (version.contains("1.17"))
            return 17;
        if (version.contains("1.16"))
            return 16;
        if (version.contains("1.15"))
            return 15;
        if (version.contains("1.14"))
            return 14;
        if (version.contains("1.13"))
            return 13;
        if (version.contains("1.12"))
            return 12;
        if (version.contains("1.11"))
            return 11;
        if (version.contains("1.10"))
            return 10;
        if (version.contains("1.9"))
            return 9;
        if (version.contains("1.8"))
            return 8;
        if (version.contains("1.7"))
            return 7;
        return -1;
    }

    /**
     * @param playerName Bukkit player's name.
     * @return Possible player based on their name.
     */
    public static Optional<Player> getUserPlayerByName(String playerName) {
        return Optional.ofNullable(BukkitApplication.getPlugin().getServer().getPlayer(playerName));
    }
}
