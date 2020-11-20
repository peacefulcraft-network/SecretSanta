package net.peacefulcraft.secretsanta;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.peacefulcraft.secretsanta.commands.ExampleCommand;
import net.peacefulcraft.secretsanta.config.Configuration;
public class SecretSanta extends JavaPlugin {
  
  public static final String messagingPrefix = ChatColor.GREEN + "[" + ChatColor.BLUE + "PCN" + ChatColor.GREEN + "]" + ChatColor.RESET;

  private static SecretSanta _this;
    public static SecretSanta _this() { return _this; }

  private static Configuration configuration;
    public static Configuration getConfiguration() { return configuration; }

  /**
   * Called when Bukkit server enables the plguin
   * For improved reload behavior, use this as if it was the class constructor
   */
  public void onEnable() {
    this._this = this;
    // Save default config if one does not exist, load the configuration into memory
    this.saveDefaultConfig();
    configuration = new Configuration(this.getConfig());

    this.setupCommands();
    this.setupEventListeners();
  }

  public void logDebug(String message) {
    if (configuration.isDebugEnabled()) {
      this.getServer().getLogger().log(Level.INFO, message);
    }
  }
  
  public void logWarning(String message) {
    this.getServer().getLogger().log(Level.WARNING, message);
  }

  public void logSevere(String message) { 
    this.getServer().getLogger().log(Level.SEVERE, message);
  }

  /**
   * Called whenever Bukkit server disableds the plugin
   * For improved reload behavior, try to reset the plugin to it's initaial state here.
   */
  public void onDisable () {
    this.getServer().getScheduler().cancelTasks(this);
  }

    private void setupCommands() {
      this.getCommand("example").setExecutor(new ExampleCommand());
    }

    private void setupEventListeners() {
      //this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
      // Test comment for discord
    }
}