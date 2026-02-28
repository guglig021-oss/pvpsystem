package com.pvpsystem.listeners;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.managers.Match;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final PvPSystem plugin;

    public PlayerListener(PvPSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var uuid = player.getUniqueId();

        // Remove from queue
        plugin.getQueueManager().removeFromQueue(uuid);

        // Handle match
        Match match = plugin.getMatchManager().getMatch(uuid);
        if (match != null && match.getState() != Match.State.ENDED) {
            var opponentUUID = match.getOpponent(uuid);
            plugin.getMatchManager().endMatch(match, opponentUUID);
        }

        // Untag combat
        if (plugin.getCombatManager().isInCombat(uuid)) {
            plugin.getCombatManager().untag(player);
        }
    }
}
