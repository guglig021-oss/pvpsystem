package com.pvpsystem.commands;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.managers.DuelManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class DuelCommand implements CommandExecutor, TabCompleter {

    private final PvPSystem plugin;
    private final DuelManager duelManager;

    public DuelCommand(PvPSystem plugin) {
        this.plugin = plugin;
        this.duelManager = new DuelManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(PvPSystem.colorize("&cUsage: /duel <player|accept|deny>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            if (!duelManager.acceptRequest(player)) {
                player.sendMessage(PvPSystem.colorize("&cYou have no pending duel requests!"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("deny")) {
            if (!duelManager.denyRequest(player)) {
                player.sendMessage(PvPSystem.colorize("&cYou have no pending duel requests!"));
            }
            return true;
        }

        // Send request
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(PvPSystem.colorize("&cPlayer not found!"));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(PvPSystem.colorize("&cYou cannot duel yourself!"));
            return true;
        }

        if (plugin.getMatchManager().isInMatch(player.getUniqueId())) {
            player.sendMessage(PvPSystem.colorize("&cYou are already in a match!"));
            return true;
        }

        if (plugin.getMatchManager().isInMatch(target.getUniqueId())) {
            player.sendMessage(PvPSystem.colorize("&c" + target.getName() + " is already in a match!"));
            return true;
        }

        duelManager.sendRequest(player, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            completions.add("accept");
            completions.add("deny");
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
