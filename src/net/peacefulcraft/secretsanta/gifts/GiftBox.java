package net.peacefulcraft.secretsanta.gifts;

import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import net.md_5.bungee.api.ChatColor;
import net.peacefulcraft.secretsanta.SecretSanta;
import net.peacefulcraft.secretsanta.io.SantaConfig;

public class GiftBox {
    
    /**Config for this box */
    private SantaConfig config;

    /**Loaded gifts from config */
    private List<Gift> gifts;

    /**Raw string data from config */
    private List<String> sGifts;

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
            Gift g = new Gift(item);
            this.gifts.add(g);
        }
    }

    /**
     * Gets shulker box containing gifts
     * @return Loaded shulkerbox
     */
    public ItemStack getGiftBox() {
        ItemStack box = new ItemStack(Material.GREEN_SHULKER_BOX, 1);
        if(box.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta bm = (BlockStateMeta)box.getItemMeta();
            if(bm.getBlockState() instanceof ShulkerBox) {
                ShulkerBox s = (ShulkerBox) bm.getBlockState();
                
                for(Gift g : gifts) {
                    s.getInventory().addItem(g.getItemStack());
                }

                box.getItemMeta().setDisplayName(getBoxName());
            }
        }

        return box;
    }

    /**
     * Helper function. Logs to console
     */
    private void log(String s) {
        SecretSanta._this().logSevere("[GiftBox] " + s);
    }

    /**
     * Helper function. Gets box custom name
     */
    private String getBoxName() {
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
