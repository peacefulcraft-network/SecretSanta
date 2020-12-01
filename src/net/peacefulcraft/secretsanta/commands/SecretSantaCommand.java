package net.peacefulcraft.secretsanta.commands;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.peacefulcraft.secretsanta.SecretSanta;

public class SecretSantaCommand implements CommandExecutor {

    private final String HELP_MESSAGE = "______" + SecretSanta.messagingPrefix + "______\n" +
                                    "1. Register: Registers you for Secret Santa! Opens December 1st!\n" +
                                    "2. Submit: Submits your gift box in hand to Santa! Opens December 15th!\n" +
                                    "4. Who: Tells you who you are Secret Santa for and gives you your gift box! Opens December 15th!\n" +
                                    "3. GetGift: Tells Santa to give you your present! Opens Christmas Eve!";

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase("secretsanta")) {
            try {

                /**
                 * ADMIN COMMANDS
                 * TODO: REMOVE
                 */
                if(sender.hasPermission("pcn.staff") && args[0].equalsIgnoreCase("admin") || args[0].equalsIgnoreCase("ad")) {
                    if(args[1].equalsIgnoreCase("reload")) {
                        SecretSanta.getGiftManager().reload();
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("getplayers")) {
                        sender.sendMessage(SecretSanta.getGiftManager().getPlayerRegistryString());
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("getgifts")) {
                        sender.sendMessage(SecretSanta.getGiftManager().getGiftRegistryString());
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("getpairs")) {
                        sender.sendMessage(SecretSanta.getGiftManager().getPairedRegistryString());
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("pair")) {
                        SecretSanta.getGiftManager().pairPlayers();
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("clear")) {
                        SecretSanta.getGiftManager().clearData();
                        return true;
                    }
                    if(args[1].equalsIgnoreCase("load")) {
                        SecretSanta.getGiftManager().loadGifts();
                        return true;
                    }
                }

                /**
                 * Calls help string to player
                 */
                if(args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage(HELP_MESSAGE);
                    return true;
                }

                /**
                 * If unregsitered player we register
                 */
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

                /**
                 * Fetches players gift target
                 */
                if(args[0].equalsIgnoreCase("who")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage("Must be player to run this command.");
                        return true;
                    }

                    if(!SecretSanta.getConfiguration().isSubmitOpen()) {
                        sender.sendMessage(SecretSanta.messagingPrefix + " Secret Santa who is not open! Check back on December 15th!");
                        return true;
                    }

                    Player p = (Player) sender;
                    String name = SecretSanta.getGiftManager().getPairedPlayer(p.getUniqueId());
                    p.sendMessage(SecretSanta.messagingPrefix + " You have been assigned " + name + " ho ho ho!");
                    return true;
                }

                /**
                 * Submits gift box
                 */
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
                        p.sendMessage(SecretSanta.messagingPrefix + " You do not have a valid box in your hand!\n Check your box and remove any: Written Books!");
                    }
                    return true;
                }

                /**
                 * Fetches players box
                 */
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
