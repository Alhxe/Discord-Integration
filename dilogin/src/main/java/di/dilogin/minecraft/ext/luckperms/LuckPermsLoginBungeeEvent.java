package di.dilogin.minecraft.ext.luckperms;

import java.util.Optional;

import di.dilogin.entity.DIUser;
import di.dilogin.minecraft.bukkit.event.custom.DILoginBungeeEvent;
import di.dilogin.minecraft.bungee.BungeeUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Class to synchronize LuckPerms roles in Bungee.
 */
public class LuckPermsLoginBungeeEvent implements Listener {

	@EventHandler
    public void onLogin(DILoginBungeeEvent event) {
        DIUser user = event.getUser();
        Optional<ProxiedPlayer> playerOptional = BungeeUtil.getProxiedPlayer(user.getPlayerName());
        
        if(!playerOptional.isPresent())
            return;

        LuckPermsController.syncUserRole(playerOptional.get().getName());
    }

}
