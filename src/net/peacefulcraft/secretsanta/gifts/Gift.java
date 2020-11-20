package net.peacefulcraft.secretsanta.gifts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.peacefulcraft.secretsanta.SecretSanta;

public class Gift {

    /** Raw string passed into constructor */
    private String sRaw;
        /**Fetches raw string data for .yml */
        public String toString() { return this.sRaw; }

    private Material mat;

    private String displayName;

    private Integer amount;

    private int durability;

    private List<String> lore = new ArrayList<>();

    private Map<Enchantment, Integer> enchantments = new HashMap<>();

    public Gift(String s) {
        this.sRaw = s;

        // &material{}
        // Fetching material
        try {
            String sMat = StringUtils.substringBetween(s, "&material{", "}");
            this.mat = Material.valueOf(sMat.toUpperCase());
        } catch(IllegalArgumentException ex) {
            this.mat = Material.STONE;
            this.displayName = "FAILED LOAD";
        }

        // Fetching display name
        String sDisplay = StringUtils.substringBetween(s, "&display{", "}");
        if(sDisplay == null || sDisplay.isEmpty()) {
            this.displayName = null;
        } else {
            this.displayName = sDisplay;
        }

        // Fetching amount of item stack
        this.amount = Integer.valueOf(StringUtils.substringBetween(s, "&amount{", "}"));

        // Fetching durability if it exists
        String sDur = StringUtils.substringBetween(s, "&durability{", "}");
        if(sDur == null || sDur.isEmpty()) {
            this.durability = -1;
        } else {
            this.durability = Short.valueOf(sDur);
        }

        // Fetching and formatting lore
        String sLore = StringUtils.substringBetween(s, "&lore{", "}");
        if(sLore == null || sLore.isEmpty()) {
            this.lore = new ArrayList<>();
        } else {
            String[] split = sLore.split("&split");
            this.lore = Arrays.asList(split);
        }

        // Fetching enchants if any
        String sEnchant = StringUtils.substringBetween(s, "&enchants{", "}");
        if(sEnchant != null && !sEnchant.isEmpty()) {
            String[] split = sEnchant.split("&split");
            for(String sub : split) {
                String[] split2 = sub.split("-");
                try {
                    Enchantment en = Enchantment.getByKey(new NamespacedKey(SecretSanta._this(), split2[0]));
                    this.enchantments.put(en, Integer.valueOf(split2[1]));
                } catch(Exception ex) {
                    continue;
                }
            }
        }

        //TODO: Possibly add loading/saving for metadata tags on items.
    }

    /**
     * Constructor
     * @param item Item we are converting to .yml
     */
    public Gift(ItemStack item) {
        this.mat = item.getType();
        this.displayName = item.getItemMeta().getDisplayName();
        this.amount = item.getAmount();
        
        if(item.getItemMeta() instanceof Damageable) {
            Damageable dam = (Damageable) item.getItemMeta();
            this.durability = dam.getDamage();
        }

        this.lore = item.getItemMeta().getLore();
        this.enchantments = item.getEnchantments();

        // Formatting raw string for .yml
        sRaw += "&material{" + this.mat.toString() + "}";
        sRaw += "&display{" + this.displayName + "}";
        sRaw += "&amount{" + String.valueOf(this.amount) + "}";

        String sLore = "&lore{";
        for(String s : this.lore) {
            sLore += s + "&split";
        }
        sLore += "}";

        sRaw += sLore;

        String sEnchant = "&enchants{";
        for(Enchantment en : this.enchantments.keySet()) {
            String sEn = en.getKey().getKey();
            String sLevel = String.valueOf(this.enchantments.get(en));
            sEnchant += sEn + "-" + sLevel + "&split";
        }
        sEnchant += "}";

        sRaw += sEnchant;
    }

    /**
     * Creates gift item stack
     * @return Item stack loaded from .yml
     */
    public ItemStack getItemStack() {
        ItemStack item = new ItemStack(this.mat, this.amount);
        ItemMeta meta = item.getItemMeta();
        if(this.displayName != null) {
            meta.setDisplayName(this.displayName);
        }

        if(!this.lore.isEmpty()) {
            meta.setLore(this.lore);
        }

        item.setItemMeta(meta);

        if(this.durability != -1) {
            if(meta instanceof Damageable) {
                Damageable dam = (Damageable) item.getItemMeta();
                dam.setDamage(this.durability);
            }
        }

        if(!this.enchantments.isEmpty()) {
            // Possibly redudant.
            item.addUnsafeEnchantments(this.enchantments);
        }

        return item;
    }
}
