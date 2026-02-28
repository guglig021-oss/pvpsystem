package com.pvpsystem.managers;
import com.pvpsystem.PvPSystem;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class CombatManager {
    private final PvPSystem plugin;
    private final Map<UUID, BukkitTask> combatTasks = new HashMap<>();
    private final Map<UUID, Long> combatTime = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, BukkitTask> bossBarTasks = new HashMap<>();
    public CombatManager(PvPSystem plugin) {
        this.plugin = plugin;
    }
    public void tag(Player player) {
        int duration = plugin.getConfig().getInt("combat.tag-duration", 15);
        if (combatTasks.containsKey(player.getUniqueId())) {
            combatTasks.get(player.getUniqueId()).cancel();
        }
        combatTime.put(player.getUniqueId(), System.currentTimeMillis());
        showBossBar(player, duration);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> untag(player), duration * 20L);
        combatTasks.put(player.getUniqueId(), task);
    }
    private void showBossBar(Player player, int duration) {
        if (bossBarTasks.containsKey(player.getUniqueId())) {
            bossBarTasks.get(player.getUniqueId()).cancel();
        }
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), k -> {
            BossBar newBar = Bukkit.createBossBar(PvPSystem.colorize("&c⚔ Combat Tagged"), BarColor.RED, BarStyle.SOLID);
            newBar.addPlayer(player);
            return newBar;
        });
        bar.setVisible(true);
        bar.setProgress(1.0);
        final long endTime = System.currentTimeMillis() + (duration * 1000L);
        BukkitTask barTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long remaining = endTime - System.currentTimeMillis();
            if (remaining <= 0) { bar.setProgress(0); bar.setVisible(false); return; }
            bar.setProgress(Math.max(0, Math.min(1, (double) remaining / (duration * 1000L))));
            int secs = (int)(remaining / 1000) + 1;
            bar.setTitle(PvPSystem.colorize((secs <= 5 ? "&e" : "&c") + "⚔ Combat Tagged &7- &f" + secs + "s"));
            bar.setColor(secs <= 5 ? BarColor.YELLOW : BarColor.RED);
        }, 0L, 20L);
        bossBarTasks.put(player.getUniqueId(), barTask);
    }
    public void untag(Player player) {
        if (!isInCombat(player.getUniqueId())) return;
        combatTime.remove(player.getUniqueId());
        if (combatTasks.containsKey(player.getUniqueId())) { combatTasks.get(player.getUniqueId()).cancel(); combatTasks.remove(player.getUniqueId()); }
        if (bossBarTasks.containsKey(player.getUniqueId())) { bossBarTasks.get(player.getUniqueId()).cancel(); bossBarTasks.remove(player.getUniqueId()); }
        if (bossBars.containsKey(player.getUniqueId())) { bossBars.get(player.getUniqueId()).setVisible(false); }
    }
    public void removePlayer(UUID uuid) {
        if (combatTasks.containsKey(uuid)) { combatTasks.get(uuid).cancel(); combatTasks.remove(uuid); }
        if (bossBarTasks.containsKey(uuid)) { bossBarTasks.get(uuid).cancel(); bossBarTasks.remove(uuid); }
        if (bossBars.containsKey(uuid)) { bossBars.get(uuid).setVisible(false); bossBars.remove(uuid); }
        combatTime.remove(uuid);
    }
    public boolean isInCombat(UUID uuid) { return combatTime.containsKey(uuid); }
    public long getRemainingCombat(UUID uuid) {
        if (!isInCombat(uuid)) return 0;
        int duration = plugin.getConfig().getInt("combat.tag-duration", 15);
        long elapsed = (System.currentTimeMillis() - combatTime.get(uuid)) / 1000;
        return Math.max(0, duration - elapsed);
    }
}
