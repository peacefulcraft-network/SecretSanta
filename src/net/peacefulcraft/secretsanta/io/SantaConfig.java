package net.peacefulcraft.secretsanta.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class SantaConfig implements GenericConfig {
    private String configName;

    private File file;

    private FileConfiguration fc;

    public File getFile() {
        return this.file;
    }

    public SantaConfig(String name, FileConfiguration fc) {
        this.configName = name;
        this.fc = fc;
    }

    public SantaConfig(String name, File file, FileConfiguration fc) {
        this.configName = name;
        this.file = file;
        this.fc = fc;
    }

    public SantaConfig(String name, File file) {
        this.configName = name;
        this.file = file;
        this.fc = (FileConfiguration)new YamlConfiguration();
        this.fc.createSection(this.configName);
    }

    public void setKey(String key) {
        this.configName = key;
    }

    public String getKey() {
        return this.configName;
    }

    public FileConfiguration getFileConfiguration() {
        return this.fc;
    }

    public boolean isSet(String field) {
        return this.fc.isSet(this.configName + "." + field);
    }

    public void set(String key, Object value) {
        this.fc.set(this.configName + "." + key, value);
    }

    public void load() {
        this.fc = (FileConfiguration)YamlConfiguration.loadConfiguration(this.file);
    }

    public void save() {
        try {
            this.fc.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SantaConfig getNestedConfig(String field) {
        return new SantaConfig(this.configName + "." + field, this.fc);
    }

    public Map<String, SantaConfig> getNestedConfigs(String key) {
        Map<String, SantaConfig> map = new HashMap<>();
        if(!isSet(key))
            return map;
        for(String k : getKeys(key))
            map.put(k, new SantaConfig(this.configName + "." + key + "." + k, this.fc));
        return map;
    }

    @Override
    public boolean getBoolean(String field) {
        return this.fc.getBoolean(this.configName + "." + field);
    }

    @Override
    public boolean getBoolean(String field, boolean def) {
        return this.fc.getBoolean(this.configName + "." + field, def);
    }

    @Override
    public String getString(String field) {
        return this.fc.getString(this.configName + "." + field);
    }

    @Override
    public String getString(String field, String def) {
        return this.fc.getString(this.configName + "." + field, def);
    }

    @Override
    public int getInteger(String paramString) {
        return this.fc.getInt(this.configName + "." + paramString);
    }

    @Override
    public int getInteger(String field, int def) {
        return this.fc.getInt(this.configName + "." + field, def);
    }

    public double getDouble(String field) {
        return this.fc.getDouble(this.configName + "." + field);
    }

    public double getDouble(String field, double def) {
        return this.fc.getDouble(this.configName + "." + field, def);
    }  
    
    public List<String> getStringList(String field) {
        return this.fc.getStringList(this.configName + "." + field);
    }

    public List<String> getColorStringList(String field) {
        List<String> list = this.fc.getStringList(this.configName + "." + field);
        List<String> parsed = new ArrayList<>();
        if (list != null)
            for (String str : list)
                parsed.add(ChatColor.translateAlternateColorCodes('&', str)); 
        return parsed;  
    }

    public List<Map<?, ?>> getMapList(String field) {
        return this.fc.getMapList(this.configName + "." + field);
    }

    public List<?> getList(String field) {
        return this.fc.getList(this.configName + "." + field);
    }

    public List<Byte> getByteList(String field) {
        return this.fc.getByteList(this.configName + "." + field);
    }

    public ItemStack getItemStack(String field, String def) {
        return this.fc.getItemStack(this.configName + "." + field);
    }

    public boolean isConfigurationSection(String section) {
        return this.fc.isConfigurationSection(this.configName + "." + section);
    }

    public Set<String> getKeys(String section) {
        return this.fc.getConfigurationSection(this.configName + "." + section).getKeys(false);
    }

    public boolean isList(String section) {
        return this.fc.isList(this.configName + "." + section);
    }

    public Set<String> getBaseKeys(String section) {
        return this.fc.getConfigurationSection(section).getKeys(false);
    }

    public String getBaseString(String field) {
        return this.fc.getString(field);
    }

    public List<String> getBaseStringList(String field) {
        return this.fc.getStringList(field);
    }
}
