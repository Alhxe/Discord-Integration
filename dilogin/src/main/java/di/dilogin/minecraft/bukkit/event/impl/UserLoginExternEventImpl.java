package di.dilogin.minecraft.bukkit.event.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import di.dilogin.controller.MainController;
import di.dilogin.dto.SessionDto;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.internal.dto.Demand;
import di.internal.dto.converter.JsonConverter;
import di.internal.utils.Util;

public class UserLoginExternEventImpl implements Listener {
    private final Plugin plugin;

    public UserLoginExternEventImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!MainController.getDILoginController().isLoginSystemEnabled())
            return;

        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerIp = player.getAddress().getAddress().toString();

        // We block the user while waiting for their registration or login
        UserBlockedCache.add(playerName);

        JsonConverter<SessionDto> sessionConverter = new JsonConverter<>(SessionDto.class);
        SessionDto session = new SessionDto();
        session.setPlayerName(playerName);
        session.setIp(playerIp);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !UserBlockedCache.contains(playerName)) {
                    cancel();
                    return;
                }

                String subChannel = Util.getRandomSubChannel(playerName);
                String jsonSession = sessionConverter.getJson(session);

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    String response = MainController.getDIApi().getInternalController().getChannelController()
                            .sendMessageAndWaitResponseWithSubChannel(subChannel, playerName, Demand.getSessionStatus.name(), jsonSession).join();

                    if (response != null && !response.equals("error")) {
                        SessionDto responseDto = sessionConverter.getDto(response);
                        if (responseDto.getPlayerName().equals(session.getPlayerName()) && responseDto.getIp().equals(session.getIp()) && responseDto.isValid()) {
                            UserBlockedCache.remove(playerName);
                            cancel();
                        }
                    }
                });
            }
        }.runTaskTimer(plugin, 10L, 30L);
    }
}
