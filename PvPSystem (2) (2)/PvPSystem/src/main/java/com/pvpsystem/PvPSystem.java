package com.pvpsystem;

import com.pvpsystem.arena.ArenaManager;
import com.pvpsystem.commands.*;
import com.pvpsystem.gui.GUIListener;
import com.pvpsystem.listeners.*;
import com.pvpsystem.managers.*;
import com.pvpsystem.queue.CustomQueueManager;
import com.pvpsystem.queue.QueueManager;
import com.pvpsystem.rtp.RTPManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PvPSystem extends JavaPlugin {

    private static PvPSystem instance;

    private ArenaManager arenaManager;
    private QueueManager queueManager;
    private CustomQueueManager customQueueManager;
    private RTPManager rtpManager;
    private MatchManager matchManager;
    private CombatManager combatManager;
    private StatsManager statsManager;
    private KitManager kitManager;
    private PlayerKitManager playerKitManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.arenaManager = new ArenaManager(this);
        this.statsManager = new StatsManager(this);
        this.kitManager = new KitManager(this);
        this.playerKitManager = new PlayerKitManager(this);
        this.combatManager = new CombatManager(this);
        this.matchManager = new MatchManager(this);
        this.queueManager = new QueueManager(this);
        this.rtpManager = new RTPManager(this);
        this.customQueueManager = new CustomQueueManager(this);

        getCommand("pvp").setExecutor(new PvPCommand(this));
        getCommand("pvp").setTabCompleter(new PvPCommand(this));
        getCommand("rtp").setExecutor(new RTPCommand(this));
        getCommand("queue").setExecutor(new QueueCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("pvpmenu").setExecutor(new PvPMenuCommand(this));
        getCommand("queueadmin").setExecutor(new QueueAdminCommand(this));
        getCommand("queueadmin").setTabCompleter(new QueueAdminCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new MatchListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyItemListener(this), this);

        getLogger().info("PvPSystem enabled! Full PvP System loaded.");
    }

    @Override
    public void onDisable() {
        if (matchManager != null) matchManager.endAllMatches();
        if (arenaManager != null) arenaManager.saveArenas();
        if (statsManager != null) statsManager.saveAll();
        if (customQueueManager != null) customQueueManager.saveQueues();
        if (playerKitManager != null) playerKitManager.saveAll();
        getLogger().info("PvPSystem disabled.");
    }

    public static PvPSystem getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public QueueManager getQueueManager() { return queueManager; }
    public CustomQueueManager getCustomQueueManager() { return customQueueManager; }
    public RTPManager getRTPManager() { return rtpManager; }
    public MatchManager getMatchManager() { return matchManager; }
    public CombatManager getCombatManager() { return combatManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public KitManager getKitManager() { return kitManager; }
    public PlayerKitManager getPlayerKitManager() { return playerKitManager; }

    public String prefix() {
        return colorize(getConfig().getString("messages.prefix", "&8[&cPvP&8] &r"));
    }

    public String msg(String key) {
        return colorize(prefix() + getConfig().getString("messages." + key, "&cMissing message: " + key));
    }

    public String msgRaw(String key) {
        return colorize(getConfig().getString("messages." + key, "&cMissing: " + key));
    }

    public static String colorize(String s) {
        return s == null ? "" : s.replace("&", "§");
    }
}
