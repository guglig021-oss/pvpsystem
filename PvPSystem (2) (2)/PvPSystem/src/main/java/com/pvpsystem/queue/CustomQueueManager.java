package com.pvpsystem.queue;

import com.pvpsystem.PvPSystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomQueueManager {

    private final PvPSystem plugin;
    private final Map<String, CustomQueue> queues = new LinkedHashMap<>();
    private File queueFile;

    public CustomQueueManager(PvPSystem plugin) {
        this.plugin = plugin;
        loadQueues();
        startMatchmaker();
    }

    // ---- CRUD ----

    public boolean createQueue(String id, String displayName, Material icon, int teamSize) {
        if (queues.containsKey(id.toLowerCase())) return false;
        CustomQueue q = new CustomQueue(id.toLowerCase(), displayName, icon, teamSize);
        queues.put(id.toLowerCase(), q);
        saveQueues();
        return true;
    }

    public boolean deleteQueue(String id) {
        if (!queues.containsKey(id.toLowerCase())) return false;
        queues.remove(id.toLowerCase());
        saveQueues();
        return true;
    }

    public CustomQueue getQueue(String id) {
        return queues.get(id.toLowerCase());
    }

    public Collection<CustomQueue> getAllQueues() {
        return queues.values();
    }

    // ---- Player Queue Actions ----

    public CustomQueue getQueueOf(UUID uuid) {
        for (CustomQueue q : queues.values()) {
            if (q.isInQueue(uuid)) return q;
        }
        return null;
    }

    public boolean isInAnyQueue(UUID uuid) {
        return getQueueOf(uuid) != null;
    }

    public boolean joinQueue(Player player, String queueId) {
        CustomQueue q = getQueue(queueId);
        if (q == null || !q.isEnabled()) return false;
        if (isInAnyQueue(player.getUniqueId())) return false;
        if (plugin.getMatchManager().isInMatch(player.getUniqueId())) return false;
        q.addPlayer(player.getUniqueId());
        return true;
    }

    public boolean leaveQueue(Player player) {
        CustomQueue q = getQueueOf(player.getUniqueId());
        if (q == null) return false;
        q.removePlayer(player.getUniqueId());
        return true;
    }

    // ---- Matchmaker ----

    private void startMatchmaker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (CustomQueue q : queues.values()) {
                if (!q.isEnabled()) continue;
                while (q.hasEnoughPlayers()) {
                    List<UUID> matchPlayers = q.pollMatch();
                    // For now support 1v1 (teamSize=1) and 2v2 (teamSize=2)
                    if (q.getTeamSize() == 1 && matchPlayers.size() >= 2) {
                        Player p1 = Bukkit.getPlayer(matchPlayers.get(0));
                        Player p2 = Bukkit.getPlayer(matchPlayers.get(1));

                        if (p1 == null || !p1.isOnline()) { if (p2 != null) q.addPlayer(p2.getUniqueId()); continue; }
                        if (p2 == null || !p2.isOnline()) { q.addPlayer(p1.getUniqueId()); continue; }

                        notifyMatchFound(p1, p2, q);
                        // Use each player's personal kit if saved, else fall back to queue default
                        org.bukkit.inventory.ItemStack[] kit1 = plugin.getPlayerKitManager().hasKit(p1.getUniqueId(), q.getId())
                                ? plugin.getPlayerKitManager().getPlayerContents(p1.getUniqueId(), q.getId()) : q.getKitContents();
                        org.bukkit.inventory.ItemStack[] armor1 = plugin.getPlayerKitManager().hasKit(p1.getUniqueId(), q.getId())
                                ? plugin.getPlayerKitManager().getPlayerArmor(p1.getUniqueId(), q.getId()) : q.getKitArmor();
                        org.bukkit.inventory.ItemStack[] kit2 = plugin.getPlayerKitManager().hasKit(p2.getUniqueId(), q.getId())
                                ? plugin.getPlayerKitManager().getPlayerContents(p2.getUniqueId(), q.getId()) : q.getKitContents();
                        org.bukkit.inventory.ItemStack[] armor2 = plugin.getPlayerKitManager().hasKit(p2.getUniqueId(), q.getId())
                                ? plugin.getPlayerKitManager().getPlayerArmor(p2.getUniqueId(), q.getId()) : q.getKitArmor();
                        plugin.getMatchManager().startMatchWithPersonalKits(p1, p2, kit1, armor1, kit2, armor2);
                        broadcastQueueUpdate(q);
                    }
                }
            }
        }, 20L, 20L);
    }

    private void notifyMatchFound(Player p1, Player p2, CustomQueue q) {
        String msg = PvPSystem.colorize("&8[&cPvP&8] &aMatch found in &e" + q.getFormattedName() + " &a(&e" + q.getMatchType() + "&a)! Teleporting...");
        p1.sendMessage(msg);
        p2.sendMessage(msg);
        p1.sendTitle(PvPSystem.colorize("&a&lMATCH FOUND!"), PvPSystem.colorize("&e" + q.getFormattedName()), 5, 40, 5);
        p2.sendTitle(PvPSystem.colorize("&a&lMATCH FOUND!"), PvPSystem.colorize("&e" + q.getFormattedName()), 5, 40, 5);
    }

    private void broadcastQueueUpdate(CustomQueue q) {
        for (UUID uuid : q.getWaitingPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(PvPSystem.colorize("&8[&cPvP&8] &aQueue position: &e" + q.getPosition(uuid) + " &7/ &e" + q.getSize()));
            }
        }
    }

    // ---- Save / Load ----

    public void saveQueues() {
        queueFile = new File(plugin.getDataFolder(), "queues.yml");
        FileConfiguration cfg = new YamlConfiguration();

        for (CustomQueue q : queues.values()) {
            String path = "queues." + q.getId();
            cfg.set(path + ".displayName", q.getDisplayName());
            cfg.set(path + ".icon", q.getIcon().name());
            cfg.set(path + ".teamSize", q.getTeamSize());
            cfg.set(path + ".enabled", q.isEnabled());

            if (q.getKitContents() != null) cfg.set(path + ".kitContents", q.getKitContents());
            if (q.getKitArmor() != null) cfg.set(path + ".kitArmor", q.getKitArmor());
        }

        try {
            cfg.save(queueFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save queues.yml: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadQueues() {
        queueFile = new File(plugin.getDataFolder(), "queues.yml");
        if (!queueFile.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(queueFile);
        if (!cfg.contains("queues")) return;

        for (String id : cfg.getConfigurationSection("queues").getKeys(false)) {
            String path = "queues." + id;
            String displayName = cfg.getString(path + ".displayName", id);
            String iconName = cfg.getString(path + ".icon", "CHEST");
            Material icon = Material.matchMaterial(iconName);
            if (icon == null) icon = Material.CHEST;
            int teamSize = cfg.getInt(path + ".teamSize", 1);
            boolean enabled = cfg.getBoolean(path + ".enabled", true);

            CustomQueue q = new CustomQueue(id, displayName, icon, teamSize);
            q.setEnabled(enabled);

            if (cfg.contains(path + ".kitContents")) {
                List<ItemStack> contents = (List<ItemStack>) cfg.getList(path + ".kitContents");
                if (contents != null) q.setKitContents(contents.toArray(new ItemStack[36]));
            }
            if (cfg.contains(path + ".kitArmor")) {
                List<ItemStack> armor = (List<ItemStack>) cfg.getList(path + ".kitArmor");
                if (armor != null) q.setKitArmor(armor.toArray(new ItemStack[4]));
            }

            queues.put(id.toLowerCase(), q);
        }

        plugin.getLogger().info("Loaded " + queues.size() + " custom queues.");
    }
}
