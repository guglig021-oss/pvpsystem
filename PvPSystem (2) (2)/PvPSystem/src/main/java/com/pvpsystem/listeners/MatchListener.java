package com.pvpsystem.listeners;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.managers.Match;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class MatchListener implements Listener {

    private final PvPSystem plugin;

    public MatchListener(PvPSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        var player = event.getEntity();
        var uuid = player.getUniqueId();

        Match match = plugin.getMatchManager().getMatch(uuid);
        if (match == null || match.getState() != Match.State.ACTIVE) return;

        // Suppress death message in match
        event.setDeathMessage(null);

        // Drop no items
        event.getDrops().clear();
        event.setDroppedExp(0);

        // End match - opponent wins
        var winnerUUID = match.getOpponent(uuid);
        plugin.getMatchManager().endMatch(match, winnerUUID);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        // Players respawn at the config respawn location if it was set
        // This is handled in MatchManager's restoreAndTeleport
    }
}
