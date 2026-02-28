package com.pvpsystem.gui;

import com.pvpsystem.PvPSystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class PvPMenu {

    private final PvPSystem plugin;

    public PvPMenu(PvPSystem plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, PvPSystem.colorize("&8&l⚔ &cPvP &8System &8&l⚔"));

        // Fill borders with black glass
        ItemStack border = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 45; i++) inv.setItem(i, border);

        // === Queue Button ===
        boolean inQueue = plugin.getQueueManager().isInQueue(player.getUniqueId());
        ItemStack queueItem = makeItem(
                inQueue ? Material.RED_CONCRETE : Material.LIME_CONCRETE,
                inQueue ? "&c&lLeave Queue" : "&a&lJoin 1v1 Queue",
                Arrays.asList(
                        inQueue ? "&7Click to leave the queue" : "&7Click to join the matchmaking queue",
                        "",
                        "&8Players in queue: &e" + plugin.getQueueManager().getSize(),
                        inQueue ? "&cPosition: &e" + plugin.getQueueManager().getPosition(player.getUniqueId()) : "",
                        "",
                        inQueue ? "&c&lClick to Leave" : "&a&lClick to Join"
                )
        );
        inv.setItem(11, queueItem);

        // === RTP Button ===
        boolean onCooldown = plugin.getRTPManager().isOnCooldown(player.getUniqueId());
        long remaining = plugin.getRTPManager().getRemainingCooldown(player.getUniqueId());
        ItemStack rtpItem = makeItem(
                onCooldown ? Material.RED_DYE : Material.ENDER_EYE,
                "&b&lRandom Teleport",
                Arrays.asList(
                        "&7Teleport to a random location",
                        "",
                        onCooldown ? "&cCooldown: &e" + remaining + "s remaining" : "&aReady to use!",
                        "",
                        onCooldown ? "&c&lOn Cooldown" : "&b&lClick to Teleport"
                )
        );
        inv.setItem(13, rtpItem);

        // === Stats Button ===
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName(PvPSystem.colorize("&e&l" + player.getName() + "'s Stats"));
            skullMeta.setLore(Arrays.asList(
                    PvPSystem.colorize(""),
                    PvPSystem.colorize("&7Wins: &a" + plugin.getStatsManager().getWins(player.getUniqueId())),
                    PvPSystem.colorize("&7Losses: &c" + plugin.getStatsManager().getLosses(player.getUniqueId())),
                    PvPSystem.colorize("&7K/D Ratio: &e" + plugin.getStatsManager().getKDR(player.getUniqueId())),
                    PvPSystem.colorize(""),
                    PvPSystem.colorize("&8In Match: " + (plugin.getMatchManager().isInMatch(player.getUniqueId()) ? "&cYes" : "&aNo")),
                    PvPSystem.colorize("&8In Queue: " + (plugin.getQueueManager().isInQueue(player.getUniqueId()) ? "&eYes" : "&aNo"))
            ));
            playerHead.setItemMeta(skullMeta);
        }
        inv.setItem(15, playerHead);

        // === Duel Button ===
        ItemStack duelItem = makeItem(
                Material.IRON_SWORD,
                "&e&lDuel a Player",
                Arrays.asList(
                        "&7Challenge a specific player",
                        "&7to a 1v1 match!",
                        "",
                        "&eUsage: &f/duel <player>",
                        "",
                        "&e&lClick for info"
                )
        );
        inv.setItem(29, duelItem);

        // === Arenas Button ===
        ItemStack arenaItem = makeItem(
                Material.BRICK,
                "&d&lArenas",
                Arrays.asList(
                        "&7Available arenas:",
                        "",
                        "&8Total arenas: &e" + plugin.getArenaManager().getAllArenas().size(),
                        "&8Available: &a" + plugin.getArenaManager().getAllArenas().stream().filter(a -> a.isReady() && !a.isInUse()).count(),
                        "",
                        "&d&lClick to view arenas"
                )
        );
        inv.setItem(31, arenaItem);

        // === Combat Status ===
        boolean inCombat = plugin.getCombatManager().isInCombat(player.getUniqueId());
        ItemStack combatItem = makeItem(
                inCombat ? Material.NETHERITE_SWORD : Material.WOODEN_SWORD,
                inCombat ? "&c&l⚔ IN COMBAT" : "&a&l✔ Not in Combat",
                Arrays.asList(
                        "",
                        inCombat ? "&cRemaining: &e" + plugin.getCombatManager().getRemainingCombat(player.getUniqueId()) + "s" : "&aYou are safe!",
                        ""
                )
        );
        inv.setItem(33, combatItem);

        // Close button
        ItemStack closeItem = makeItem(Material.BARRIER, "&c&lClose", Arrays.asList("&7Click to close menu"));
        inv.setItem(40, closeItem);

        player.openInventory(inv);
    }

    public void openArenaList(Player player) {
        int size = Math.min(54, (int) Math.ceil(plugin.getArenaManager().getAllArenas().size() / 9.0 + 1) * 9);
        if (size < 9) size = 27;

        Inventory inv = Bukkit.createInventory(null, size, PvPSystem.colorize("&8&lArenas List"));

        ItemStack border = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < size; i++) inv.setItem(i, border);

        int slot = 10;
        for (var arena : plugin.getArenaManager().getAllArenas()) {
            Material mat = arena.isInUse() ? Material.RED_CONCRETE : (arena.isReady() ? Material.GREEN_CONCRETE : Material.YELLOW_CONCRETE);
            String status = arena.isInUse() ? "&cIn Use" : (arena.isReady() ? "&aAvailable" : "&eNot Ready");

            ItemStack item = makeItem(mat, "&f&l" + arena.getName(), Arrays.asList(
                    "",
                    "&7Status: " + status,
                    "&7Spawn 1: " + (arena.getSpawn1() != null ? "&aSet" : "&cNot Set"),
                    "&7Spawn 2: " + (arena.getSpawn2() != null ? "&aSet" : "&cNot Set"),
                    ""
            ));
            if (slot < size) inv.setItem(slot++, item);
        }

        ItemStack back = makeItem(Material.ARROW, "&cBack", Arrays.asList("&7Return to main menu"));
        inv.setItem(size - 5, back);

        player.openInventory(inv);
    }

    public static ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (name != null) meta.setDisplayName(PvPSystem.colorize(name));
        if (lore != null) {
            lore.replaceAll(PvPSystem::colorize);
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
}
