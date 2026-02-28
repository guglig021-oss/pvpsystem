package com.pvpsystem.commands;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.gui.PvPMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class PvPCommand implements CommandExecutor, TabCompleter {

    private final PvPSystem plugin;
    private final PvPMenu menu;

    public PvPCommand(PvPSystem plugin) {
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
