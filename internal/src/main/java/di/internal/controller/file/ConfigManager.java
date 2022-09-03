package di.internal.controller.file;

import di.internal.controller.PluginController;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Configuration file driver.
 */
public class ConfigManager implements FileController {

	private static final String FILENAME = "config.yml";

	private final PluginController controller;

	private Map<String, Object> yamlData;

	public ConfigManager(PluginController controller, File dataFolder) {
		File customConfigFile = new File(dataFolder, FILENAME);
		this.controller = controller;
		if (!customConfigFile.exists()) {
			customConfigFile.getParentFile().mkdirs();
			saveResource(controller, dataFolder, FILENAME, false);
		}
		Yaml yaml = new Yaml();
		try {
			this.yamlData = yaml.load(new FileInputStream(customConfigFile));
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
			return Optional.of(Long.parseLong(get(path)));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public boolean contains(String path) {
		return this.yamlData.containsKey(path);
	}

	public List<Map<Object, Object>> getList(String path) {
		Yaml yaml = new Yaml();
		String value = get(path).replace("=", " : ");
		return yaml.load(value);
	}

	private String get(String path) {
		try {
			return this.yamlData.get(path).toString();
		} catch (Exception e) {
			String message = "Failed to find in config file: " + path;
			String message2 = "Regenerate the file to correct the problem or include the above mentioned line in the configuration file.";
			controller.getLogger().log(Level.SEVERE, message);
			controller.getLogger().log(Level.SEVERE, message2);
			controller.disablePlugin();
		}
		return "error";
	}
}