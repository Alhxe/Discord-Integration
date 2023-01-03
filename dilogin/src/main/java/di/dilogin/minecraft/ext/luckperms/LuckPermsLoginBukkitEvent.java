package di.dilogin.minecraft.ext.luckperms;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import di.dilogin.entity.DIUser;
import di.dilogin.minecraft.bukkit.BukkitUtil;
import di.dilogin.minecraft.bukkit.event.custom.DILoginEvent;

/**
 * Class to synchronize LuckPerms roles in Bukkit.
 */
public class LuckPermsLoginBukkitEvent implements Listener {

    @EventHandler
    public void onLogin(DILoginEvent event) {
        DIUser user = event.getUser();
        Optional<Player> playerOptional = BukkitUtil.getUserPlayerByName(user.getPlayerName());
        if(!playerOptional.isPresent())
            return;

        LuckPermsController.syncUserRole(playerOptional.get().getName());
    }

}
