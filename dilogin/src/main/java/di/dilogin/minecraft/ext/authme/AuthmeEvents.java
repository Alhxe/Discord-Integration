package di.dilogin.minecraft.ext.authme;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import di.dilogin.controller.DILoginController;
import di.dilogin.dao.DIUserDao;
import fr.xephi.authme.events.UnregisterByAdminEvent;
import fr.xephi.authme.events.UnregisterByPlayerEvent;

/**
 * AuthMe related events.
 */
public class AuthmeEvents implements Listener {

    /**
     * User management.
     */
    private DIUserDao userDao = DILoginController.getDIUserDao();

    /**
     * Main unregister by admin event body.
     *
     * @param event It is the object that includes the event information.
     */
    @EventHandler
    void onUnregisterByAdminEvent(UnregisterByAdminEvent event) {
        unregister(event.getPlayerName());
    }

    /**
     * Main unregister by player event body.
     *
     * @param event It is the object that includes the event information.
     */
    @EventHandler
    void onUnregisterByPlayerEvent(UnregisterByPlayerEvent event) {
        Optional<Player> optPlayer = Optional.ofNullable(event.getPlayer());
        if (optPlayer.isPresent())
            unregister(optPlayer.get().getName());
    }

    /**
     * Unregister user from DILogin.
     */
    private void unregister(String playerName) {
        userDao.remove(playerName);
    }
}
