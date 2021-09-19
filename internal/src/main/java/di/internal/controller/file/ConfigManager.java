package di.internal.controller.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Optional;

import org.yaml.snakeyaml.Yaml;

import di.internal.controller.PluginController;

/**
 * Configuration file driver.
 */
public class ConfigManager implements FileController {

	private static final String FILENAME = "config.yml";

	private File customConfigFile;

	private Map<String, Object> yamlData;

	@SuppressWarnings("unchecked")
	public ConfigManager(PluginController controller, File dataFolder) {
		this.customConfigFile = new File(dataFolder, FILENAME);
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
		return this.yamlData.get(path).toString().replace('&', specialChar);
	}

	public long getLong(String path) {
		return Long.parseLong(this.yamlData.get(path).toString());
	}

	public boolean getBoolean(String path) {
		return Boolean.parseBoolean(this.yamlData.get(path).toString());
	}

	public int getInt(String path) {
		return Integer.parseInt(this.yamlData.get(path).toString());
	}

	public Optional<Long> getOptionalLong(String path) {
		try {
			return Optional.ofNullable(Long.parseLong(this.yamlData.get(path).toString()));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}
}