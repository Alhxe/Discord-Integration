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

    /**
     * Name of the file to be controlled.
     */
    private static final String FILENAME = "config.yml";

    /**
     * The plugin controller.
     */
    private final PluginController controller;

    /**
     * Map of the data obtained from the yaml.
     */
    private Map<String, Object> yamlData;

    /**
     * Main constructor. It initializes the file and loads the data. If the data comes
     * from the plugin located in BungeeCord, the default local file will be temporarily
     * loaded into memory, to later be replaced by the information that comes from BungeeCord.
     *
     * @param controller     Plugin controller.
     * @param dataFolder     The folder where it is located.
     * @param classLoader    Class loader.
     * @param isDataInBungee If the data is in bungee.
     */
    public ConfigManager(PluginController controller, File dataFolder, ClassLoader classLoader, boolean isDataInBungee) {
        this.controller = controller;
        if (!isDataInBungee) {
            File customConfigFile = new File(dataFolder, FILENAME);
            if (!customConfigFile.exists()) {
                customConfigFile.getParentFile().mkdirs();
                saveResource(controller, dataFolder, FILENAME, classLoader, false);
            }
            Yaml yaml = new Yaml();
            try {
                this.yamlData = yaml.load(new FileInputStream(customConfigFile));
            } catch (FileNotFoundException e) {
                controller.getLogger().log(Level.SEVERE, "ConfiManager constructor", e);
            }
        } else {
            this.yamlData = getOriginalYamlContent(FILENAME, classLoader);
        }
    }

    /**
     * @param path The value you want to obtain.
     * @return The content of the sought value.
     */
    public String getString(String path) {
        char specialChar = (char) 167;
        return get(path).replace('&', specialChar);
    }

    /**
     * @param path The value you want to obtain.
     * @return The content of the sought value.
     */
    public long getLong(String path) {
        if (get(path).contains(".")) {
            return (long) Double.parseDouble(get(path));
        }
        return Long.parseLong(get(path));
    }

    /**
     * @param path The value you want to obtain.
     * @return The content of the sought value.
     */
    public boolean getBoolean(String path) {
        return Boolean.parseBoolean(get(path));
    }

    /**
     * @param path The value you want to obtain.
     * @return The content of the sought value.
     */
    public int getInt(String path) {
        if (get(path).contains(".")) {
            return (int) Double.parseDouble(get(path));
        }
        return Integer.parseInt(Long.parseLong(get(path)) + "");
    }

    /**
     * @param path The value you want to obtain.
     * @return The possible content of the sought value.
     */
    public Optional<Long> getOptionalLong(String path) {
        try {
            return Optional.of(Long.parseLong(get(path)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * @param path The value you want to obtain.
     * @return The possible content of the sought value.
     */
    public boolean contains(String path) {
        return this.yamlData.containsKey(path);
    }

    /**
     * @param path The value you want to obtain.
     * @return The possible content of the sought value.
     */
    public List<Map<Object, Object>> getList(String path) {
        Yaml yaml = new Yaml();
        String value = get(path).replace("=", " : ");
        return yaml.load(value);
    }

    @Override
    public Map<String, Object> getMap() {
        return this.yamlData;
    }

    @Override
    public void setData(Map<String, Object> data) {
        this.yamlData = data;
    }

    /**
     * @param path The value you want to obtain.
     * @return The content of the sought value.
     */
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