package com.pvpsystem.managers;

import com.pvpsystem.PvPSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Stores each player's personal kit per queue.
 * Key: UUID + ":" + queueId  →  Value: [contents (36), armor (4)]
 * Saved to plugins/PvPSystem/player_kits.yml
 */
public class PlayerKitManager {

    private final PvPSystem plugin;
    // Map< uuid:queueId , ItemStack[2] > where [0]=contents[36], [1]=armor[4]
    private final Map<String, ItemStack[][]> kits = new HashMap<>();
    private File kitFile;

    public PlayerKitManager(PvPSystem plugin) {
        this.plugin = plugin;
        loadAll();
    }

    // ---- Public API ----

    /** Save a player's kit for a specific queue */
    public void savePlayerKit(UUID uuid, String queueId, ItemStack[] contents, ItemStack[] armor) {
        String key = uuid.toString() + ":" + queueId.toLowerCase();
        kits.put(key, new ItemStack[][]{contents, armor});
        saveAll();
    }

    /** Get a player's kit contents for a queue, or null if not set */
    public ItemStack[] getPlayerContents(UUID uuid, String queueId) {
        String key = uuid.toString() + ":" + queueId.toLowerCase();
        ItemStack[][] kit = kits.get(key);
        return kit != null ? kit[0] : null;
    }

    /** Get a player's armor for a queue, or null if not set */
    public ItemStack[] getPlayerArmor(UUID uuid, String queueId) {
        String key = uuid.toString() + ":" + queueId.toLowerCase();
        ItemStack[][] kit = kits.get(key);
        return kit != null ? kit[1] : null;
    }

    /** Returns true if the player has a personal kit saved for this queue */
    public boolean hasKit(UUID uuid, String queueId) {
        return kits.containsKey(uuid.toString() + ":" + queueId.toLowerCase());
    }

    /** Delete a player's kit for a queue */
    public void deleteKit(UUID uuid, String queueId) {
        kits.remove(uuid.toString() + ":" + queueId.toLowerCase());
        saveAll();
    }

    // ---- Persistence ----

    @SuppressWarnings("unchecked")
    public void loadAll() {
        kitFile = new File(plugin.getDataFolder(), "player_kits.yml");
        if (!kitFile.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(kitFile);
        if (!cfg.contains("kits")) return;

        for (String key : cfg.getConfigurationSection("kits").getKeys(false)) {
            try {
                List<ItemStack> contents = (List<ItemStack>) cfg.getList("kits." + key + ".contents");
                List<ItemStack> armor    = (List<ItemStack>) cfg.getList("kits." + key + ".armor");

                ItemStack[] c = contents != null ? contents.toArray(new ItemStack[36]) : new ItemStack[36];
                ItemStack[] a = armor    != null ? armor.toArray(new ItemStack[4])    : new ItemStack[4];

                kits.put(key, new ItemStack[][]{c, a});
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load kit for key: " + key);
            }
        }

        plugin.getLogger().info("Loaded " + kits.size() + " player kits.");
    }

    public void saveAll() {
        kitFile = new File(plugin.getDataFolder(), "player_kits.yml");
        FileConfiguration cfg = new YamlConfiguration();

        for (Map.Entry<String, ItemStack[][]> entry : kits.entrySet()) {
            String key = entry.getKey();
            ItemStack[][] kit = entry.getValue();
            if (kit[0] != null) cfg.set("kits." + key + ".contents", Arrays.asList(kit[0]));
            if (kit[1] != null) cfg.set("kits." + key + ".armor",    Arrays.asList(kit[1]));
        }

        try {
            cfg.save(kitFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player_kits.yml: " + e.getMessage());
        }
    }
}
