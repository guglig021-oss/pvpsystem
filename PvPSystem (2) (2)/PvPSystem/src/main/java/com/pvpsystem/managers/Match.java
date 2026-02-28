package com.pvpsystem.managers;

import com.pvpsystem.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Match {

    public enum State { COUNTDOWN, ACTIVE, ENDED }

    private final UUID player1;
    private final UUID player2;
    private final Arena arena;
    private State state;
    private UUID winner;

    // Saved inventories
    private ItemStack[] inv1;
    private ItemStack[] inv2;
    private ItemStack[] armor1;
    private ItemStack[] armor2;

    public Match(UUID p1, UUID p2, Arena arena) {
        this.player1 = p1;
        this.player2 = p2;
        this.arena = arena;
        this.state = State.COUNTDOWN;
    }

    public UUID getPlayer1() { return player1; }
    public UUID getPlayer2() { return player2; }
    public Arena getArena() { return arena; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public UUID getWinner() { return winner; }
    public void setWinner(UUID winner) { this.winner = winner; }

    public UUID getOpponent(UUID uuid) {
        return uuid.equals(player1) ? player2 : player1;
    }

    public boolean hasPlayer(UUID uuid) {
        return player1.equals(uuid) || player2.equals(uuid);
    }

    public void saveInventory(Player p, ItemStack[] inv, ItemStack[] armor) {
        if (p.getUniqueId().equals(player1)) {
            inv1 = inv; armor1 = armor;
        } else {
            inv2 = inv; armor2 = armor;
        }
    }

    public ItemStack[] getSavedInv(UUID uuid) {
        return uuid.equals(player1) ? inv1 : inv2;
    }

    public ItemStack[] getSavedArmor(UUID uuid) {
        return uuid.equals(player1) ? armor1 : armor2;
    }
}
