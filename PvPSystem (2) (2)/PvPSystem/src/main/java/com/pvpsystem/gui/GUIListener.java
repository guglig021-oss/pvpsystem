package com.pvpsystem.gui;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.queue.CustomQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final PvPSystem plugin;
    private final PvPMenu menu;
    private final QueueSelectorGUI queueSelector;
    private final QueueAdminGUI queueAdmin;
    private final KitEditorGUI adminKitEditor;

    public GUIListener(PvPSystem plugin) {
        this.plugin = plugin;
        this.menu = new PvPMenu(plugin);
        this.queueSelector = new QueueSelectorGUI(plugin);
        this.queueAdmin = new QueueAdminGUI(plugin);
        this.adminKitEditor = new KitEditorGUI(plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        // ===================== MAIN PVP MENU =====================
        if (title.equals(PvPSystem.colorize("&8&l⚔ &cPvP &8System &8&l⚔"))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            switch (event.getSlot()) {
                case 11 -> { player.closeInventory(); queueSelector.open(player); }
                case 13 -> { player.closeInventory(); plugin.getRTPManager().teleport(player); }
                case 15 -> { player.closeInventory(); menu.open(player); }
                case 29 -> { player.sendMessage(PvPSystem.colorize("&8[&cPvP&8] &eUse &f/duel <player>")); player.closeInventory(); }
                case 31 -> { player.closeInventory(); menu.openArenaList(player); }
                case 40 -> player.closeInventory();
            }
        }

        // ===================== QUEUE SELECTOR =====================
        else if (title.equals(QueueSelectorGUI.TITLE)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            String itemName = event.getCurrentItem().getItemMeta() != null
                    ? event.getCurrentItem().getItemMeta().getDisplayName() : "";

            // Close / Leave buttons
            if (itemName.equals(PvPSystem.colorize("&c&lClose"))) { player.closeInventory(); return; }
            if (itemName.equals(PvPSystem.colorize("&c&lLeave Queue"))) {
                plugin.getCustomQueueManager().leaveQueue(player);
                player.sendMessage(plugin.msg("queue-left"));
                player.closeInventory(); queueSelector.open(player); return;
            }

            // Edit My Kit button — name format: "&e&lEdit My Kit &8[queueId]"
            if (itemName.startsWith(PvPSystem.colorize("&e&lEdit My Kit"))) {
                String queueId = extractBracket(itemName);
                if (queueId != null) {
                    CustomQueue q = plugin.getCustomQueueManager().getQueue(queueId);
                    if (q != null) {
                        player.closeInventory();
                        queueSelector.getKitEditor().open(player, q);
                    }
                }
                return;
            }

            // Join/Leave queue buttons
            for (CustomQueue q : plugin.getCustomQueueManager().getAllQueues()) {
                String joined = PvPSystem.colorize("&f" + q.getFormattedName() + " &8[" + q.getMatchType() + "]");
                String leaving = PvPSystem.colorize("&c" + q.getFormattedName() + " &8[" + q.getMatchType() + "]");
                if (itemName.equals(joined) || itemName.equals(leaving)) {
                    if (plugin.getCustomQueueManager().isInAnyQueue(player.getUniqueId())) {
                        plugin.getCustomQueueManager().leaveQueue(player);
                        player.sendMessage(plugin.msg("queue-left"));
                    } else {
                        if (plugin.getMatchManager().isInMatch(player.getUniqueId())) {
                            player.sendMessage(PvPSystem.colorize("&cYou are already in a match!")); return;
                        }
                        boolean joined2 = plugin.getCustomQueueManager().joinQueue(player, q.getId());
                        if (joined2) {
                            player.sendMessage(PvPSystem.colorize("&aJoined &e" + q.getFormattedName()
                                    + " &a(" + q.getMatchType() + ")! Position: &e" + q.getPosition(player.getUniqueId())));
                        }
                    }
                    player.closeInventory(); return;
                }
            }
        }

        // ===================== ARENA LIST =====================
        else if (title.equals(PvPSystem.colorize("&8&lArenas List"))) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            String itemName = event.getCurrentItem().getItemMeta() != null ?
                    event.getCurrentItem().getItemMeta().getDisplayName() : "";
            if (itemName.equals(PvPSystem.colorize("&cBack"))) { player.closeInventory(); menu.open(player); }
        }

        // ===================== QUEUE ADMIN GUI =====================
        else if (title.equals(QueueAdminGUI.TITLE)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            String itemName = event.getCurrentItem().getItemMeta() != null ?
                    event.getCurrentItem().getItemMeta().getDisplayName() : "";
            if (itemName.equals(PvPSystem.colorize("&cBack to Main Menu"))) { player.closeInventory(); menu.open(player); return; }

            for (CustomQueue q : plugin.getCustomQueueManager().getAllQueues()) {
                String qName = PvPSystem.colorize("&f" + q.getFormattedName() + " &8[" + q.getMatchType() + "]");
                if (itemName.equals(qName)) {
                    ClickType click = event.getClick();
                    if (click == ClickType.LEFT) {
                        player.closeInventory(); adminKitEditor.open(player, q);
                    } else if (click == ClickType.RIGHT) {
                        q.setEnabled(!q.isEnabled());
                        plugin.getCustomQueueManager().saveQueues();
                        player.sendMessage(PvPSystem.colorize("&aQueue " + q.getFormattedName() + ": " + (q.isEnabled() ? "&aEnabled" : "&cDisabled")));
                        player.closeInventory(); queueAdmin.open(player);
                    } else if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
                        plugin.getCustomQueueManager().deleteQueue(q.getId());
                        player.sendMessage(PvPSystem.colorize("&cQueue &e" + q.getFormattedName() + " &cdeleted."));
                        player.closeInventory(); queueAdmin.open(player);
                    }
                    return;
                }
            }
        }

        // ===================== ADMIN KIT EDITOR =====================
        else if (title.startsWith(KitEditorGUI.TITLE_PREFIX)) {
            int slot = event.getSlot();
            if (slot >= 44) {
                event.setCancelled(true);
                if (event.getCurrentItem() == null) return;
                String itemName = event.getCurrentItem().getItemMeta() != null ?
                        event.getCurrentItem().getItemMeta().getDisplayName() : "";
                if (itemName.equals(PvPSystem.colorize("&a&lSAVE KIT"))) saveAdminKit(player);
                else if (itemName.equals(PvPSystem.colorize("&c&lCLEAR KIT"))) clearEditor(player, 36);
                else if (itemName.equals(PvPSystem.colorize("&c&lCANCEL"))) {
                    KitEditorGUI.stopEditing(player.getUniqueId());
                    player.closeInventory(); queueAdmin.open(player);
                }
            }
        }

        // ===================== PLAYER KIT EDITOR =====================
        else if (title.startsWith(PlayerKitEditorGUI.TITLE_PREFIX)) {
            int slot = event.getSlot();
            if (slot >= 44) {
                event.setCancelled(true);
                if (event.getCurrentItem() == null) return;
                String itemName = event.getCurrentItem().getItemMeta() != null ?
                        event.getCurrentItem().getItemMeta().getDisplayName() : "";
                if (itemName.equals(PvPSystem.colorize("&a&lSAVE MY KIT"))) savePlayerKit(player);
                else if (itemName.equals(PvPSystem.colorize("&e&lRESET TO DEFAULT"))) resetPlayerKit(player);
                else if (itemName.equals(PvPSystem.colorize("&6&lCLEAR ALL"))) clearEditor(player, 40);
                else if (itemName.equals(PvPSystem.colorize("&c&lCANCEL"))) {
                    PlayerKitEditorGUI.stopEditing(player.getUniqueId());
                    player.closeInventory(); queueSelector.open(player);
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        KitEditorGUI.stopEditing(player.getUniqueId());
        PlayerKitEditorGUI.stopEditing(player.getUniqueId());
    }

    // ---- Save helpers ----

    private void saveAdminKit(Player player) {
        String queueId = KitEditorGUI.getEditingQueue(player.getUniqueId());
        if (queueId == null) return;
        CustomQueue q = plugin.getCustomQueueManager().getQueue(queueId);
        if (q == null) return;
        ItemStack[] contents = new ItemStack[36];
        for (int i = 0; i < 36; i++) contents[i] = player.getOpenInventory().getTopInventory().getItem(i);
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            ItemStack item = player.getOpenInventory().getTopInventory().getItem(36 + i);
            if (item != null && item.getType().name().contains("STAINED_GLASS_PANE")) item = null;
            armor[i] = item;
        }
        q.setKitContents(contents);
        q.setKitArmor(armor);
        plugin.getCustomQueueManager().saveQueues();
        KitEditorGUI.stopEditing(player.getUniqueId());
        player.sendMessage(PvPSystem.colorize("&a✔ Default kit saved for queue &e" + q.getFormattedName()));
        player.closeInventory(); queueAdmin.open(player);
    }

    private void savePlayerKit(Player player) {
        String queueId = PlayerKitEditorGUI.getEditingQueue(player.getUniqueId());
        if (queueId == null) return;
        CustomQueue q = plugin.getCustomQueueManager().getQueue(queueId);
        if (q == null) return;
        ItemStack[] contents = new ItemStack[36];
        for (int i = 0; i < 36; i++) contents[i] = player.getOpenInventory().getTopInventory().getItem(i);
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            ItemStack item = player.getOpenInventory().getTopInventory().getItem(36 + i);
            if (item != null && item.getType().name().contains("STAINED_GLASS_PANE")) item = null;
            armor[i] = item;
        }
        plugin.getPlayerKitManager().savePlayerKit(player.getUniqueId(), queueId, contents, armor);
        PlayerKitEditorGUI.stopEditing(player.getUniqueId());
        player.sendMessage(PvPSystem.colorize("&a✔ Your personal kit for &e" + q.getFormattedName() + " &ahas been saved!"));
        player.closeInventory(); queueSelector.open(player);
    }

    private void resetPlayerKit(Player player) {
        String queueId = PlayerKitEditorGUI.getEditingQueue(player.getUniqueId());
        if (queueId == null) return;
        plugin.getPlayerKitManager().deleteKit(player.getUniqueId(), queueId);
        PlayerKitEditorGUI.stopEditing(player.getUniqueId());
        player.sendMessage(PvPSystem.colorize("&eYour personal kit has been reset to the queue default."));
        player.closeInventory(); queueSelector.open(player);
    }

    private void clearEditor(Player player, int slotsToKeep) {
        for (int i = 0; i < slotsToKeep; i++) {
            player.getOpenInventory().getTopInventory().setItem(i, null);
        }
    }

    private String extractBracket(String s) {
        int start = s.lastIndexOf('[');
        int end = s.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) return null;
        return s.substring(start + 1, end);
    }
}
