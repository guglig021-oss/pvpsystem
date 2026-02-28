package com.pvpsystem.listeners;
import com.pvpsystem.PvPSystem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.List;
public class CombatListener implements Listener {
    private final PvPSystem plugin;
    public CombatListener(PvPSystem plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile proj) {
            if (proj.getShooter() instanceof Player) attacker = (Player) proj.getShooter();
        }
        if (attacker == null || attacker.equals(victim)) return;
        if (plugin.getMatchManager().isInMatch(attacker.getUniqueId()) ||
            plugin.getMatchManager().isInMatch(victim.getUniqueId())) return;
        plugin.getCombatManager().tag(attacker);
        plugin.getCombatManager().tag(victim);
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getCombatManager().isInCombat(player.getUniqueId())) return;
        List<String> blockedCommands = plugin.getConfig().getStringList("combat.prevent-commands");
        String cmd = event.getMessage().toLowerCase().split(" ")[0];
        for (String blocked : blockedCommands) {
            if (cmd.equalsIgnoreCase(blocked.toLowerCase())) {
                event.setCancelled(true);
                player.sendMessage(plugin.msg("in-combat"));
                return;
            }
        }
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getCombatManager().removePlayer(event.getPlayer().getUniqueId());
    }
}
