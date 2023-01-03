package di.dilogin.minecraft.bukkit.ext.luckperms;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import di.dilogin.entity.DIUser;
import di.dilogin.minecraft.bukkit.BukkitUtil;
import di.dilogin.minecraft.bukkit.event.custom.DILoginEvent;

/**
 * Class to synchronize LuckPerms roles.
 */
public class LuckPermsLoginEvent implements Listener {

    @EventHandler
    public void onLogin(DILoginEvent event) {
        DIUser user = event.getUser();
        Optional<Player> playerOptional = BukkitUtil.getUserPlayerByName(user.getPlayerName());
        if(!playerOptional.isPresent())
            return;

        LuckPermsController.syncUserRole(playerOptional.get());
    }

}
