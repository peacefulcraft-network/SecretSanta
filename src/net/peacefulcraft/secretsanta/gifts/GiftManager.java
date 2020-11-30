package net.peacefulcraft.secretsanta.gifts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import net.md_5.bungee.api.ChatColor;
import net.peacefulcraft.secretsanta.SecretSanta;
import net.peacefulcraft.secretsanta.io.IOHandler;
import net.peacefulcraft.secretsanta.io.IOLoader;
import net.peacefulcraft.secretsanta.io.SantaConfig;

/**
 * Gift manager.
 * Loads gifts from .yml and stores locally
 * Saves gifts on reload/kill
 */
public class GiftManager {
    private final SecretSanta s;

    /**
     * Stores loaded gifts for active server
     */
    private HashMap<UUID, GiftBox> giftRegistry = new HashMap<>();
        //TODO: REMOVE
        public String getGiftRegistryString() { return giftRegistry.toString(); }

    /**
     * Stores registered players for gifts
     */
    private HashMap<UUID, String> playerRegistry = new HashMap<>();
        //TODO: REMOVE
        public String getPlayerRegistryString() { return playerRegistry.toString(); }

    /**
     * Stores paired players
     * Key-> gift giver, value-> gift receiver
     */
    private HashMap<UUID, UUID> pairedPlayers = new HashMap<>();
        //TODO: REMOVE
        public String getPairedRegistryString() { return pairedPlayers.toString(); }

    /**
     * Stores players who have done /who
     */
    private ArrayList<UUID> givenEmpty = new ArrayList<>();
        //TODO: REMOVE
        public String getGivenEmpty() { return givenEmpty.toString(); }

    /**
     * Constructor
     * @param s SecretSanta instance
     */
    public GiftManager(SecretSanta s) {
        this.s = s;
        loadGifts();
    }

    /**
     * Main loading operation
     * Loads all relative .yml files to secret santa
     */
    public void loadGifts() {
        this.giftRegistry.clear();
        this.playerRegistry.clear();
        this.pairedPlayers.clear();
        this.givenEmpty.clear();

        // Fetching loaders from Gifts directory
        IOLoader<SecretSanta> defaultGifts = new IOLoader<SecretSanta>(SecretSanta._this(), "ExampleGift.yml", "Gifts");
        defaultGifts = new IOLoader<SecretSanta>(SecretSanta._this(), "ExampleGift.yml", "Gifts");
        List<File> giftFiles = IOHandler.getAllFiles(defaultGifts.getFile().getParent());
        List<IOLoader<SecretSanta>> giftLoaders = IOHandler.getSaveLoad(SecretSanta._this(), giftFiles, "Gifts");

        for(IOLoader<SecretSanta> s1 : giftLoaders) {
            for(String name : s1.getCustomConfig().getConfigurationSection("").getKeys(false)) {
                try {
                    SantaConfig sc = new SantaConfig(name, s1.getFile(), s1.getCustomConfig());

                    // Fetching owner of box by UUID
                    String owner = s1.getCustomConfig().getString(name + ".Owner");
                    owner = s1.getCustomConfig().getString(name + ".BoxOwner", owner);

                    GiftBox box = new GiftBox(sc);
                    this.giftRegistry.put(UUID.fromString(owner), box);

                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        //SecretSanta._this().logDebug("[GiftManager] Gift data load complete.");

        IOLoader<SecretSanta> playerLoader = new IOLoader<SecretSanta>(SecretSanta._this(), "PlayerRegistry.yml", "Data");
        SantaConfig pConfig = new SantaConfig("PlayerRegistry", playerLoader.getFile(), playerLoader.getCustomConfig());
        registerPlayers(pConfig);
        //SecretSanta._this().logDebug("[GiftManager] Player Registry load complete.");

        IOLoader<SecretSanta> pairLoader = new IOLoader<SecretSanta>(SecretSanta._this(), "PairedRegistry.yml", "Data");
        SantaConfig pairConfig = new SantaConfig("PairedRegistry", pairLoader.getFile(), pairLoader.getCustomConfig());
        registerPairs(pairConfig);
        //SecretSanta._this().logDebug("[GiftManager] Paired Registry load complete.");

        IOLoader<SecretSanta> givenLoader = new IOLoader<SecretSanta>(SecretSanta._this(), "GivenEmptyRegistry.yml", "Data");
        SantaConfig givenConfig = new SantaConfig("GivenEmptyRegistry", givenLoader.getFile(), givenLoader.getCustomConfig());
        registerGiven(givenConfig);

        SecretSanta._this().logDebug("[GiftManager] SecretSanta Data loading complete.");
    }

    /**
     * Saves gift registry to .yml
     */
    public void save() {        
        /*
         * Iterate over registry to pull box values
         * Creating new IOLoader for instance 
         */
        for(UUID id : giftRegistry.keySet()) {
            GiftBox box = giftRegistry.get(id);

            String sId = id.toString();
            IOLoader<SecretSanta> config = new IOLoader<SecretSanta>(SecretSanta._this(), sId + ".yml", "Gifts");

            FileConfiguration fCon = config.getCustomConfig();
            fCon.createSection("SecretSanta");
            fCon.set("SecretSanta", box.getConfig());

            try{
                fCon.save(config.getFile());
            } catch(IOException ex) {
                SecretSanta._this().logSevere("[GiftManager] Failed to save: " + sId + ".yml");
                continue;
            }
        }
        //SecretSanta._this().logDebug("[GiftManager] Gift Registry save successful.");

        IOLoader<SecretSanta> pairedConfig = new IOLoader<SecretSanta>(SecretSanta._this(), "PairedRegistry.yml", "Data");
        FileConfiguration fPaired = pairedConfig.getCustomConfig();
        fPaired.createSection("paired");
        fPaired.set("paired", savePairs());

        try{
            fPaired.save(pairedConfig.getFile());
            //SecretSanta._this().logDebug("[GiftManager] PairedRegistry.yml save successful.");
        } catch(IOException ex) {
            SecretSanta._this().logSevere("[GiftManager] Failed to save: PairedRegistry.yml");
        }

        IOLoader<SecretSanta> playerConfig = new IOLoader<SecretSanta>(SecretSanta._this(), "PlayerRegistry.yml", "Data");
        FileConfiguration fPlayer = playerConfig.getCustomConfig();
        fPlayer.createSection("registered");
        fPlayer.set("registered", savePlayers());

        try{
            fPlayer.save(playerConfig.getFile());
            //SecretSanta._this().logDebug("[GiftManager] PlayerRegistry.yml save successful.");
        } catch(IOException ex) {
            SecretSanta._this().logSevere("[GiftManager] Failed to save: PlayerRegistry.yml");
        }

        IOLoader<SecretSanta> givenConfig = new IOLoader<SecretSanta>(SecretSanta._this(), "GivenEmptyRegistry.yml", "Data");
        FileConfiguration fGiven = givenConfig.getCustomConfig();
        fGiven.createSection("given");
        fGiven.set("given", saveGivens());

        try{
            fGiven.save(givenConfig.getFile());
        } catch(IOException ex) {
            SecretSanta._this().logSevere("[GiftManager] Failed to save: GivenEmptyRegistry.yml");
        }

        SecretSanta._this().logDebug("[GiftManager] Saving complete.");
    }

    public void reload() {
        save();
        loadGifts();
        SecretSanta._this().logDebug("[GiftManager] Reload complete.");
    }

    public void clearData() {
        this.playerRegistry.clear();
        this.giftRegistry.clear();
        this.pairedPlayers.clear();
        this.givenEmpty.clear();
        SecretSanta._this().logDebug("[GiftManager] Data cleared.");
    }

    /**
     * Helper method
     * Registers players from .yml to active server instance
     * @param sc Config of players
     */
    private void registerPlayers(SantaConfig sc) {
        // Second try.
        Set<String> keys = sc.getBaseKeys("registered");
        for(String key : keys) {
            String name = sc.getBaseString("registered." + key);
            UUID id = UUID.fromString(key);
            playerRegistry.put(id, name);
        }
    }

    /**
     * Helper method
     * Re-registers paired players from .yml
     * @param sc Config of pairs
     */
    private void registerPairs(SantaConfig sc) {
        Set<String> keys = sc.getBaseKeys("paired");
        for(String key : keys) {
            UUID owner = UUID.fromString(key);
            UUID rec = UUID.fromString(sc.getBaseString("paired." + key));
            pairedPlayers.put(owner, rec);
        }
    }

    /**
     * Helper method
     * Re-registers given empties
     * @param sc Config of givens
     */
    private void registerGiven(SantaConfig sc) {
        List<String> lis = sc.getBaseStringList("given");
        for(String s : lis) {
            UUID given = UUID.fromString(s);
            givenEmpty.add(given);
        }
    }

    /**
     * Helper method
     * Creates a config map of paired players
     */
    private Object savePairs() {
        Hashtable<String, String> pairTable = new Hashtable<String, String>();
        for(UUID id : pairedPlayers.keySet()) {
            String owner = id.toString();
            String rec = pairedPlayers.get(id).toString();

            pairTable.put(owner, rec);
        }

        return pairTable;
    }

    /**
     * Helper method
     * Creates a config map of registered players
     */
    private Object savePlayers() {
        Hashtable<String, String> playerTable = new Hashtable<>();
        for(UUID id : playerRegistry.keySet()) {
            playerTable.put(id.toString(), playerRegistry.get(id));
        }
        return playerTable;
    }

    /**
     * Helper method
     * Creates config list of givens
     */
    private Object saveGivens() {
        List<String> out = new ArrayList<>();
        for(UUID id : givenEmpty) {
            out.add(id.toString());
        }
        return out;
    }

    /**
     * Registers player for gifts
     * @param id UUID of player
     */
    public boolean registerPlayer(UUID id) {
        if(playerRegistry.containsKey(id)) { return false; }
        String name = Bukkit.getPlayer(id).getName();
        playerRegistry.put(id, name);

        return true;
    }

    /**
     * Registers shulker box
     * @param id ID of player registering
     * @param item Shulkerbox
     * @return True if successful, false otherwise.
     */
    public boolean registerGiftBox(UUID id, ItemStack item) {
        if(!item.getType().equals(Material.GREEN_SHULKER_BOX)) { return false; }
        if(!ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase("Secret Santa Gift Box")) { return false; }

        if(item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta bm = (BlockStateMeta)item.getItemMeta();
            if(bm.getBlockState() instanceof ShulkerBox) {
                ShulkerBox s = (ShulkerBox) bm.getBlockState();

                // Player is not registered
                if(!playerRegistry.containsKey(id)) { return false; }

                // Player is not paired
                UUID rec = pairedPlayers.get(id);
                if(rec == null) { return false; }

                if(s.getInventory().contains(Material.WRITTEN_BOOK)) { return false; }

                // Create and register box
                GiftBox box = new GiftBox(s.getInventory().getContents(), id, rec);
                giftRegistry.put(id, box);

                Player p = Bukkit.getPlayer(id);
                ItemStack i = p.getInventory().getItemInMainHand();
                p.getInventory().remove(i);

                return true;
            }
        }

        return false;
    }

    /**
     * Pairs players to their secret santa
     */
    public void pairPlayers() {
        List<UUID> lis = new ArrayList<UUID>(playerRegistry.keySet());
        
        Collections.shuffle(lis);
        UUID first = lis.get(0);

        for(int i = 0; i <= lis.size(); i++) {
            UUID temp = lis.get(i);
            if(i == lis.size()) {
                pairedPlayers.put(temp, first);
                break;
            }

            UUID tempNext = lis.get(i + 1);
            pairedPlayers.put(temp, tempNext);
        }

        SecretSanta._this().logDebug("[GiftManager] Pairing complete: " + pairedPlayers.toString());
    }

    /**
     * Fetches the players gift target and gives player free box
     * @param santa ID of santa
     * @return String player name
     */
    public String getPairedPlayer(UUID santa) {
        // Giving player empty box
        if(!givenEmpty.contains(santa)) {
            Player p = Bukkit.getPlayer(santa);
            HashMap<Integer, ItemStack> left = p.getInventory().addItem(GiftBox.getEmptyGiftBox(santa));
            for(ItemStack item : left.values()) {
                p.getLocation().getWorld().dropItemNaturally(p.getLocation(), item);
            }
            givenEmpty.add(santa);
        }

        // Getting players partner
        return playerRegistry.get(pairedPlayers.get(santa));
    }

    /**
     * Fetches gift box of santa for player
     * @param id ID of receiver
     * @return ShulkerBox with contents, null if it doesn't exist or was gifted already
     */
    public ItemStack getGift(UUID id) {
        for(UUID i : pairedPlayers.keySet()) {
            SecretSanta._this().logDebug("[GiftManager] Test: " + i.toString());
            if(pairedPlayers.get(i).equals(id)) {
                return giftRegistry.get(i).getGiftBox();
            }
        }

        SecretSanta._this().logDebug("[GiftManager] Hit butt");
        return null;
    }

    /**
     * Gets registered players name
     * @param id ID of player
     * @return String name or null
     */
    public String getPlayerName(UUID id) {
        return playerRegistry.get(id);
    }
}
