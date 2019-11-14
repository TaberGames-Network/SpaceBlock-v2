package cc.taber.spaceblock;

import cc.taber.spaceblock.core.commands.CoreCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CoreClass extends JavaPlugin {

    public static CoreClass plugin;
    private File customConfigFile;
    private FileConfiguration customConfig;

    public void onEnable() {
       // createCustomConfig();
        CoreCommand.registerCommands(this);
    }

    public void onDisable() {

    }

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }

    private void createCustomConfig() {

        customConfigFile = new File(getDataFolder(), "taber.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("taber.yml", false);
        }

        customConfig= new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}