package net.peacefulcraft.templateus.config;

import java.io.File;
import java.net.URL;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.peacefulcraft.templateus.Templateus;

public class Configuration {
  private FileConfiguration c;

  public Configuration(FileConfiguration c) {
    this.c = c;

    /**
     * Load the default plugin configration and use it's values as fallbacks if user-supplied configuration is incomplete.
     * This will also copy the default values for any missing configuration directives into the user's configuration.
     */
    URL defaultConfigurationURI = getClass().getClassLoader().getResource("config.yml");
    File defaultConfigurationFile = new File(defaultConfigurationURI.toString());
    YamlConfiguration defaultConfiguration = YamlConfiguration.loadConfiguration(defaultConfigurationFile);
    c.setDefaults(defaultConfiguration);
    saveConfiguration();
  }

  private boolean debugEnabled;
    public void setDebugEnabled(boolean v) {
      // Avoid blocking disk work if we can
      if (v != debugEnabled) {
        debugEnabled = v;
        c.set("debug", v);
        saveConfiguration();
      }
    }
    public boolean isDebugEnabled() { return debugEnabled; }

  public void saveConfiguration() { Templateus._this().saveConfig(); }
}