package di.dilogin.minecraft.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import di.dilogin.BukkitApplication;
import di.dilogin.entity.UserData;

/**
 * Pre-login user data controller.
 */
public class UserDataController {

	/**
	 * Prohibits instantiation of the class.
	 */
	private UserDataController() {
		throw new IllegalStateException();
	}

	/**
	 * DILogin data folder.
	 */
	private static final File dataFolder = getPlayerDataFolder();

	public static void saveUserData(Player player) {
		if (isFilePresent(player.getUniqueId().toString()))
			return;

		UserData userData = new UserData(player.getLocation());
		saveData(player.getUniqueId().toString(), userData);
	}

	public static void removeFile(String uuid) {
		File file = new File(dataFolder.getAbsolutePath(), uuid + ".json");
		boolean isDeleted = file.delete();

		if(!isDeleted) {
			BukkitApplication.getDIApi().getInternalController().getPlugin().getLogger().warning("Could not delete file: " + file.getAbsolutePath());
		}
	}

	public static boolean isFilePresent(String uuid) {
		return new File(dataFolder.getAbsolutePath(), uuid + ".json").exists();
	}

	public static Optional<UserData> getUserDataFromUuid(String uuid) {
		try {
			JsonReader reader = new JsonReader(new FileReader(dataFolder.getAbsolutePath() + "/" + uuid + ".json"));
			UserData userData = new Gson().fromJson(reader, UserData.class);
			reader.close();
			return Optional.ofNullable(userData);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	private static void saveData(String uuid, UserData userData) {
		try (FileWriter fileWriter = new FileWriter(dataFolder.getAbsolutePath() + "/" + uuid + ".json")) {
			fileWriter.write(new Gson().toJson(userData));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static File getPlayerDataFolder() {
		File file = new File(BukkitApplication.getDIApi().getInternalController().getDataFolder(), "PlayerData");

		if (file.exists())
			return file;

		file.mkdir();
		return file;
	}

}
