package di.internal.controller.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.yaml.snakeyaml.Yaml;

import di.internal.controller.PluginController;

/**
 * Configuration file driver.
 */
public class ConfigManager implements FileController {

	private static final String FILENAME = "config.yml";

	private File customConfigFile;

	private Map<String, Object> yamlData;
	
	private PluginController pluginController;

	@SuppressWarnings("unchecked")
	public ConfigManager(PluginController controller, File dataFolder) {
		this.customConfigFile = new File(dataFolder, FILENAME);
		this.pluginController = controller;
		if (!this.customConfigFile.exists()) {
			this.customConfigFile.getParentFile().mkdirs();
			saveResource(controller.getPlugin(), dataFolder, FILENAME, false);
		}
		Yaml yaml = new Yaml();
		try {
			this.yamlData = (Map<String, Object>) yaml.load(new FileInputStream(this.customConfigFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String getString(String path) {
		char specialChar = (char) 167;
		return get(path).replace('&', specialChar);
	}

	public long getLong(String path) {
		return Long.parseLong(get(path));
	}

	public boolean getBoolean(String path) {
		return Boolean.parseBoolean(get(path));
	}

	public int getInt(String path) {
		return Integer.parseInt(get(path));
	}

	public Optional<Long> getOptionalLong(String path) {
		try {
			return Optional.ofNullable(Long.parseLong(get(path)));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public boolean contains(String path) {
		return this.yamlData.containsKey(path);
	}

	private String get(String path) {
		try {
			return this.yamlData.get(path).toString();
		} catch (Exception e) {
			String message = "Failed to find in config file: "+path;
			String message2 = "Regenerate the file to correct the problem or include the above mentioned line in the configuration file.";
			pluginController.getPlugin().getLogger().log(Level.SEVERE, message);
			pluginController.getPlugin().getLogger().log(Level.SEVERE, message2);
			pluginController.getPlugin().getServer().getPluginManager().disablePlugin(pluginController.getPlugin());
		}
		return "error";
	}
}