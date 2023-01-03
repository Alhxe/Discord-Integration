package di.internal.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.concurrent.CompletableFuture;

/**
 * This class is used to get the first user to log into the server, in order to receive data from BungeeCord.
 */
public class UserLoginForBungeeCallBukkitEvent implements Listener {

    /**
     * The first user to log into the server.
     */
    private static CompletableFuture<Player> future = new CompletableFuture<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        future.complete(event.getPlayer());
    }

    /**
     * Get the future first user to log into the server.
     *
     * @return Player.
     */
    public CompletableFuture<Player> getFirstPlayer() {
        return future;
    }
}
