package di.dilogin.minecraft.bungee.controller;

import java.util.stream.Collectors;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import di.dilogin.BungeeApplication;
import di.dilogin.controller.MainController;
import di.dilogin.dto.DIUserDto;
import di.dilogin.dto.SessionDto;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.dto.BotDto;
import di.internal.dto.ConnectionDto;
import di.internal.dto.Demand;
import di.internal.dto.FileDto;
import di.internal.dto.converter.JsonConverter;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

/**
 * Class used to respond to plugin requests.
 *
 * It is expected to obtain:
 * SubChannel: This will be unique. It will be used to send the response and have it retrieved by the origin.
 * Data1: Demand. It will be the identifier that we hope to obtain.
 * Data2: Info of demand. The information required by data1.
 */
public class ChannelMessageController implements Listener {

    /**
     * Main bungee plugin.
     */
    private final Plugin plugin = BungeeApplication.getPlugin();

    @EventHandler
    public void on(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel = in.readUTF();
        String data1 = in.readUTF();
        String data2 = in.readUTF();

        String playerName = getPlayerNameFromSubChannel(subChannel);
        MainController.getDIApi().getInternalController().getChannelController()
                .sendMessageToPluginWithSubChannel(subChannel + "answer", playerName, getResponse(data1, data2));
    }

    /**
     * Get information from the database
     *
     * @param demand The demand.
     * @param data   Information needed for the demand.
     * @return JSON with the information requested.
     */
    private String getResponse(String demand, String data) {
        if (demand.equalsIgnoreCase(Demand.getDIUser.name())) {
            return getDIUser(data);
        } else if (demand.equalsIgnoreCase(Demand.getConfigFile.name())) {
            return getConfigFile();
        } else if (demand.equalsIgnoreCase(Demand.getLangFile.name())) {
            return getLanguageFile();
        } else if (demand.equalsIgnoreCase(Demand.checkConnection.name())) {
            return getConnection();
        } else if (demand.equalsIgnoreCase(Demand.getBotConfig.name())){
            return getBotConfig();
        } else if (demand.equalsIgnoreCase(Demand.getSessionStatus.name())) {
        	return getUserSession(data);
        }
        return "error";
    }
    
    private String getUserSession(String data) {
    	JsonConverter<SessionDto> converter = new JsonConverter<SessionDto>(SessionDto.class);
    	SessionDto dto = converter.getDto(data);
    	boolean isValid = !TmpCache.containsLogin(dto.getPlayerName()) && !TmpCache.containsRegister(dto.getPlayerName());
    	dto.setValid(isValid);
        return converter.getJson(dto);
    }

    private String getDIUser(String playerName) {
        JsonConverter<DIUserDto> converter = new JsonConverter<DIUserDto>(DIUserDto.class);
        DIUserDto dto = new DIUserDto();
        dto.setUuid("Uuid");
        dto.setPlayerName("PlayerName");
        dto.setDiscordId("DiscordId");
        return converter.getJson(dto);
    }

    private String getConfigFile() {
        JsonConverter<FileDto> converter = new JsonConverter<FileDto>(FileDto.class);
        FileDto dto = new FileDto();
        dto.setYamlData(MainController.getDIApi().getInternalController().getConfigManager().getMap());
        return converter.getJson(dto);
    }

    private String getLanguageFile() {
        JsonConverter<FileDto> converter = new JsonConverter<FileDto>(FileDto.class);
        FileDto dto = new FileDto();
        dto.setYamlData(MainController.getDIApi().getInternalController().getLangManager().getMap());
        return converter.getJson(dto);
    }

    private String getConnection(){
        JsonConverter<ConnectionDto> converter = new JsonConverter<ConnectionDto>(ConnectionDto.class);
        ConnectionDto dto = new ConnectionDto();
        dto.setServerList(plugin.getProxy().getServers().keySet().stream().collect(Collectors.toList()));
        dto.setOnlinePlayers(plugin.getProxy().getOnlineCount());
        dto.setServerName(plugin.getProxy().getName());
        return converter.getJson(dto);
    }

    private String getBotConfig(){
        JsonConverter<BotDto> converter = new JsonConverter<BotDto>(BotDto.class);
        BotDto dto = new BotDto();
        dto.setPrefix(MainController.getDIApi().getCoreController().getBot().getPrefix());
        dto.setServerId(MainController.getDIApi().getCoreController().getBot().getServerId());
        return converter.getJson(dto);
    }

    /**
     * Get the player name from the subchannel.
     *
     * @param subChannel The subchannel.
     * @return The player name.
     */
    private String getPlayerNameFromSubChannel(String subChannel) {
        String playerName = "";
        for (int i = 0; i < subChannel.length(); i++) {
            char c = subChannel.charAt(i);
            if (Character.isDigit(c)) {
                break;
            }
            playerName += c;
        }
        return playerName;
    }
}
