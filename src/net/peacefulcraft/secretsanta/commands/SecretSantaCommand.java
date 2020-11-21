package net.peacefulcraft.secretsanta.commands;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.peacefulcraft.secretsanta.SecretSanta;

public class SecretSantaCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase("secretsanta")) {
            try {

                if(args[0].equalsIgnoreCase("register")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage("Must be player to run this command.");
                        return true;
                    }

                    if(!SecretSanta.getConfiguration().isRegistrationOpen()) {
                        sender.sendMessage(SecretSanta.messagingPrefix + " Secret Santa registration is not open! Check back on December 1st!");
                        return true;
                    }

                    Player p = (Player) sender;
                    boolean res = SecretSanta.getGiftManager().registerPlayer(p.getUniqueId());
                    if(res) {
                        p.sendMessage(SecretSanta.messagingPrefix + " Registered for Secret Santa!");
                    } else {
                        p.sendMessage(SecretSanta.messagingPrefix + " You are already registered for Secret Santa!");
                    }
                    return true;
                }

                if(args[0].equalsIgnoreCase("submit")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage("Must be player to run this command.");
                        return true;
                    }

                    if(!SecretSanta.getConfiguration().isSubmitOpen()) {
                        sender.sendMessage(SecretSanta.messagingPrefix + " Gift box submission is not open! Check back on December 15th!");
                        return true;
                    }

                    Player p = (Player) sender;
                    ItemStack hand = p.getInventory().getItemInMainHand();
                    boolean res = SecretSanta.getGiftManager().registerGiftBox(p.getUniqueId(), hand);
                    if(res) {
                        p.sendMessage(SecretSanta.messagingPrefix + " Your Secret Santa box has been submitted!");
                    } else {
                        p.sendMessage(SecretSanta.messagingPrefix + " You do not have a valid box in your hand!");
                    }
                    return true;
                }

                if(args[0].equalsIgnoreCase("getgift")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage("Must be player to run this command.");
                        return true;
                    }

                    if(!SecretSanta.getConfiguration().isGiftOpen()) {
                        sender.sendMessage(SecretSanta.messagingPrefix + " Santa has not arrived yet! Check back on Christmas Day!");
                        return true;
                    }

                    Player p = (Player) sender;
                    ItemStack box = SecretSanta.getGiftManager().getGift(p.getUniqueId());
                    if(box == null) {
                        sender.sendMessage(SecretSanta.messagingPrefix + " There was an issue getting your present from Santa! Check back in a bit!");
                        return true;
                    }

                    HashMap<Integer,ItemStack> left = p.getInventory().addItem(box);
                    for(ItemStack item : left.values()) {
                        p.getLocation().getWorld().dropItemNaturally(p.getLocation(), item);
                    }

                    p.sendMessage(SecretSanta.messagingPrefix + " Hohoho! Merry Christmas!");
                    return true;
                }

            } catch(IndexOutOfBoundsException ex) {
                sender.sendMessage("Error processing command. Use /secretsanta help for commands");
                return true;
            }
        }
        return true;
    }
}
