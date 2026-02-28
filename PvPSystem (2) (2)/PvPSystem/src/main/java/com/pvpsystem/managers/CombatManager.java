package com.pvpsystem.managers;

import com.pvpsystem.PvPSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {

    private final PvPSystem plugin;
    private final Map<UUID, BukkitTask> combatTasks = new HashMap<>();
    private final Map<UUID, Long> combatTime = new HashMap<>();

    public CombatManager(PvPSystem plugin) {
        this.plugin = plugin;
    }

    public void tag(Player player) {
        int duration = plugin.getConfig().getInt("combat.tag-duration", 15);

        // Cancel existing timer
        if (combatTasks.containsKey(player.getUniqueId())) {
            combatTasks.get(player.getUniqueId()).cancel();
        }

        combatTime.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(PvPSystem.colorize("&c⚔ You are now &lCOMBAT TAGGED &cfor &e" + duration + "s&c!"));

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            untag(player);
        }, duration * 20L);

        combatTasks.put(player.getUniqueId(), task);
    }

    public void untag(Player player) {
        if (!isInCombat(player.getUniqueId())) return;

        combatTime.remove(player.getUniqueId());
        if (combatTasks.containsKey(player.getUniqueId())) {
            combatTasks.get(player.getUniqueId()).cancel();
            combatTasks.remove(player.getUniqueId());
        }

        player.sendMessage(PvPSystem.colorize("&aYou are no longer combat tagged."));
    }

    public boolean isInCombat(UUID uuid) {
        return combatTime.containsKey(uuid);
    }

    public long getRemainingCombat(UUID uuid) {
        if (!isInCombat(uuid)) return 0;
        int duration = plugin.getConfig().getInt("combat.tag-duration", 15);
        long elapsed = (System.currentTimeMillis() - combatTime.get(uuid)) / 1000;
        return Math.max(0, duration - elapsed);
    }
}
