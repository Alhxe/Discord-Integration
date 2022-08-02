package di.dilogin.minecraft.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import di.dilogin.BukkitApplication;
import di.dilogin.entity.UserData;

/**
 * Pre-login user data controller.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDataController {

	/**
	 * DILogin data folder.
	 */
	private static final File dataFolder = getPlayerDataFolder();

	/**
	 * Gson object for serializing and deserializing user data.
	 */
	public static void saveUserData(Player player) {
		if (isFilePresent(player.getUniqueId().toString()))
			return;

		UserData userData = new UserData(player.getLocation());
		saveData(player.getUniqueId().toString(), userData);
	}

	/**
	 * Removes the user data file for the given player.
	 * @param uuid user uuid.
	 */
	public static void removeFile(String uuid) {
		File file = new File(dataFolder.getAbsolutePath(), uuid + ".json");
		boolean isDeleted = file.delete();

		if(!isDeleted) {
			BukkitApplication.getDIApi().getInternalController().getPlugin().getLogger().warning("Could not delete file: " + file.getAbsolutePath());
		}
	}

	/**
	 * Checks if the user data file is present.
	 * @param uuid user uuid.
	 * @return true if the file is present, false otherwise.
	 */
	public static boolean isFilePresent(String uuid) {
		return new File(dataFolder.getAbsolutePath(), uuid + ".json").exists();
	}

	/**
	 * Loads the user data file for the given player.
	 * @param uuid user uuid.
	 * @return the user data object. Optional clear is file is not present.
	 */
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

	/**
	 * Saves the user data to the file.
	 * @param uuid user uuid.
	 * @param userData user data object.
	 */
	private static void saveData(String uuid, UserData userData) {
		try (FileWriter fileWriter = new FileWriter(dataFolder.getAbsolutePath() + "/" + uuid + ".json")) {
			fileWriter.write(new Gson().toJson(userData));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the player data folder.
	 * @return the player data folder.
	 */
	private static File getPlayerDataFolder() {
		File file = new File(BukkitApplication.getDIApi().getInternalController().getDataFolder(), "PlayerData");

		if (file.exists())
			return file;

		file.mkdir();
		return file;
	}

}
