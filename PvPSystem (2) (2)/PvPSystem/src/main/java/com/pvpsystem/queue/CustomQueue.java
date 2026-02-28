package com.pvpsystem.queue;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomQueue {

    private final String id;
    private String displayName;
    private Material icon;
    private int teamSize;       // 1 = 1v1, 2 = 2v2, etc.
    private int maxPlayers;     // teamSize * 2
    private ItemStack[] kitContents;   // 36 slots
    private ItemStack[] kitArmor;      // 4 slots: boots, legs, chest, helmet
    private final LinkedList<UUID> waitingPlayers = new LinkedList<>();
    private boolean enabled;

    public CustomQueue(String id, String displayName, Material icon, int teamSize) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.teamSize = teamSize;
        this.maxPlayers = teamSize * 2;
        this.kitContents = new ItemStack[36];
        this.kitArmor = new ItemStack[4];
        this.enabled = true;
    }

    // --- Queue Operations ---
    public boolean addPlayer(UUID uuid) {
        if (waitingPlayers.contains(uuid)) return false;
        waitingPlayers.add(uuid);
        return true;
    }

    public boolean removePlayer(UUID uuid) {
        return waitingPlayers.remove(uuid);
    }

    public boolean isInQueue(UUID uuid) {
        return waitingPlayers.contains(uuid);
    }

    public int getPosition(UUID uuid) {
        int pos = 1;
        for (UUID id : waitingPlayers) {
            if (id.equals(uuid)) return pos;
            pos++;
        }
        return -1;
    }

    public boolean hasEnoughPlayers() {
        return waitingPlayers.size() >= maxPlayers;
    }

    public List<UUID> pollMatch() {
        List<UUID> group = new ArrayList<>();
        for (int i = 0; i < maxPlayers; i++) {
            if (waitingPlayers.isEmpty()) break;
            group.add(waitingPlayers.poll());
        }
        return group;
    }

    public LinkedList<UUID> getWaitingPlayers() { return waitingPlayers; }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Material getIcon() { return icon; }
    public void setIcon(Material icon) { this.icon = icon; }
    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; this.maxPlayers = teamSize * 2; }
    public int getMaxPlayers() { return maxPlayers; }
    public ItemStack[] getKitContents() { return kitContents; }
    public void setKitContents(ItemStack[] kitContents) { this.kitContents = kitContents; }
    public ItemStack[] getKitArmor() { return kitArmor; }
    public void setKitArmor(ItemStack[] kitArmor) { this.kitArmor = kitArmor; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getSize() { return waitingPlayers.size(); }

    public String getFormattedName() {
        return displayName.replace("&", "§");
    }

    public String getMatchType() {
        return teamSize + "v" + teamSize;
    }
}
