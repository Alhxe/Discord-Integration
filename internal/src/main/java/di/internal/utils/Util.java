package di.internal.utils;

import java.awt.Color;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import di.internal.controller.ChannelController;
import di.internal.controller.CoreController;
import di.internal.controller.file.ConfigManager;
import di.internal.controller.file.YamlManager;
import di.internal.dto.BotDto;
import di.internal.dto.Demand;
import di.internal.dto.FileDto;
import di.internal.dto.converter.JsonConverter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

/**
 * General utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Util {

    /**
     * @param colorStr hexadecimal color.
     * @return Color.
     */
    public static Color hex2Rgb(String colorStr) {
        return new Color(Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }
    
	/**
	 * @param api Discord JDA api.
	 * @param id  Discord user id.
	 * @return Possible user based on their ID.
	 */
	public static Optional<User> getDiscordUserById(JDA api, long id) {
		try {
			Optional<User> cachedUserOpt = Optional.ofNullable(api.getUserById(id));
			if (cachedUserOpt.isPresent())
				return cachedUserOpt;

			CompletableFuture<User> request = api.retrieveUserById(id).submit();
			Optional<User> userOpt = Optional.ofNullable(request.join());
			if (userOpt.isPresent())
				return userOpt;

			return Optional.empty();
		} catch (ErrorResponseException e) {
			return Optional.empty();
		}
	} 

    /**
     * Delete a message after a certain time.
     *
     * @param message Message to be deleted.
     * @param seconds Time in which it will be erased.
     */
    public static void deleteMessage(Message message, int seconds) {
        Executors.newCachedThreadPool().submit(() -> {
            int millis = seconds * 1000;
            try {
                Thread.sleep(millis);
                message.delete().queue();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * @param fileName    File name.
     * @param classLoader Loader.
     * @return File from jar or resources.
     */
    public static InputStream getFileFromResourceAsStream(ClassLoader classLoader, String fileName) {
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new IllegalArgumentException("File not found! " + fileName);
        } else {
            return inputStream;
        }
    }

    /**
     * Generates a random subchannel.
     *
     * @param playerName Player name.
     * @return Random subchannel.
     */
    public static String getRandomSubChannel(String playerName) {
        return playerName + generateNumber();
    }

    /**
     * Generates a random number.
     *
     * @return Random number.
     */
    private static String generateNumber() {
        SecureRandom sr = new SecureRandom();
        StringBuilder result = new StringBuilder((sr.nextInt(9) + 1) + "");
        for (int i = 0; i < 20 - 2; i++) result.append(sr.nextInt(10));
        result.append(sr.nextInt(9) + 1);
        return result.toString();
    }

    /**
     * Loads the configuration file from bungee.
     *s
     * @param controller    Channel controller.
     * @param configManager Config manager.
     * @param playerName    Player name.
     */
    public static void loadConfigFile(ChannelController controller, ConfigManager configManager, String playerName) {
        controller.sendMessageAndWaitResponse(playerName, "getConfigFile", "").whenCompleteAsync((json, throwable) -> {
            if (json != null) {
                JsonConverter<FileDto> fileDtoJsonConverter = new JsonConverter<FileDto>(FileDto.class);
                configManager.setData(fileDtoJsonConverter.getDto(json).getYamlData());
            }
        });
    }

    /**
     * Loads the language file from bungee.
     *
     * @param controller  Channel controller.
     * @param yamlManager Yaml manager.
     * @param playerName  Player name.
     */
    public static void loadLangFile(ChannelController controller, YamlManager yamlManager, String playerName) {
        controller.sendMessageAndWaitResponse(playerName, "getLangFile", "").whenCompleteAsync((json, throwable) -> {
            if (json != null) {
                JsonConverter<FileDto> fileDtoJsonConverter = new JsonConverter<FileDto>(FileDto.class);
                yamlManager.setData(fileDtoJsonConverter.getDto(json).getYamlData());
            }
        });
    }

    /**
     * Update the bot's configuration, asking bungee for the information.
     *
     * @param controller Channel controller.
     * @param coreController Core controller.
     * @param playerName Player name.
     */
    public static void updateBotInfo(ChannelController controller, CoreController coreController, String playerName) {
        controller.sendMessageAndWaitResponse(playerName, Demand.getBotConfig.name(), "")
                .whenCompleteAsync((s, throwable) -> {
                    JsonConverter<BotDto> jsonConverter = new JsonConverter<>(BotDto.class);
                    BotDto botDto = jsonConverter.getDto(s);
                    coreController.setBotInfo(botDto.getPrefix(), botDto.getServerId());
                });
    }
    
    /**
     * @param guild  Discord guild.
     * @param name Discord user name.
     * @return Possible discord user.
     */
    public static Optional<User> getDiscordUserByUsername(Guild guild, String name) {
        List<Member> memberList = guild.getMembersByName(name, false);
        
        if(memberList.size()>0) {
        	return Optional.of(memberList.get(0).getUser());
        }

        CompletableFuture<Optional<User>> futureResult = new CompletableFuture<>();

        // Load members asynchronously
        guild.loadMembers().onSuccess(members -> {
            long count = members.stream()
                               .filter(m -> m.getUser().getName().equalsIgnoreCase(name))
                               .count();

            if (count > 100) {
                System.out.println("More than 100 names have been found that begin with " + name);
                futureResult.complete(Optional.empty());
            } else {
                Optional<Member> matchingMember = members.stream()
                                                         .filter(m -> m.getUser().getName().equalsIgnoreCase(name))
                                                         .findFirst();
                if (matchingMember.isPresent()) {
                    futureResult.complete(Optional.of(matchingMember.get().getUser()));
                } else {
                    futureResult.complete(Optional.empty());
                }
            }
        });

        return futureResult.join();
    }
}