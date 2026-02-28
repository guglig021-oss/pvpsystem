package com.pvpsystem.gui;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.queue.CustomQueue;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KitEditorGUI {

    // Title prefix so we can detect this GUI
    public static final String TITLE_PREFIX = "§8Kit Editor: §e";

    // Track which queue each player is editing
    private static final Map<UUID, String> editing = new HashMap<>();

    private final PvPSystem plugin;

    public KitEditorGUI(PvPSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the kit editor for a queue.
     * Layout (54 slots):
     *   Slots 0-35  = inventory contents (items, weapons, food)
     *   Slots 36-39 = armor (boots, legs, chest, helmet) with labels
     *   Slots 40-44 = info/labels (glass panes, not editable)
     *   Slots 45-53 = bottom bar (save, cancel, clear)
     */
    public void open(Player player, CustomQueue queue) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + queue.getDisplayName().replace("&", "§"));

        // Fill action bar with glass
        ItemStack gray = makeLabel(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 44; i < 54; i++) inv.setItem(i, gray);

        // Load existing kit contents (slots 0-35)
        ItemStack[] existing = queue.getKitContents();
        if (existing != null) {
            for (int i = 0; i < Math.min(36, existing.length); i++) {
                inv.setItem(i, existing[i]);
            }
        }

        // Armor slots (36-39): boots, leggings, chestplate, helmet
        ItemStack[] armor = queue.getKitArmor();
        String[] armorLabels = {"&6Boots slot", "&6Leggings slot", "&6Chestplate slot", "&6Helmet slot"};
        for (int i = 0; i < 4; i++) {
            if (armor != null && armor[i] != null) {
                inv.setItem(36 + i, armor[i]);
            } else {
                inv.setItem(36 + i, makeLabel(Material.LIGHT_GRAY_STAINED_GLASS_PANE, armorLabels[i]));
            }
        }

        // Info label slots 40-43
        inv.setItem(40, makeLabel(Material.BOOK, "&e&lKit Editor &7- " + queue.getMatchType(),
                Arrays.asList(
                        "&7Place items in slots 0-35 for inventory.",
                        "&7Place armor in the 4 armor slots.",
                        "&7Items will NOT drop on death.",
                        "",
                        "&aSlots 0-35 &7= Inventory (weapons, food, etc)",
                        "&6Slots 36-39 &7= Armor (boots→helmet)"
                )));

        // Save button
        inv.setItem(49, makeLabel(Material.LIME_CONCRETE, "&a&lSAVE KIT",
                Arrays.asList("", "&7Click to save the kit for", "&7queue: &e" + queue.getFormattedName(), "")));

        // Clear button
        inv.setItem(47, makeLabel(Material.RED_CONCRETE, "&c&lCLEAR KIT",
                Arrays.asList("", "&7Removes all items from this kit", "")));

        // Cancel button
        inv.setItem(53, makeLabel(Material.BARRIER, "&c&lCANCEL",
                Arrays.asList("", "&7Close without saving", "")));

        editing.put(player.getUniqueId(), queue.getId());
        player.openInventory(inv);
    }

    public static String getEditingQueue(UUID uuid) {
        return editing.get(uuid);
    }

    public static void stopEditing(UUID uuid) {
        editing.remove(uuid);
    }

    public static boolean isEditing(UUID uuid) {
        return editing.containsKey(uuid);
    }

    public static ItemStack makeLabel(Material mat, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(PvPSystem.colorize(name));
        if (lore != null) { lore.replaceAll(PvPSystem::colorize); meta.setLore(lore); }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeLabel(Material mat, String name) {
        return makeLabel(mat, name, null);
    }
}
