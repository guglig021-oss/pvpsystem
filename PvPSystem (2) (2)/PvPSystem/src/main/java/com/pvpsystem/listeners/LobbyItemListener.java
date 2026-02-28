package com.pvpsystem.listeners;
import com.pvpsystem.PvPSystem;
import com.pvpsystem.gui.QueueSelectorGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
public class LobbyItemListener implements Listener {
    private final PvPSystem plugin;
    private final QueueSelectorGUI queueSelector;
    public LobbyItemListener(PvPSystem plugin) {
        this.plugin = plugin;
        this.queueSelector = new QueueSelectorGUI(plugin);
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        giveLobbyItem(event.getPlayer());
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getMatchManager().isInMatch(player.getUniqueId())) {
            giveLobbyItem(player);
        }
    }
    public void giveLobbyItem(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isQueueCompass(item)) return;
        }
        player.getInventory().setItem(0, createQueueCompass());
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !isQueueCompass(item)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        event.setCancelled(true);
        if (plugin.getCustomQueueManager().getAllQueues().isEmpty()) {
            player.sendMessage(PvPSystem.colorize("&cNo queues have been created yet! Ask an admin to create one with /queueadmin create"));
            return;
        }
        queueSelector.open(player);
    }
    private ItemStack createQueueCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(PvPSystem.colorize("&6&l⚔ Queue Selector"));
            meta.setLore(Arrays.asList(
                    PvPSystem.colorize(""),
                    PvPSystem.colorize("&7Right-click to open"),
                    PvPSystem.colorize("&7the queue selector GUI!"),
                    PvPSystem.colorize(""),
                    PvPSystem.colorize("&8pvpsystem:queue_compass")
            ));
            compass.setItemMeta(meta);
        }
        return compass;
    }
    private boolean isQueueCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        if (!item.hasItemMeta() || item.getItemMeta() == null) return false;
        if (item.getItemMeta().getLore() == null) return false;
        return item.getItemMeta().getLore().contains(PvPSystem.colorize("&8pvpsystem:queue_compass"));
    }
}
