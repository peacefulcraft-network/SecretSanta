package net.peacefulcraft.secretsanta.gifts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

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

    /**
     * Stores registered players for gifts
     */
    private HashMap<UUID, String> playerRegistry = new HashMap<>();

    /**
     * Stores paired players
     * Key-> gift giver, value-> gift receiver
     */
    private HashMap<UUID, UUID> pairedPlayers = new HashMap<>();

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

        // Fetching loaders from Gifts directory
        IOLoader<SecretSanta> defaultGifts = new IOLoader<SecretSanta>(SecretSanta._this(), "ExampleGift.yml", "Gifts");
        defaultGifts = new IOLoader<SecretSanta>(SecretSanta._this(), "ExampleGift.yml", "Gifts");
        List<File> giftFiles = IOHandler.getAllFiles(defaultGifts.getFile().getParent());
        List<IOLoader<SecretSanta>> giftLoaders = IOHandler.getSaveLoad(SecretSanta._this(), giftFiles, "Gifts");

        for(IOLoader<SecretSanta> s1 : giftLoaders) {
            for(String name : s1.getCustomConfig().getConfigurationSection("").getKeys(false)) {
                try {
                    String file = s1.getFile().getPath();
                    SantaConfig sc = new SantaConfig(name, s1.getFile(), s1.getCustomConfig());

                    // Catching file of registered players
                    if(file.contains("PlayerRegistry.yml")) {
                        registerPlayers(sc);
                        continue;
                    }

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
            IOLoader<SecretSanta> config = new IOLoader<SecretSanta>(SecretSanta._this(), sId + ".yml");

            FileConfiguration fCon = config.getCustomConfig();
            fCon.createSection("SecretSanta");
            fCon.set("SecretSanta", box.getConfig());

            try{
                fCon.save(config.getFile());
            } catch(IOException ex) {
                SecretSanta._this().logSevere("[GiftManager] Failed to save: " + sId + ".yml");
                return;
            }
        }

        SecretSanta._this().logDebug("[GiftManager] Saving complete.");
    }

    public void reload() {
        save();
        loadGifts();
    }

    /**
     * Helper method
     * Registers players from .yml to active server instance
     * @param sc Config of players
     */
    private void registerPlayers(SantaConfig sc) {
        // Players are stored in lis of maps
        // Format-> [UUID] : [name]
        List<Map<?,?>> lis = sc.getMapList("registered");
        for(Map<?,?> m : lis) {
            for(Object o : m.keySet()) {
                Object value = m.get(o);

                UUID id = UUID.fromString(o.toString());
                String name = value.toString();

                playerRegistry.put(id, name);
            }
        }
    }

    /**
     * Registers shulker box
     * @param id ID of player registering
     * @param item Shulkerbox
     * @return True if successful, false otherwise.
     */
    public boolean registerGiftBox(UUID id, ItemStack item) {
        if(!item.getType().equals(Material.GREEN_SHULKER_BOX)) { return false; }
        if(!item.getItemMeta().getDisplayName().equalsIgnoreCase("Secret Santa Gift Box")) { return false; }

        if(item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta bm = (BlockStateMeta)item.getItemMeta();
            if(bm.getBlockState() instanceof ShulkerBox) {
                ShulkerBox s = (ShulkerBox) bm.getBlockState();

                // Player is not registered
                if(!playerRegistry.containsKey(id)) { return false; }

                // Player is not paired
                UUID rec = pairedPlayers.get(id);
                if(rec == null) { return false; }

                // Create and register box
                GiftBox box = new GiftBox(s.getInventory().getContents(), id, rec);
                giftRegistry.put(id, box);

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
        Random rand = new Random();

        if(lis.size() % 2 != 0) {
            SecretSanta._this().logWarning("[GiftManager] Registered player size uneven. Register FixedKage.");
            return;
        }

        while(!lis.isEmpty()) {
            UUID first = lis.remove(rand.nextInt(lis.size()));
            UUID second = lis.remove(rand.nextInt(lis.size()));

            pairedPlayers.put(first, second);
        }
    }

    /**
     * Fetches the players gift target
     * @param santa ID of santa
     * @return String player name
     */
    public String getPairedPlayer(UUID santa) {
        return Bukkit.getPlayer(pairedPlayers.get(santa)).getDisplayName();
    }

    /**
     * Fetches gift box of santa for player
     * @param id ID of receiver
     * @return ShulkerBox with contents, null if it doesn't exist or was gifted already
     */
    public ItemStack getGift(UUID id) {
        for(UUID i : pairedPlayers.keySet()) {
            if(pairedPlayers.get(i) == id) {
                return giftRegistry.get(i).getGiftBox();
            }
        }

        return null;
    }
}
