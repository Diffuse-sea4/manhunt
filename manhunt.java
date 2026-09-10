package com.yourname.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Manhunt extends JavaPlugin implements Listener, CommandExecutor {

    private final Set<UUID> runners = new HashSet<>();
    private final Set<UUID> hunters = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("runner").setExecutor(this);
        getCommand("hunter").setExecutor(this);
        getLogger().info("Classic Manhunt Tracker (1.12.2) Enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use these commands.");
            return true;
        }

        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("runner")) {
            hunters.remove(player.getUniqueId());
            runners.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "You are now a RUNNER. Run and prepare to beat the game!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("hunter")) {
            runners.remove(player.getUniqueId());
            hunters.add(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You are now a HUNTER. You received a tracking compass!");
            
            // Give the hunter a compass automatically
            ItemStack compass = new ItemStack(Material.COMPASS);
            ItemMeta meta = compass.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Runner Tracker");
            compass.setItemMeta(meta);
            player.getInventory().addItem(compass);
            
            return true;
        }

        return false;
    }

    @EventHandler
    public void onCompassClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if the player is holding a compass and right-clicking
        if (event.getItem() != null && event.getItem().getType() == Material.COMPASS) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                
                // Only let registered hunters track runners
                if (!hunters.contains(player.getUniqueId())) {
                    return;
                }

                Player nearestRunner = null;
                double bestDistance = Double.MAX_VALUE;

                // Loop through all online players to find the nearest runner
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (runners.contains(onlinePlayer.getUniqueId())) {
                        // Ensure they are in the same world/dimension
                        if (onlinePlayer.getWorld().equals(player.getWorld())) {
                            double distance = player.getLocation().distance(onlinePlayer.getLocation());
                            if (distance < bestDistance) {
                                bestDistance = distance;
                                nearestRunner = onlinePlayer;
                            }
                        }
                    }
                }

                // Update the compass target
                if (nearestRunner != null) {
                    player.setCompassTarget(nearestRunner.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Compass locked onto nearest runner: " + ChatColor.YELLOW + nearestRunner.getName());
                } else {
                    player.sendMessage(ChatColor.RED + "No runners found in this dimension!");
                }
            }
        }
    }
}
