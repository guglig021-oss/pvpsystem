package com.pvpsystem.managers;

import com.pvpsystem.PvPSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelManager {

    private final PvPSystem plugin;
    // Maps sender -> target
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();
    private final Map<UUID, BukkitTask> expiryTasks = new HashMap<>();

    public DuelManager(PvPSystem plugin) {
        this.plugin = plugin;
    }

    public void sendRequest(Player sender, Player target) {
        pendingRequests.put(sender.getUniqueId(), target.getUniqueId());

        if (expiryTasks.containsKey(sender.getUniqueId())) {
            expiryTasks.get(sender.getUniqueId()).cancel();
        }

        sender.sendMessage(plugin.msg("duel-sent").replace("{player}", target.getName()));
        target.sendMessage(plugin.msg("duel-received").replace("{player}", sender.getName()));

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingRequests.remove(sender.getUniqueId());
            expiryTasks.remove(sender.getUniqueId());
            if (sender.isOnline())
                sender.sendMessage(plugin.msg("duel-expired").replace("{player}", target.getName()));
        }, 30 * 20L);

        expiryTasks.put(sender.getUniqueId(), task);
    }

    public UUID getPendingRequest(UUID target) {
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(target)) return entry.getKey();
        }
        return null;
    }

    public boolean acceptRequest(Player target) {
        UUID senderUUID = getPendingRequest(target.getUniqueId());
        if (senderUUID == null) return false;

        Player sender = Bukkit.getPlayer(senderUUID);
        clearRequest(senderUUID);

        if (sender == null || !sender.isOnline()) return false;

        target.sendMessage(plugin.msg("duel-accepted"));
        sender.sendMessage(plugin.msg("duel-accepted"));

        plugin.getMatchManager().startMatch(sender, target);
        return true;
    }

    public boolean denyRequest(Player target) {
        UUID senderUUID = getPendingRequest(target.getUniqueId());
        if (senderUUID == null) return false;

        Player sender = Bukkit.getPlayer(senderUUID);
        clearRequest(senderUUID);

        if (sender != null && sender.isOnline()) {
            sender.sendMessage(plugin.msg("duel-denied").replace("{player}", target.getName()));
        }
        return true;
    }

    private void clearRequest(UUID uuid) {
        pendingRequests.remove(uuid);
        if (expiryTasks.containsKey(uuid)) {
            expiryTasks.get(uuid).cancel();
            expiryTasks.remove(uuid);
        }
    }
}
