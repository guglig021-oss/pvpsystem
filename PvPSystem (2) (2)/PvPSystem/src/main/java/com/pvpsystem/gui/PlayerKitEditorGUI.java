package com.pvpsystem.gui;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.queue.CustomQueue;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Per-player kit editor.
 * Players open this from the queue selector to customize their loadout for a specific queue.
 * Admins define the base kit; players can swap items freely within the editor.
 */
public class PlayerKitEditorGUI {

    public static final String TITLE_PREFIX = "§8Your Kit: §e";

    // Track which queue each player is editing their kit for
    private static final Map<UUID, String> editing = new HashMap<>();

    private final PvPSystem plugin;

    public PlayerKitEditorGUI(PvPSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the kit editor for a player for a specific queue.
     * Layout (54 slots):
     *   0-35  = inventory contents
     *   36-39 = armor slots (boots, legs, chest, helmet)
     *   40-43 = info labels (non-editable glass)
     *   44-53 = action bar (save, reset to default, clear, cancel)
     */
    public void open(Player player, CustomQueue queue) {
        Inventory inv = Bukkit.createInventory(null, 54,
                TITLE_PREFIX + queue.getDisplayName().replace("&", "§"));

        // Action bar background
        ItemStack gray = label(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 44; i < 54; i++) inv.setItem(i, gray);

        // Load player's saved kit, or fall back to queue default kit
        boolean hasPersonal = plugin.getPlayerKitManager().hasKit(player.getUniqueId(), queue.getId());
        ItemStack[] contents = hasPersonal
                ? plugin.getPlayerKitManager().getPlayerContents(player.getUniqueId(), queue.getId())
                : queue.getKitContents();
        ItemStack[] armor = hasPersonal
                ? plugin.getPlayerKitManager().getPlayerArmor(player.getUniqueId(), queue.getId())
                : queue.getKitArmor();

        // Fill inventory slots (0-35)
        if (contents != null) {
            for (int i = 0; i < Math.min(36, contents.length); i++) {
                inv.setItem(i, contents[i]);
            }
        }

        // Fill armor slots (36-39): boots, legs, chest, helmet
        String[] armorHints = {"§6Boots", "§6Leggings", "§6Chestplate", "§6Helmet"};
        if (armor != null) {
            for (int i = 0; i < 4; i++) {
                if (armor[i] != null) {
                    inv.setItem(36 + i, armor[i]);
                } else {
                    inv.setItem(36 + i, label(Material.LIGHT_GRAY_STAINED_GLASS_PANE, armorHints[i]));
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                inv.setItem(36 + i, label(Material.LIGHT_GRAY_STAINED_GLASS_PANE, armorHints[i]));
            }
        }

        // Info label (slot 40)
        inv.setItem(40, label(Material.BOOK, "&e&lYour Personal Kit",
                Arrays.asList(
                        "&7Queue: &f" + queue.getFormattedName() + " &8(" + queue.getMatchType() + ")",
                        "",
                        "&7Drag items into slots 0-35 for inventory.",
                        "&7Put armor in the 4 armor slots.",
                        "",
                        hasPersonal ? "&aYou have a saved kit!" : "&7Using queue default kit."
                )));

        // Save button (slot 49)
        inv.setItem(49, label(Material.LIME_CONCRETE, "&a&lSAVE MY KIT",
                Arrays.asList("", "&7Saves this kit for &e" + queue.getFormattedName(), "&7You'll get this every match!", "")));

        // Reset to default (slot 47)
        inv.setItem(47, label(Material.YELLOW_CONCRETE, "&e&lRESET TO DEFAULT",
                Arrays.asList("", "&7Resets to the queue's default kit", "&7and deletes your personal kit.", "")));

        // Clear (slot 51)
        inv.setItem(51, label(Material.ORANGE_CONCRETE, "&6&lCLEAR ALL",
                Arrays.asList("", "&7Clears all items from editor", "")));

        // Cancel (slot 53)
        inv.setItem(53, label(Material.BARRIER, "&c&lCANCEL",
                Arrays.asList("", "&7Close without saving", "")));

        editing.put(player.getUniqueId(), queue.getId());
        player.openInventory(inv);
    }

    // ---- Static helpers ----

    public static String getEditingQueue(UUID uuid) { return editing.get(uuid); }
    public static void stopEditing(UUID uuid) { editing.remove(uuid); }
    public static boolean isEditing(UUID uuid) { return editing.containsKey(uuid); }

    public static ItemStack label(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(PvPSystem.colorize(name));
        if (lore != null) { lore.replaceAll(PvPSystem::colorize); meta.setLore(lore); }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack label(Material mat, String name) {
        return label(mat, name, null);
    }
}
