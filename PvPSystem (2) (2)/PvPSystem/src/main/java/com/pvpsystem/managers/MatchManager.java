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

    public void startMatchWithKit(Player p1, Player p2, ItemStack[] kitContents, ItemStack[] kitArmor) {
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
            p1.sendMessage(plugin.msg("a
