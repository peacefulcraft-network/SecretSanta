package net.peacefulcraft.secretsanta.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class IOLoader<T extends JavaPlugin> {
    private T plugin;

  private File file = null;
    public File getFile() { return this.file; }

  private FileConfiguration fileConfig = null;

  private String defaultFile;

  public IOLoader(T plugin, String newfile) {
      this(plugin, newfile, (String) null);
  }

  public IOLoader(T plugin, String newfile, String folder) {
      this.plugin = plugin;
      this.defaultFile = newfile;
      if (folder != null) {
        folder = folder.replace("/", System.getProperty("file.separator"));
        String path = plugin.getDataFolder() + System.getProperty("file.separator") + folder;
        File dir = new File(path);
        if (!dir.exists())
          dir.mkdir(); 
        this.file = new File(plugin.getDataFolder() + System.getProperty("file.separator") + folder, newfile);
      } else {
        this.file = new File(plugin.getDataFolder(), newfile);
      } 
      reloadCustomConfig(!this.file.exists());
    }

  public IOLoader(T plugin, File newfile, String folder) {
      this.plugin = plugin;
      this.file = newfile;
      reloadCustomConfig((this.file == null));
  }

  public void reloadCustomConfig(boolean loadDefaults) {
    if (loadDefaults && this.file != null) {
      this.file = new File(this.file.getParent(), this.defaultFile);
      this.fileConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(this.file);
      InputStream defConfigStream = this.plugin.getResource(this.defaultFile);
      if (defConfigStream != null) {
        Reader reader = new InputStreamReader(defConfigStream);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
        this.fileConfig.setDefaults((Configuration)defConfig);
      } 
      getCustomConfig().options().copyDefaults(true);
      this.plugin.getLogger().log(Level.SEVERE, "File " + this.defaultFile + " not found! Creating a new one...");
      saveCustomConfig();
    } else {
      this.fileConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(this.file);
    } 
  }

  public FileConfiguration getCustomConfig() {
    if(this.fileConfig == null)
      reloadCustomConfig((this.file == null));
    return this.fileConfig;
  }

  public void saveCustomConfig() {
    if(this.fileConfig == null || this.file == null)
      return;
    try{
      getCustomConfig().save(this.file);
    } catch (IOException ex) {
      this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.file, ex);
    }
  }
}
