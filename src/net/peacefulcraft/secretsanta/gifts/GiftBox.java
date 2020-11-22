package net.peacefulcraft.secretsanta.gifts;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import net.peacefulcraft.secretsanta.SecretSanta;
import net.peacefulcraft.secretsanta.io.SantaConfig;

public class GiftBox {
    
    /**Config for this box */
    private SantaConfig config;

    /**Loaded gifts from config */
    private List<Gift> gifts = new ArrayList<>();

    /**Raw string data from config */
    private List<String> sGifts = new ArrayList<>();

    /**UUID of box owner */
    private UUID owner;

    /**UUID of box receiver */
    private UUID receiver;

    /**Determines if box was gifted to receiver */
    private boolean isGifted;

    /**
     * Constructor
     * @param sc Config we parse into gifts
     */
    public GiftBox(SantaConfig sc) {
        this.config = sc;

        String oid = sc.getString("Owner", "");
        oid = sc.getString("BoxOwner", oid);
        if(oid == null || oid.isEmpty()) {
            log("No box owner field loaded");
            return;
        }
        this.owner = UUID.fromString(oid);

        String rid = sc.getString("Receiver", "");
        rid = sc.getString("BoxReceiver", rid);
        if(rid == null || rid.isEmpty()) {
            log("No box receiver field loaded");
            return;
        }
        this.receiver = UUID.fromString(rid);

        this.isGifted = sc.getBoolean("IsGifted", false);

        // Loading gifts
        this.sGifts = sc.getStringList("Gifts");
        for(String s : sGifts) {
            Gift g = new Gift(s);
            this.gifts.add(g);
        }
    }

    /**
     * Constructor
     * @param stack ItemStack pulled from shulkerbox
     */
    public GiftBox(ItemStack[] stack, UUID owner, UUID receiver) {
        this.owner = owner;
        this.receiver = owner;

        this.isGifted = false;

        for(ItemStack item : stack) {
            if(item == null || item.getType() == null || item.getType().equals(Material.AIR)) { continue; }
            Gift g = new Gift(item);
            this.gifts.add(g);
        }
    }

    /**
     * Gets shulker box containing gifts
     * @return Loaded shulkerbox, null if gifted already
     */
    public ItemStack getGiftBox() {
        if(this.isGifted) { 
            SecretSanta._this().logDebug("[GiftBox] Gifted");
            return null; 
        }

        ItemStack box = new ItemStack(Material.GREEN_SHULKER_BOX, 1);
        if(box.getItemMeta() instanceof BlockStateMeta) {
            ItemMeta meta = box.getItemMeta();
            BlockStateMeta bm = (BlockStateMeta)meta;
            if(bm.getBlockState() instanceof ShulkerBox) {
                ShulkerBox s = (ShulkerBox) bm.getBlockState();
                
                for(Gift g : gifts) {
                    s.getInventory().addItem(g.getItemStack());
                }
                bm.setBlockState(s);

                bm.setDisplayName(getBoxName());
                box.setItemMeta(bm);
                this.isGifted = true;

                SecretSanta._this().logDebug("[GiftBox] Box got");
                return box;
            }
        }
        SecretSanta._this().logDebug("[GiftBox] Straight null");
        return null;
    }

    /**
     * Creates empty shulker gift box
     * @return Shulker box item
     */
    public static ItemStack getEmptyGiftBox(UUID owner) {
        ItemStack box = new ItemStack(Material.GREEN_SHULKER_BOX, 1);
        ItemMeta meta = box.getItemMeta();

        // Setting owner name in box title
        String name = Bukkit.getPlayer(owner).getName();
        meta.setDisplayName(getBoxName());
        box.setItemMeta(meta);

        return box;
    }

    /**
     * Creates a config map to be saved by manager
     * 
     * @return Hashtable object of GiftBox payload
     */
    public Object getConfig() {
        Hashtable<String, Object> boxTable = new Hashtable<String, Object>();
        boxTable.put("Owner", this.owner.toString());
        boxTable.put("Receiver", this.receiver.toString());
        boxTable.put("IsGifted", this.isGifted);

        List<String> lis = new ArrayList<>();
        for (Gift g : this.gifts) {
            lis.add(g.toString());
        }
        boxTable.put("Gifts", lis);

        return boxTable;
    }

    /**
     * Helper function. Logs to console
     */
    private void log(String s) {
        SecretSanta._this().logSevere("[GiftBox] " + s);
    }

    /**
     * Helper function. Gets box custom name
     * 
     * @return Formattedb box name string
     */
    private static String getBoxName() {
        String s = "Secret Santa Gift Box";
        String out = "";

        for(int i = 0; i < s.length(); i++) {
            if(i % 2 == 0) {
                out += ChatColor.RED + "" + ChatColor.BOLD + s.charAt(i);
            } else {
                out += ChatColor.GREEN + "" + ChatColor.BOLD + s.charAt(i);
            }
        }
        return out;
    }
}
