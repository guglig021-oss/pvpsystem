package com.pvpsystem.commands;

import com.pvpsystem.PvPSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand implements CommandExecutor {

    private final PvPSystem plugin;

    public QueueCommand(PvPSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this!");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("leave")) {
            if (plugin.getQueueManager().removeFromQueue(player.getUniqueId())) {
                player.sendMessage(plugin.msg("queue-left"));
            } else {
                player.sendMessage(PvPSystem.colorize("&cYou are not in the queue!"));
            }
            return true;
        }

        if (plugin.getMatchManager().isInMatch(player.getUniqueId())) {
            player.sendMessage(PvPSystem.colorize("&cYou are already in a match!"));
            return true;
        }

        if (plugin.getQueueManager().isInQueue(player.getUniqueId())) {
            player.sendMessage(plugin.msg("queue-already"));
            return true;
        }

        plugin.getQueueManager().addToQueue(player);
        player.sendMessage(plugin.msg("queue-joined")
                .replace("{pos}", String.valueOf(plugin.getQueueManager().getPosition(player.getUniqueId()))));
        return true;
    }
}
