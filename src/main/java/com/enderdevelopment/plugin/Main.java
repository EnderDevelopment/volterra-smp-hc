package com.enderdevelopment.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private final Map<UUID, String> playerTeams = new HashMap<>();
    private Team blueTeam;
    private Team purpleTeam;
    private Scoreboard scoreboard;

    @Override
    public void onEnable() {
        // Initialize scoreboard and teams
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        blueTeam = scoreboard.getTeam("blue") != null ? scoreboard.getTeam("blue") : scoreboard.registerNewTeam("blue");
        purpleTeam = scoreboard.getTeam("purple") != null ? scoreboard.getTeam("purple") : scoreboard.registerNewTeam("purple");

        // Configure teams
        blueTeam.setPrefix(ChatColor.BLUE + "[BLUE] ");
        blueTeam.setColor(ChatColor.BLUE);
        purpleTeam.setPrefix(ChatColor.DARK_PURPLE + "[PURPLE] ");
        purpleTeam.setColor(ChatColor.DARK_PURPLE);

        // Register events
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("team")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Usage: /team <join|leave|set|spawn>");
                return true;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "join":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /team join <blue|purple>");
                        return true;
                    }
                    handleTeamJoin(player, args[1].toLowerCase());
                    break;
                case "leave":
                    handleTeamLeave(player);
                    break;
                case "set":
                    if (!player.hasPermission("hardcore.teams.admin")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                        return true;
                    }
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "Usage: /team set <player> <blue|purple>");
                        return true;
                    }
                    handleTeamSet(player, args[1], args[2].toLowerCase());
                    break;
                case "spawn":
                    handleTeamSpawn(player);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown subcommand.");
                    break;
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("spawn")) {
            handleTeamSpawn(player);
            return true;
        } else if (command.getName().equalsIgnoreCase("admin")) {
            if (!player.hasPermission("hardcore.teams.admin")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Usage: /admin <inventory|setteam>");
                return true;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "inventory":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /admin inventory <player>");
                        return true;
                    }
                    handleAdminInventory(player, args[1]);
                    break;
                case "setteam":
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "Usage: /admin setteam <player> <blue|purple>");
                        return true;
                    }
                    handleTeamSet(player, args[1], args[2].toLowerCase());
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown subcommand.");
                    break;
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!playerTeams.containsKey(player.getUniqueId())) {
            // Auto-assign team
            if (blueTeam.getEntries().size() <= purpleTeam.getEntries().size()) {
                blueTeam.addEntry(player.getName());
                playerTeams.put(player.getUniqueId(), "blue");
                player.sendMessage(ChatColor.BLUE + "You have been assigned to the Blue team!");
            } else {
                purpleTeam.addEntry(player.getName());
                playerTeams.put(player.getUniqueId(), "purple");
                player.sendMessage(ChatColor.DARK_PURPLE + "You have been assigned to the Purple team!");
            }
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ItemStack item = event.getItem().getItemStack();

            // Limit totems to 2
            if (item.getType() == Material.TOTEM_OF_UNDYING) {
                int totemCount = 0;
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (invItem != null && invItem.getType() == Material.TOTEM_OF_UNDYING) {
                        totemCount += invItem.getAmount();
                    }
                }
                if (totemCount >= 2) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You can only carry 2 totems at a time!");
                }
            }

            // Limit cobwebs to 64
            if (item.getType() == Material.COBWEB) {
                int cobwebCount = 0;
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (invItem != null && invItem.getType() == Material.COBWEB) {
                        cobwebCount += invItem.getAmount();
                    }
                }
                if (cobwebCount >= 64) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You can only carry 64 cobwebs at a time!");
                }
            }

            // Limit ender pearls to 30
            if (item.getType() == Material.ENDER_PEARL) {
                int enderPearlCount = 0;
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (invItem != null && invItem.getType() == Material.ENDER_PEARL) {
                        enderPearlCount += invItem.getAmount();
                    }
                }
                if (enderPearlCount >= 30) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You can only carry 30 ender pearls at a time!");
                }
            }
        }
    }

    private void handleTeamJoin(Player player, String teamName) {
        if (playerTeams.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already on a team!");
            return;
        }

        Team team;
        if (teamName.equals("blue")) {
            team = blueTeam;
        } else if (teamName.equals("purple")) {
            team = purpleTeam;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid team name. Use blue or purple.");
            return;
        }

        team.addEntry(player.getName());
        playerTeams.put(player.getUniqueId(), teamName);
        player.sendMessage(ChatColor.GREEN + "You have joined the " + team.getColor() + teamName + ChatColor.GREEN + " team!");
    }

    private void handleTeamLeave(Player player) {
        if (!playerTeams.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are not on a team!");
            return;
        }

        String teamName = playerTeams.get(player.getUniqueId());
        Team team = teamName.equals("blue") ? blueTeam : purpleTeam;
        team.removeEntry(player.getName());
        playerTeams.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You have left the " + team.getColor() + teamName + ChatColor.GREEN + " team!");
    }

    private void handleTeamSet(Player player, String targetName, String teamName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        Team team;
        if (teamName.equals("blue")) {
            team = blueTeam;
        } else if (teamName.equals("purple")) {
            team = purpleTeam;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid team name. Use blue or purple.");
            return;
        }

        if (playerTeams.containsKey(target.getUniqueId())) {
            String oldTeamName = playerTeams.get(target.getUniqueId());
            Team oldTeam = oldTeamName.equals("blue") ? blueTeam : purpleTeam;
            oldTeam.removeEntry(target.getName());
        }

        team.addEntry(target.getName());
        playerTeams.put(target.getUniqueId(), teamName);
        player.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " has been set to the " + team.getColor() + teamName + ChatColor.GREEN + " team!");
        target.sendMessage(ChatColor.GREEN + "You have been set to the " + team.getColor() + teamName + ChatColor.GREEN + " team by an admin!");
    }

    private void handleTeamSpawn(Player player) {
        if (!playerTeams.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are not on a team!");
            return;
        }

        String teamName = playerTeams.get(player.getUniqueId());
        // Implement spawn logic based on team
        player.sendMessage(ChatColor.GREEN + "Teleporting to " + (teamName.equals("blue") ? ChatColor.BLUE : ChatColor.DARK_PURPLE) + teamName + ChatColor.GREEN + " spawn...");
    }

    private void handleAdminInventory(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        player.openInventory(target.getInventory());
        player.sendMessage(ChatColor.GREEN + "Viewing inventory of " + target.getName());
    }
}