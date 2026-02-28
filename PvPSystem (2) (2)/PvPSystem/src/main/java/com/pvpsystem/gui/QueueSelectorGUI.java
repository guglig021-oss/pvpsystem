package com.pvpsystem.gui;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.queue.CustomQueue;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueueSelectorGUI {

    public static final String TITLE = "§8§l⚔ §cSelect a Queue";

    private final PvPSystem plugin;
    private final PlayerKitEditorGUI kitEditor;

    public QueueSelectorGUI(PvPSystem plugin) {
        this.plugin = plugin;
        this.kitEditor = new PlayerKitEditorGUI(plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        ItemStack border = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, border);

        boolean inQueue = plugin.getCustomQueueManager().isInAnyQueue(player.getUniqueId());
        CustomQueue myQueue = plugin.getCustomQueueManager().getQueueOf(player.getUniqueId());

        // Each queue gets 2 adjacent slots: [join][kit]
        int[] joinSlots = {10, 13, 16, 19, 22, 25, 28, 31, 34, 37, 40, 43};
        int[] kitSlots  = {11, 14, 17, 20, 23, 26, 29, 32, 35, 38, 41, 44};

        int idx = 0;
        for (CustomQueue q : plugin.getCustomQueueManager().getAllQueues()) {
            if (!q.isEnabled() || idx >= joinSlots.length) continue;

            boolean isMyQueue = myQueue != null && myQueue.getId().equals(q.getId());
            boolean hasPersonalKit = plugin.getPlayerKitManager().hasKit(player.getUniqueId(), q.getId());

            // JOIN button
            List<String> joinLore = new ArrayList<>();
            joinLore.add("");
            joinLore.add(PvPSystem.colorize("&7Mode: &f" + q.getMatchType()));
            joinLore.add(PvPSystem.colorize("&7Waiting: &e" + q.getSize() + " players"));
            if (isMyQueue) joinLore.add(PvPSystem.colorize("&7Your position: &e" + q.getPosition(player.getUniqueId())));
            joinLore.add("");
            joinLore.add(PvPSystem.colorize(isMyQueue ? "&c&lClick to Leave" : (inQueue ? "&7Already in a queue" : "&a&lClick to Join")));
            Material joinIcon = isMyQueue ? Material.RED_CONCRETE : (q.getIcon() != null ? q.getIcon() : Material.CHEST);
            inv.setItem(joinSlots[idx], makeItem(joinIcon,
                    (isMyQueue ? "&c" : "&f") + q.getFormattedName() + " &8[" + q.getMatchType() + "]", joinLore));

            // EDIT KIT button
            inv.setItem(kitSlots[idx], makeItem(
                    hasPersonalKit ? Material.ENCHANTED_BOOK : Material.BOOK,
                    "&e&lEdit My Kit &8[" + q.getId() + "]",
                    Arrays.asList("",
                            PvPSystem.colorize(hasPersonalKit ? "&aCustom kit saved!" : "&7Using default kit"),
                            "", PvPSystem.colorize("&eClick to edit your kit"), "")));
            idx++;
        }

        if (inQueue) inv.setItem(49, makeItem(Material.RED_CONCRETE, "&c&lLeave Queue", Arrays.asList("", "&7Leave your current queue")));
        inv.setItem(53, makeItem(Material.BARRIER, "&c&lClose", null));
        player.openInventory(inv);
    }

    public PlayerKitEditorGUI getKitEditor() { return kitEditor; }

    public static ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(PvPSystem.colorize(name));
        if (lore != null) { lore.replaceAll(PvPSystem::colorize); meta.setLore(lore); }
        item.setItemMeta(meta);
        return item;
    }
}
