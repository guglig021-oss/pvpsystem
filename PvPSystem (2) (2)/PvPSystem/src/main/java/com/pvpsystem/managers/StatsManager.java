package com.pvpsystem.managers;

import com.pvpsystem.PvPSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final PvPSystem plugin;
    private final Map<UUID, int[]> stats = new HashMap<>(); // [wins, losses]
    private File statsFile;
    private FileConfiguration statsConfig;

    public StatsManager(PvPSystem plugin) {
        this.plugin = plugin;
        loadStats();
    }

    public void addWin(UUID uuid) {
        int[] s = getOrCreate(uuid);
        s[0]++;
    }

    public void addLoss(UUID uuid) {
        int[] s = getOrCreate(uuid);
        s[1]++;
    }

    public int getWins(UUID uuid) {
        return getOrCreate(uuid)[0];
    }

    public int getLosses(UUID uuid) {
        return getOrCreate(uuid)[1];
    }

    public double getKDR(UUID uuid) {
        int wins = getWins(uuid);
        int losses = getLosses(uuid);
        if (losses == 0) return wins;
        return Math.round((double) wins / losses * 100.0) / 100.0;
    }

    private int[] getOrCreate(UUID uuid) {
        return stats.computeIfAbsent(uuid, k -> new int[]{0, 0});
    }

    public void saveAll() {
        statsFile = new File(plugin.getDataFolder(), "stats.yml");
        statsConfig = new YamlConfiguration();
        for (Map.Entry<UUID, int[]> entry : stats.entrySet()) {
            statsConfig.set(entry.getKey().toString() + ".wins", entry.getValue()[0]);
            statsConfig.set(entry.getKey().toString() + ".losses", entry.getValue()[1]);
        }
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save stats: " + e.getMessage());
        }
    }

    private void loadStats() {
        statsFile = new File(plugin.getDataFolder(), "stats.yml");
        if (!statsFile.exists()) return;

        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        for (String key : statsConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int wins = statsConfig.getInt(key + ".wins", 0);
                int losses = statsConfig.getInt(key + ".losses", 0);
                stats.put(uuid, new int[]{wins, losses});
            } catch (Exception ignored) {}
        }
    }
}
