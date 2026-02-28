package com.pvpsystem.commands;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.arena.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaCommand implements CommandExecutor, TabCompleter {

    private final PvPSystem plugin;

    public ArenaCommand(PvPSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("pvpsystem.admin")) {
            sender.sendMessage(plugin.msg("no-permission"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can manage arenas in-world!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) { player.sendMessage(PvPSystem.colorize("&cUsage: /arena create <name>")); return true; }
                String name = args[1];
                if (plugin.getArenaManager().createArena(name)) {
                    player.sendMessage(plugin.msg("arena-created").replace("{name}", name));
                } else {
                    player.sendMessage(PvPSystem.colorize("&cArena &e" + name + " &calready exists!"));
                }
            }
            case "delete" -> {
                if (args.length < 2) { player.sendMessage(PvPSystem.colorize("&cUsage: /arena delete <name>")); return true; }
                String name = args[1];
                if (plugin.getArenaManager().deleteArena(name)) {
                    player.sendMessage(plugin.msg("arena-deleted").replace("{name}", name));
                } else {
                    player.sendMessage(PvPSystem.colorize("&cArena &e" + name + " &cdoes not exist!"));
                }
            }
            case "setspawn1", "setspawn2" -> {
                if (args.length < 2) { player.sendMessage(PvPSystem.colorize("&cUsage: /arena setspawn1/2 <name>")); return true; }
                String name = args[1];
                int num = args[0].endsWith("1") ? 1 : 2;
                Arena arena = plugin.getArenaManager().getArena(name);
                if (arena == null) { player.sendMessage(PvPSystem.colorize("&cArena not found!")); return true; }
                plugin.getArenaManager().setSpawn(name, num, player.getLocation());
                player.sendMessage(plugin.msg("arena-spawn-set").replace("{num}", String.valueOf(num)).replace("{name}", name));
            }
            case "list" -> {
                player.sendMessage(PvPSystem.colorize("&8--- &cArenas &8---"));
                if (plugin.getArenaManager().getAllArenas().isEmpty()) {
                    player.sendMessage(PvPSystem.colorize("&7No arenas found. Create one with /arena create <name>"));
                    return true;
                }
                for (Arena arena : plugin.getArenaManager().getAllArenas()) {
                    String status = arena.isInUse() ? "&cIn Use" : (arena.isReady() ? "&aReady" : "&eNot Ready");
                    player.sendMessage(PvPSystem.colorize("&e" + arena.getName() + " &8- " + status));
                }
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(PvPSystem.colorize("&8--- &cArena Commands &8---"));
        player.sendMessage(PvPSystem.colorize("&e/arena create <name> &8- Create an arena"));
        player.sendMessage(PvPSystem.colorize("&e/arena delete <name> &8- Delete an arena"));
        player.sendMessage(PvPSystem.colorize("&e/arena setspawn1 <name> &8- Set spawn 1"));
        player.sendMessage(PvPSystem.colorize("&e/arena setspawn2 <name> &8- Set spawn 2"));
        player.sendMessage(PvPSystem.colorize("&e/arena list &8- List all arenas"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "setspawn1", "setspawn2", "list")
                    .stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("create")) {
            return plugin.getArenaManager().getAllArenas().stream()
                    .map(Arena::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
