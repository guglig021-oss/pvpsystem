package com.pvpsystem.queue;

import com.pvpsystem.PvPSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class QueueManager {

    private final PvPSystem plugin;
    private final LinkedList<UUID> queue = new LinkedList<>();

    public QueueManager(PvPSystem plugin) {
        this.plugin = plugin;
        startQueueChecker();
    }

    public boolean addToQueue(Player player) {
        if (isInQueue(player.getUniqueId())) return false;
        if (plugin.getMatchManager().isInMatch(player.getUniqueId())) return false;
        queue.add(player.getUniqueId());
        updateQueueMessages();
        return true;
    }

    public boolean removeFromQueue(UUID uuid) {
        return queue.remove(uuid);
    }

    public boolean isInQueue(UUID uuid) {
        return queue.contains(uuid);
    }

    public int getPosition(UUID uuid) {
        int pos = 1;
        for (UUID id : queue) {
            if (id.equals(uuid)) return pos;
            pos++;
        }
        return -1;
    }

    public int getSize() {
        return queue.size();
    }

    private void startQueueChecker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            while (queue.size() >= 2) {
                UUID uuid1 = queue.poll();
                UUID uuid2 = queue.poll();

                Player p1 = Bukkit.getPlayer(uuid1);
                Player p2 = Bukkit.getPlayer(uuid2);

                if (p1 == null || !p1.isOnline()) {
                    if (p2 != null && p2.isOnline()) queue.addFirst(uuid2);
                    continue;
                }
                if (p2 == null || !p2.isOnline()) {
                    queue.addFirst(uuid1);
                    continue;
                }

                p1.sendMessage(plugin.msg("queue-match-found"));
                p2.sendMessage(plugin.msg("queue-match-found"));
                plugin.getMatchManager().startMatch(p1, p2);
                updateQueueMessages();
            }
        }, 20L, 20L);
    }

    private void updateQueueMessages() {
        int pos = 1;
        for (UUID uuid : queue) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(PvPSystem.colorize("&8[&cPvP&8] &aQueue position: &e" + pos + " &7/ &e" + queue.size()));
            }
            pos++;
        }
    }

    public List<UUID> getQueue() {
        return Collections.unmodifiableList(queue);
    }
}
