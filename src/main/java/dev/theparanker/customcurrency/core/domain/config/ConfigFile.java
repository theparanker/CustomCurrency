package dev.theparanker.customcurrency.core.domain.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigFile extends YamlConfiguration {
    public final String finalName;
    public final String resourcePath;
    public final Plugin plugin;
    private final File dataFolder;
    private final boolean hasDefaultResource;

    public ConfigFile(Plugin plugin, File dataFolder, String name, boolean load) {
        this.plugin = plugin;
        this.finalName = name;
        this.resourcePath = name + ".yml";
        this.dataFolder = dataFolder;
        this.hasDefaultResource = hasResourceFile();
        this.initializeConfig(load);
    }

    public ConfigFile(Plugin plugin, String name, boolean load, boolean forceCreate) {
        this.plugin = plugin;
        this.finalName = name;
        this.resourcePath = name + ".yml";
        this.dataFolder = plugin.getDataFolder();
        this.hasDefaultResource = hasResourceFile();
        if (forceCreate) {
            this.create();
        } else {
            this.initializeConfig(load);
        }
    }

    public ConfigFile(Plugin plugin, String name, boolean load) {
        this.plugin = plugin;
        this.finalName = name;
        this.resourcePath = name + ".yml";
        this.dataFolder = plugin.getDataFolder();
        this.hasDefaultResource = hasResourceFile();
        this.initializeConfig(load);
    }

    public ConfigFile(Plugin plugin, String name) {
        this.plugin = plugin;
        this.finalName = name;
        this.resourcePath = name + ".yml";
        this.dataFolder = plugin.getDataFolder();
        this.hasDefaultResource = hasResourceFile();
        this.initializeConfig(true);
    }

    // Nuovo costruttore per supportare sottocartelle
    public ConfigFile(Plugin plugin, File dataFolder, String folderPath, String fileName, boolean load) {
        this.plugin = plugin;
        this.finalName = fileName;
        this.resourcePath = folderPath + "/" + fileName + ".yml";
        this.dataFolder = dataFolder;
        this.hasDefaultResource = hasResourceFile();
        this.initializeConfig(load);
    }

    private boolean hasResourceFile() {
        InputStream resource = plugin.getResource(resourcePath);
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // Ignora
            }
            return true;
        }
        return false;
    }

    private void initializeConfig(boolean load) {
        try {
            File file = new File(dataFolder, finalName + ".yml");

            if (!file.exists() && hasDefaultResource) {
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                // Usa il resourcePath corretto
                saveResourceFromJar(resourcePath, file);
            }

            if (load && file.exists()) {
                this.load(file);
            }

            if (hasDefaultResource) {
                this.updateConfigWithDefaults();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveResourceFromJar(String resourcePath, File targetFile) {
        try {
            InputStream in = plugin.getResource(resourcePath);
            if (in == null) {
                plugin.getLogger().warning("Impossibile trovare la risorsa: " + resourcePath);
                return;
            }

            Files.copy(in, targetFile.toPath());
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateConfigWithDefaults() {
        if (!hasDefaultResource) {
            return;
        }

        try {
            File configFile = new File(dataFolder, finalName + ".yml");
            if (!configFile.exists()) {
                return;
            }

            InputStream defaultConfigStream = plugin.getResource(resourcePath);
            if (defaultConfigStream == null) {
                return;
            }

            String defaultContent = new String(defaultConfigStream.readAllBytes(), StandardCharsets.UTF_8);
            defaultConfigStream.close();

            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(plugin.getResource(resourcePath), StandardCharsets.UTF_8)
            );

            Set<String> defaultKeys = defaultConfig.getKeys(true);
            Set<String> currentKeys = this.getKeys(true);

            boolean hasNewKeys = false;

            for (String key : defaultKeys) {
                if (!currentKeys.contains(key)) {
                    hasNewKeys = true;
                    break;
                }
            }

            if (hasNewKeys) {
                YamlConfiguration backup = new YamlConfiguration();
                backup.load(configFile);

                Files.write(configFile.toPath(), defaultContent.getBytes(StandardCharsets.UTF_8));

                this.load(configFile);

                for (String key : backup.getKeys(true)) {
                    if (defaultConfig.contains(key)) {
                        this.set(key, backup.get(key));
                    }
                }

                this.save(configFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void create() {
        try {
            File file = new File(dataFolder, this.finalName + ".yml");
            if (!file.exists()) {
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            File file = new File(dataFolder, this.finalName + ".yml");
            if (file.exists()) {
                this.load(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            this.save(new File(dataFolder, this.finalName + ".yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        try {
            File file = new File(dataFolder, this.finalName + ".yml");
            if (file.exists()) {
                this.load(file);
                if (hasDefaultResource) {
                    this.updateConfigWithDefaults();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDefaults(String path, Object value) {
        if (!this.contains(path)) {
            this.set(path, value);
        }
    }

    public void forceUpdateDefaults() {
        if (hasDefaultResource) {
            this.updateConfigWithDefaults();
        }
    }

    public void deleteFile() {
        try {
            File file = new File(dataFolder, this.finalName + ".yml");
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasDefaultResource() {
        return hasDefaultResource;
    }

    public boolean exists() {
        return new File(dataFolder, finalName + ".yml").exists();
    }

}