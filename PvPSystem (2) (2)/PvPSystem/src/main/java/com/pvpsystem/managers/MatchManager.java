package com.pvpsystem.managers;

import com.pvpsystem.PvPSystem;
import com.pvpsystem.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class MatchManager {

    private final PvPSystem plugin;
    private final Map<UUID, Match> playerMatches = new HashMap<>();
    private final Map<Match, BukkitTask> countdownTasks = new HashMap<>();

    public MatchManager(PvPSystem plugin) {
        this.plugin = plugin;
    }

    /** Start a match where each player gets their own personal kit */
    public void startMatchWithPersonalKits(Player p1, Player p2,
                                            ItemStack[] kit1, ItemStack[] armor1,
                                            ItemStack[] kit2, ItemStack[] armor2) {
        Arena arena = plugin.getArenaManager().getAvailableArena();
        if (arena == null) {
            p1.sendMessage(plugin.msg("arena-not-ready"));
            p2.sendMessage(plugin.msg("arena-not-ready"));
            return;
        }
        arena.setInUse(true);
        Match match = new Match(p1.getUniqueId(), p2.getUniqueId(), arena);
        if (plugin.getConfig().getBoolean("queue.restore-inventory", true)) {
            match.saveInventory(p1, p1.getInventory().getContents().clone(), p1.getInventory().getArmorContents().clone());
            match.saveInventory(p2, p2.getInventory().getContents().clone(), p2.getInventory().getArmorContents().clone());
        }
        playerMatches.put(p1.getUniqueId(), match);
        playerMatches.put(p2.getUniqueId(), match);

        p1.getInventory().clear();
        if (kit1 != null) p1.getInventory().setContents(kit1);
        if (armor1 != null) p1.getInventory().setArmorContents(armor1);
        p1.setHealth(p1.getMaxHealth()); p1.setFoodLevel(20);

        p2.getInventory().clear();
        if (kit2 != null) p2.getInventory().setContents(kit2);
        if (armor2 != null) p2.getInventory().setArmorContents(armor2);
        p2.setHealth(p2.getMaxHealth()); p2.setFoodLevel(20);

        p1.teleport(arena.getSpawn1()); p2.teleport(arena.getSpawn2());
        p1.setWalkSpeed(0f); p2.setWalkSpeed(0f);

        int countdownSec = plugin.getConfig().getInt("queue.countdown", 5);
        p1.sendMessage(plugin.msg("match-start").replace("{time}", String.valueOf(countdownSec)));
        p2.sendMessage(plugin.msg("match-start").replace("{time}", String.valueOf(countdownSec)));

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int seconds = countdownSec;
            @Override
            public void run() {
                if (!p1.isOnline() || !p2.isOnline()) {
                    if (p1.isOnline()) endMatch(match, p1.getUniqueId());
                    else if (p2.isOnline()) endMatch(match, p2.getUniqueId());
                    return;
                }
                if (seconds > 0) {
                    String m = PvPSystem.colorize("&e&lMatch starts in &c&l" + seconds + "s!");
                    p1.sendMessage(m); p2.sendMessage(m);
                    p1.sendTitle(PvPSystem.colorize("&c&l" + seconds), PvPSystem.colorize("&eGet ready!"), 5, 20, 5);
                    p2.sendTitle(PvPSystem.colorize("&c&l" + seconds), PvPSystem.colorize("&eGet ready!"), 5, 20, 5);
                    seconds--;
                } else {
                    match.setState(Match.State.ACTIVE);
                    p1.setWalkSpeed(0.2f); p2.setWalkSpeed(0.2f);
                    p1.sendTitle(PvPSystem.colorize("&a&lFIGHT!"), PvPSystem.colorize("&eGood luck!"), 5, 30, 5);
                    p2.sendTitle(PvPSystem.colorize("&a&lFIGHT!"), PvPSystem.colorize("&eGood luck!"), 5, 30, 5);
                    countdownTasks.get(match).cancel(); countdownTasks.remove(match);
                }
            }
        }, 0L, 20L);
        countdownTasks.put(match, task);
    }

    public void startMatchWithKit(Player p1, Player p2, org.bukkit.inventory.ItemStack[] kitContents, org.bukkit.inventory.ItemStack[] kitArmor) {
        Arena arena = plugin.getArenaManager().getAvailableArena();
        if (arena == null) {
            p1.sendMessage(plugin.msg("arena-not-ready"));
            p2.sendMessage(plugin.msg("arena-not-ready"));
            return;
        }

        arena.setInUse(true);
        Match match = new Match(p1.getUniqueId(), p2.getUniqueId(), arena);

        if (plugin.getConfig().getBoolean("queue.restore-inventory", true)) {
            match.saveInventory(p1, p1.getInventory().getContents().clone(), p1.getInventory().getArmorContents().clone());
            match.saveInventory(p2, p2.getInventory().getContents().clone(), p2.getInventory().getArmorContents().clone());
        }

        playerMatches.put(p1.getUniqueId(), match);
        playerMatches.put(p2.getUniqueId(), match);

        // Give custom kit
        for (Player p : new Player[]{p1, p2}) {
            p.getInventory().clear();
            if (kitContents != null) p.getInventory().setContents(kitContents);
            if (kitArmor != null) p.getInventory().setArmorContents(kitArmor);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
        }

        p1.teleport(arena.getSpawn1());
        p2.teleport(arena.getSpawn2());
        p1.setWalkSpeed(0f);
        p2.setWalkSpeed(0f);

        int countdownSec = plugin.getConfig().getInt("queue.countdown", 5);
        p1.sendMessage(plugin.msg("match-start").replace("{time}", String.valueOf(countdownSec)));
        p2.sendMessage(plugin.msg("match-start").replace("{time}", String.valueOf(countdownSec)));

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int seconds = countdownSec;
            @Override
            public void run() {
                if (!p1.isOnline() || !p2.isOnline()) {
                    if (p1.isOnline()) endMatch(match, p1.getUniqueId());
                    else if (p2.isOnline()) endMatch(match, p2.getUniqueId());
                    return;
                }
                if (seconds > 0) {
                    String countMsg = PvPSystem.colorize("&e&lMatch starts in &c&l" + seconds + " &e&lseconds!");
                    p1.sendMessage(countMsg); p2.sendMessage(countMsg);
                    p1.sendTitle(PvPSystem.colorize("&c&l" + seconds), PvPSystem.colorize("&eGet ready!"), 5, 20, 5);
                    p2.sendTitle(PvPSystem.colorize("&c&l" + seconds), PvPSystem.colorize("&eGet ready!"), 5, 20, 5);
                    seconds--;
                } else {
                    match.setState(Match.State.ACTIVE);
                    p1.setWalkSpeed(0.2f); p2.setWalkSpeed(0.2f);
                    p1.sendTitle(PvPSystem.colorize("&a&lFIGHT!"), PvPSystem.colorize("&eGood luck!"), 5, 30, 5);
                    p2.sendTitle(PvPSystem.colorize("&a&lFIGHT!"), PvPSystem.colorize("&eGood luck!"), 5, 30, 5);
                    countdownTasks.get(match).cancel();
                    countdownTasks.remove(match);
                }
            }
        }, 0L, 20L);
        countdownTasks.put(match, task);
    }

    public void startMatch(Player p1, Player p2) {
        Arena arena = plugin.getArenaManager().getAvailableArena();
        if (arena == null) {
            p1.sendMessage(plugin.msg("arena-not-ready"));
            p2.sendMessage(plugin.msg("arena-not-ready"));
            return;
        }

        arena.setInUse(true);
        Match match = new Match(p1.getUniqueId(), p2.getUniqueId(), arena);

        // Save inventories
        if (plugin.getConfig().getBoolean("queue.restore-inventory", true)) {
            match.saveInventory(p1, p1.getInventory().getContents().clone(), p1.getInventory().getArmorContents().clone());
            match.saveInventory(p2, p2.getInventory().getContents().clone(), p2.getInventory().getArmorContents().clone());
        }

        playerMatches.put(p1.getUniqueId(), match);
        playerMatches.put(p2.getUniqueId(), match);

        // Clear and kit
        p1.getInventory().clear();
        p2.getInventory().clear();

        if (plugin.getConfig().getBoolean("queue.kit-on-start", true)) {
            plugin.getKitManager().giveKit(p1);
            plugin.getKitManager().giveKit(p2);
        }

        // Teleport
        p1.teleport(arena.getSpawn1());
        p2.teleport(arena.getSpawn2());

        // Freeze
        p1.setWalkSpeed(0f);
        p2.setWalkSpeed(0f);

        // Countdown
        int countdownSec = plugin.getConfig().getInt("queue.countdown", 5);
        p1.sendMessage(plugin.msg("match-start").replace("{time}", String.valueOf(countdownSec)));
        p2.sendMessage(plugin.msg("match-start").replace("{time}", String.valueOf(countdownSec)));

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int seconds = countdownSec;

            @Override
            public void run() {
                if (!p1.isOnline() || !p2.isOnline()) {
                    if (p1.isOnline()) endMatch(match, p1.getUniqueId());
                    else if (p2.isOnline()) endMatch(match, p2.getUniqueId());
                    return;
                }

                if (seconds > 0) {
                    String countMsg = PvPSystem.colorize("&e&lMatch starts in &c&l" + seconds + " &e&lseconds!");
                    p1.sendMessage(countMsg);
                    p2.sendMessage(countMsg);
                    p1.sendTitle(PvPSystem.colorize("&c&l" + seconds), PvPSystem.colorize("&eGet ready!"), 5, 20, 5);
                    p2.sendTitle(PvPSystem.colorize("&c&l" + seconds), PvPSystem.colorize("&eGet ready!"), 5, 20, 5);
                    seconds--;
                } else {
                    match.setState(Match.State.ACTIVE);
                    p1.setWalkSpeed(0.2f);
                    p2.setWalkSpeed(0.2f);
                    p1.sendTitle(PvPSystem.colorize("&a&lFIGHT!"), PvPSystem.colorize("&eGood luck!"), 5, 30, 5);
                    p2.sendTitle(PvPSystem.colorize("&a&lFIGHT!"), PvPSystem.colorize("&eGood luck!"), 5, 30, 5);
                    countdownTasks.get(match).cancel();
                    countdownTasks.remove(match);
                }
            }
        }, 0L, 20L);

        countdownTasks.put(match, task);
    }

    public void endMatch(Match match, UUID winnerUUID) {
        if (match.getState() == Match.State.ENDED) return;
        match.setState(Match.State.ENDED);

        if (countdownTasks.containsKey(match)) {
            countdownTasks.get(match).cancel();
            countdownTasks.remove(match);
        }

        match.setWinner(winnerUUID);
        UUID loserUUID = match.getOpponent(winnerUUID);

        Player winner = Bukkit.getPlayer(winnerUUID);
        Player loser = Bukkit.getPlayer(loserUUID);

        // Stats
        plugin.getStatsManager().addWin(winnerUUID);
        plugin.getStatsManager().addLoss(loserUUID);

        // Messages
        if (winner != null && winner.isOnline()) {
            winner.sendMessage(plugin.msg("match-end-winner"));
            winner.sendTitle(PvPSystem.colorize("&6&lYOU WON!"), PvPSystem.colorize("&eGG!"), 5, 60, 10);
            winner.setWalkSpeed(0.2f);
        }
        if (loser != null && loser.isOnline()) {
            loser.sendMessage(plugin.msg("match-end-loser"));
            loser.sendTitle(PvPSystem.colorize("&c&lYOU LOST"), PvPSystem.colorize("&7Better luck next time!"), 5, 60, 10);
            loser.setWalkSpeed(0.2f);
        }

        // Broadcast
        String winnerName = winner != null ? winner.getName() : Bukkit.getOfflinePlayer(winnerUUID).getName();
        String loserName = loser != null ? loser.getName() : Bukkit.getOfflinePlayer(loserUUID).getName();
        Bukkit.broadcastMessage(PvPSystem.colorize("&8[&cPvP&8] &e" + winnerName + " &abeat &e" + loserName + " &ain a 1v1!"));

        // Restore and teleport after 3 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            restoreAndTeleport(match, winner, winnerUUID);
            restoreAndTeleport(match, loser, loserUUID);
            match.getArena().setInUse(false);
        }, 60L);

        playerMatches.remove(winnerUUID);
        playerMatches.remove(loserUUID);
    }

    private void restoreAndTeleport(Match match, Player p, UUID uuid) {
        if (p == null || !p.isOnline()) return;

        p.getInventory().clear();

        if (plugin.getConfig().getBoolean("queue.restore-inventory", true)) {
            ItemStack[] savedInv = match.getSavedInv(uuid);
            ItemStack[] savedArmor = match.getSavedArmor(uuid);
            if (savedInv != null) p.getInventory().setContents(savedInv);
            if (savedArmor != null) p.getInventory().setArmorContents(savedArmor);
        }

        Location spawn = new Location(
                Bukkit.getWorld(plugin.getConfig().getString("arena.respawn-world", "world")),
                plugin.getConfig().getDouble("arena.respawn-x", 0),
                plugin.getConfig().getDouble("arena.respawn-y", 64),
                plugin.getConfig().getDouble("arena.respawn-z", 0)
        );
        p.teleport(spawn);
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
    }

    public void endAllMatches() {
        for (Match match : new HashSet<>(playerMatches.values())) {
            if (match.getState() != Match.State.ENDED) {
                match.setState(Match.State.ENDED);
                match.getArena().setInUse(false);
            }
        }
    }

    public Match getMatch(UUID uuid) {
        return playerMatches.get(uuid);
    }

    public boolean isInMatch(UUID uuid) {
        return playerMatches.containsKey(uuid);
    }
}
