package com.pvpsystem.gui;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.queue.CustomQueue;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class QueueAdminGUI {

    public static final String TITLE = "§8§lQueue Manager";

    private final PvPSystem plugin;
    private final KitEditorGUI kitEditor;

    public QueueAdminGUI(PvPSystem plugin) {
        this.plugin = plugin;
        this.kitEditor = new KitEditorGUI(plugin);
    }

    public void open(Player player) {
        int size = 54;
        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        ItemStack border = QueueSelectorGUI.makeItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < size; i++) inv.setItem(i, border);

        // Place each queue
        int slot = 10;
        for (CustomQueue q : plugin.getCustomQueueManager().getAllQueues()) {
            if (slot > 43) break;
            if (slot % 9 == 0) slot++;

            List<String> lore = Arrays.asList(
                    "",
                    PvPSystem.colorize("&7Mode: &f" + q.getMatchType()),
                    PvPSystem.colorize("&7Players waiting: &e" + q.getSize()),
                    PvPSystem.colorize("&7Status: " + (q.isEnabled() ? "&aEnabled" : "&cDisabled")),
                    "",
                    PvPSystem.colorize("&eLeft-click &7to edit kit"),
                    PvPSystem.colorize("&6Right-click &7to toggle enable/disable"),
                    PvPSystem.colorize("&cShift+Click &7to delete queue")
            );

            inv.setItem(slot, QueueSelectorGUI.makeItem(
                    q.isEnabled() ? q.getIcon() : Material.BARRIER,
                    "&f" + q.getFormattedName() + " &8[" + q.getMatchType() + "]",
                    lore
            ));
            slot++;
            if (slot % 9 == 8) slot += 2;
        }

        // Create new queue button
        inv.setItem(49, QueueSelectorGUI.makeItem(Material.LIME_CONCRETE, "&a&l+ Create New Queue",
                Arrays.asList(
                        "",
                        "&7Use the command:",
                        "&e/queueadmin create <id> <name> <1v1|2v2>",
                        "",
                        "&eExample: &f/queueadmin create diamond &bDiamond &f1v1"
                )));

        // Back button
        inv.setItem(53, QueueSelectorGUI.makeItem(Material.ARROW, "&cBack to Main Menu", null));

        player.openInventory(inv);
    }

    public KitEditorGUI getKitEditor() { return kitEditor; }
}
