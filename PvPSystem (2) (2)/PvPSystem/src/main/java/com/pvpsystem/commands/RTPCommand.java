package com.pvpsystem.commands;

import com.pvpsystem.PvPSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPCommand implements CommandExecutor {

    private final PvPSystem plugin;

    public RTPCommand(PvPSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this!");
            return true;
        }
        plugin.getRTPManager().teleport(player);
        return true;
    }
}
