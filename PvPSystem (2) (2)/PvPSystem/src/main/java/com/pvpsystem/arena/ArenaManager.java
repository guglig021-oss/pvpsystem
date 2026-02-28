package com.pvpsystem.arena;

import com.pvpsystem.PvPSystem;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {

    private final PvPSystem plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private File arenaFile;
    private FileConfiguration arenaConfig;

    public ArenaManager(PvPSystem plugin) {
        this.plugin = plugin;
        loadArenas();
    }

    public boolean createArena(String name) {
        if (arenas.containsKey(name.toLowerCase())) return false;
        arenas.put(name.toLowerCase(), new Arena(name));
        saveArenas();
        return true;
    }

    public boolean deleteArena(String name) {
        if (!arenas.containsKey(name.toLowerCase())) return false;
        arenas.remove(name.toLowerCase());
        saveArenas();
        return true;
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public Collection<Arena> getAllArenas() {
        return arenas.values();
    }

    public Arena getAvailableArena() {
        return arenas.values().stream()
                .filter(a -> a.isReady() && !a.isInUse())
                .findFirst()
                .orElse(null);
    }

    public void setSpawn(String name, int num, Location loc) {
        Arena arena = arenas.get(name.toLowerCase());
        if (arena == null) return;
        if (num == 1) arena.setSpawn1(loc);
        else arena.setSpawn2(loc);
        saveArenas();
    }

    public void saveArenas() {
        arenaFile = new File(plugin.getDataFolder(), "arenas.yml");
        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
        arenaConfig.set("arenas", null);

        for (Arena arena : arenas.values()) {
            String path = "arenas." + arena.getName();
            if (arena.getSpawn1() != null) {
                arenaConfig.set(path + ".spawn1", serializeLocation(arena.getSpawn1()));
            }
            if (arena.getSpawn2() != null) {
                arenaConfig.set(path + ".spawn2", serializeLocation(arena.getSpawn2()));
            }
        }

        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save arenas.yml: " + e.getMessage());
        }
    }

    private void loadArenas() {
        arenaFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenaFile.exists()) {
            plugin.getDataFolder().mkdirs();
            return;
        }

        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
        if (!arenaConfig.contains("arenas")) return;

        for (String name : arenaConfig.getConfigurationSection("arenas").getKeys(false)) {
            String path = "arenas." + name;
            Location s1 = null, s2 = null;
            if (arenaConfig.contains(path + ".spawn1"))
                s1 = deserializeLocation(arenaConfig.getString(path + ".spawn1"));
            if (arenaConfig.contains(path + ".spawn2"))
                s2 = deserializeLocation(arenaConfig.getString(path + ".spawn2"));
            arenas.put(name.toLowerCase(), new Arena(name, s1, s2));
        }

        plugin.getLogger().info("Loaded " + arenas.size() + " arenas.");
    }

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ()
                + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private Location deserializeLocation(String s) {
        try {
            String[] parts = s.split(",");
            return new Location(
                    plugin.getServer().getWorld(parts[0]),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]),
                    Float.parseFloat(parts[4]),
                    Float.parseFloat(parts[5])
            );
        } catch (Exception e) {
            return null;
        }
    }
}
