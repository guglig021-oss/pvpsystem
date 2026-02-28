package com.pvpsystem.rtp;

import com.pvpsystem.PvPSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RTPManager {

    private final PvPSystem plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Random random = new Random();

    public RTPManager(PvPSystem plugin) {
        this.plugin = plugin;
    }

    public void teleport(Player player) {
        if (!player.hasPermission("pvpsystem.rtp")) {
            player.sendMessage(plugin.msg("no-permission"));
            return;
        }

        // Combat check
        if (plugin.getCombatManager().isInCombat(player.getUniqueId())) {
            player.sendMessage(plugin.msg("in-combat"));
            return;
        }

        // Cooldown check
        int cooldownSec = plugin.getConfig().getInt("rtp.cooldown", 30);
        if (cooldowns.containsKey(player.getUniqueId())) {
            long elapsed = (System.currentTimeMillis() - cooldowns.get(player.getUniqueId())) / 1000;
            if (elapsed < cooldownSec) {
                long remaining = cooldownSec - elapsed;
                player.sendMessage(plugin.msg("rtp-cooldown").replace("{time}", String.valueOf(remaining)));
                return;
            }
        }

        player.sendMessage(plugin.msg("rtp-teleporting"));

        String worldName = plugin.getConfig().getString("rtp.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(PvPSystem.colorize("&cRTP world not found: " + worldName));
            return;
        }

        int min = plugin.getConfig().getInt("rtp.min-distance", 100);
        int max = plugin.getConfig().getInt("rtp.max-distance", 5000);
        int maxAttempts = plugin.getConfig().getInt("rtp.max-attempts", 20);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Location safe = findSafeLocation(world, min, max, maxAttempts);
            if (safe == null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(PvPSystem.colorize("&cCould not find a safe location. Try again!"))
                );
                return;
            }

            Location finalSafe = safe;
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(finalSafe);
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                player.sendMessage(plugin.msg("rtp-success")
                        .replace("{x}", String.valueOf(finalSafe.getBlockX()))
                        .replace("{y}", String.valueOf(finalSafe.getBlockY()))
                        .replace("{z}", String.valueOf(finalSafe.getBlockZ())));
            });
        });
    }

    private Location findSafeLocation(World world, int min, int max, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            int range = max - min;
            int x = (random.nextBoolean() ? 1 : -1) * (min + random.nextInt(range));
            int z = (random.nextBoolean() ? 1 : -1) * (min + random.nextInt(range));

            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);

            if (isSafe(loc)) return loc;
        }
        return null;
    }

    private boolean isSafe(Location loc) {
        if (!plugin.getConfig().getBoolean("rtp.safe-height-check", true)) return true;

        Material below = loc.clone().subtract(0, 1, 0).getBlock().getType();
        Material feet = loc.getBlock().getType();
        Material head = loc.clone().add(0, 1, 0).getBlock().getType();

        return below.isSolid()
                && !below.name().contains("LAVA")
                && !below.name().contains("WATER")
                && feet.isAir()
                && head.isAir()
                && loc.getY() > 50;
    }

    public boolean isOnCooldown(UUID uuid) {
        int cooldownSec = plugin.getConfig().getInt("rtp.cooldown", 30);
        if (!cooldowns.containsKey(uuid)) return false;
        long elapsed = (System.currentTimeMillis() - cooldowns.get(uuid)) / 1000;
        return elapsed < cooldownSec;
    }

    public long getRemainingCooldown(UUID uuid) {
        int cooldownSec = plugin.getConfig().getInt("rtp.cooldown", 30);
        if (!cooldowns.containsKey(uuid)) return 0;
        long elapsed = (System.currentTimeMillis() - cooldowns.get(uuid)) / 1000;
        return Math.max(0, cooldownSec - elapsed);
    }
}
