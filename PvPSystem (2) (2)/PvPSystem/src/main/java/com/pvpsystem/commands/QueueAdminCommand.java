package com.pvpsystem.commands;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.gui.KitEditorGUI;
import com.pvpsystem.gui.QueueAdminGUI;
import com.pvpsystem.queue.CustomQueue;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QueueAdminCommand implements CommandExecutor, TabCompleter {

    private final PvPSystem plugin;
    private final QueueAdminGUI adminGUI;
    private final KitEditorGUI kitEditor;

    public QueueAdminCommand(PvPSystem plugin) {
        this.plugin = plugin;
        this.adminGUI = new QueueAdminGUI(plugin);
        this.kitEditor = new KitEditorGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this!");
            return true;
        }

        if (!player.hasPermission("pvpsystem.admin")) {
            player.sendMessage(plugin.msg("no-permission"));
            return true;
        }

        if (args.length == 0) {
            adminGUI.open(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "create" -> {
                // /queueadmin create <id> <displayName> <1v1|2v2|3v3>
                if (args.length < 4) {
                    player.sendMessage(PvPSystem.colorize("&cUsage: /queueadmin create <id> <displayName> <1v1|2v2|3v3>"));
                    player.sendMessage(PvPSystem.colorize("&eExample: /queueadmin create diamond &bDiamond &f1v1"));
                    return true;
                }

                String id = args[1];
                String mode = args[args.length - 1]; // last arg is mode
                // Display name = everything between id and mode
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 2; i < args.length - 1; i++) {
                    if (i > 2) nameBuilder.append(" ");
                    nameBuilder.append(args[i]);
                }
                String displayName = nameBuilder.toString();
                if (displayName.isEmpty()) displayName = id;

                int teamSize = parseTeamSize(mode);
                if (teamSize == -1) {
                    player.sendMessage(PvPSystem.colorize("&cInvalid mode! Use: 1v1, 2v2, 3v3, 4v4, etc."));
                    return true;
                }

                boolean created = plugin.getCustomQueueManager().createQueue(id, displayName, Material.CHEST, teamSize);
                if (created) {
                    player.sendMessage(PvPSystem.colorize("&aQueue &e" + displayName + " &a(&f" + teamSize + "v" + teamSize + "&a) created!"));
                    player.sendMessage(PvPSystem.colorize("&7Now edit its kit with: &e/queueadmin editkit " + id));
                } else {
                    player.sendMessage(PvPSystem.colorize("&cA queue with id &e" + id + " &calready exists!"));
                }
            }

            case "delete" -> {
                if (args.length < 2) { player.sendMessage(PvPSystem.colorize("&cUsage: /queueadmin delete <id>")); return true; }
                String id = args[1];
                if (plugin.getCustomQueueManager().deleteQueue(id)) {
                    player.sendMessage(PvPSystem.colorize("&cQueue &e" + id + " &cdeleted."));
                } else {
                    player.sendMessage(PvPSystem.colorize("&cQueue not found: &e" + id));
                }
            }

            case "editkit" -> {
                if (args.length < 2) { player.sendMessage(PvPSystem.colorize("&cUsage: /queueadmin editkit <id>")); return true; }
                CustomQueue q = plugin.getCustomQueueManager().getQueue(args[1]);
                if (q == null) { player.sendMessage(PvPSystem.colorize("&cQueue not found!")); return true; }
                kitEditor.open(player, q);
                player.sendMessage(PvPSystem.colorize("&aOpened kit editor for &e" + q.getFormattedName()));
            }

            case "seticon" -> {
                if (args.length < 3) { player.sendMessage(PvPSystem.colorize("&cUsage: /queueadmin seticon <id> <MATERIAL>")); return true; }
                CustomQueue q = plugin.getCustomQueueManager().getQueue(args[1]);
                if (q == null) { player.sendMessage(PvPSystem.colorize("&cQueue not found!")); return true; }
                Material mat = Material.matchMaterial(args[2].toUpperCase());
                if (mat == null) { player.sendMessage(PvPSystem.colorize("&cInvalid material: &e" + args[2])); return true; }
                q.setIcon(mat);
                plugin.getCustomQueueManager().saveQueues();
                player.sendMessage(PvPSystem.colorize("&aIcon set to &e" + mat.name() + " &afor queue &e" + q.getFormattedName()));
            }

            case "toggle" -> {
                if (args.length < 2) { player.sendMessage(PvPSystem.colorize("&cUsage: /queueadmin toggle <id>")); return true; }
                CustomQueue q = plugin.getCustomQueueManager().getQueue(args[1]);
                if (q == null) { player.sendMessage(PvPSystem.colorize("&cQueue not found!")); return true; }
                q.setEnabled(!q.isEnabled());
                plugin.getCustomQueueManager().saveQueues();
                player.sendMessage(PvPSystem.colorize("&aQueue &e" + q.getFormattedName() + " &ais now: " + (q.isEnabled() ? "&aEnabled" : "&cDisabled")));
            }

            case "list" -> {
                player.sendMessage(PvPSystem.colorize("&8--- &cQueues &8---"));
                if (plugin.getCustomQueueManager().getAllQueues().isEmpty()) {
                    player.sendMessage(PvPSystem.colorize("&7No queues. Create one with /queueadmin create <id> <name> <1v1>"));
                    return true;
                }
                for (CustomQueue q : plugin.getCustomQueueManager().getAllQueues()) {
                    player.sendMessage(PvPSystem.colorize(
                            "&e" + q.getId() + " &8| &f" + q.getFormattedName() +
                            " &8| &7" + q.getMatchType() +
                            " &8| " + (q.isEnabled() ? "&aEnabled" : "&cDisabled") +
                            " &8| &7Waiting: &e" + q.getSize()
                    ));
                }
            }

            case "gui" -> adminGUI.open(player);

            default -> {
                player.sendMessage(PvPSystem.colorize("&8--- &c/queueadmin &8---"));
                player.sendMessage(PvPSystem.colorize("&e/queueadmin &7- Open queue manager GUI"));
                player.sendMessage(PvPSystem.colorize("&e/queueadmin create <id> <name> <1v1|2v2> &7- Create queue"));
                player.sendMessage(PvPSystem.colorize("&e/queueadmin delete <id> &7- Delete queue"));
                player.sendMessage(PvPSystem.colorize("&e/queueadmin editkit <id> &7- Edit queue kit in GUI"));
                player.sendMessage(PvPSystem.colorize("&e/queueadmin seticon <id> <MATERIAL> &7- Set queue icon"));
                player.sendMessage(PvPSystem.colorize("&e/queueadmin toggle <id> &7- Enable/disable queue"));
                player.sendMessage(PvPSystem.colorize("&e/queueadmin list &7- List all queues"));
            }
        }

        return true;
    }

    private int parseTeamSize(String mode) {
        try {
            // Accept "1v1", "2v2", "3v3", or just "1", "2"
            if (mode.contains("v")) {
                return Integer.parseInt(mode.split("v")[0]);
            }
            return Integer.parseInt(mode);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "editkit", "seticon", "toggle", "list", "gui")
                    .stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("create")) {
            return plugin.getCustomQueueManager().getAllQueues().stream()
                    .map(q -> q.getId())
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            return Arrays.asList("1v1", "2v2", "3v3", "4v4");
        }
        return List.of();
    }
}
