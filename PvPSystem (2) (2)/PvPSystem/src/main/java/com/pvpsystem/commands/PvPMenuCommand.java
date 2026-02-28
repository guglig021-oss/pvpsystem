package com.pvpsystem.commands;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.gui.PvPMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvPMenuCommand implements CommandExecutor {

    private final PvPSystem plugin;
    private final PvPMenu menu;

    public PvPMenuCommand(PvPSystem plugin) {
        this.plugin = plugin;
        this.menu = new PvPMenu(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this!");
            return true;
        }
        menu.open(player);
        return true;
    }
}
