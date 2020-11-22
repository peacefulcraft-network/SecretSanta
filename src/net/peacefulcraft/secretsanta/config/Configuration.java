package net.peacefulcraft.secretsanta.config;

import java.io.File;
import java.net.URL;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.peacefulcraft.secretsanta.SecretSanta;

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

    // Loading default variables
    debugEnabled = c.getBoolean("debug", false);
    openRegistration = c.getBoolean("openRegistration", false);
    openSubmit = c.getBoolean("openSubmit", false);
    openGift = c.getBoolean("openGift", false);
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
  
  private boolean openRegistration;
    public void setRegistration(boolean b) {
      if(b != openRegistration) {
        openRegistration = b;
        c.set("openRegistration", b);
        saveConfiguration();
      }
    }
    public boolean isRegistrationOpen() { return openRegistration; }

  private boolean openSubmit;
    public void setOpenSubmit(boolean b) {
      if(b != openSubmit) {
        openSubmit = b;
        c.set("openSubmit", b);
        saveConfiguration();
      }
    }
    public boolean isSubmitOpen() { return openSubmit; }

  private boolean openGift;
    public void setOpenGift(boolean b) {
      if(b != openGift) {
        openGift = b;
        c.set("openGift", b);
        saveConfiguration();
      }
    }
    public boolean isGiftOpen() { return openGift; }

  public void saveConfiguration() { SecretSanta._this().saveConfig(); }
}