package com.pvpsystem.managers;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class Kit {

    private final String id;
    private final String displayName;
    private final Material icon;
    private final String description;
    private final ItemStack[] contents;  // full 36-slot inventory
    private final ItemStack[] armor;     // helmet, chestplate, leggings, boots

    public Kit(String id, String displayName, Material icon, String description,
               ItemStack[] contents, ItemStack[] armor) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.contents = contents;
        this.armor = armor;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public String getDescription() { return description; }
    public ItemStack[] getContents() { return contents; }
    public ItemStack[] getArmor() { return armor; }

    // Helper to create enchanted item
    public static ItemStack enchanted(Material mat, Map<Enchantment, Integer> enchants) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            enchants.forEach((e, lvl) -> meta.addEnchant(e, lvl, true));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack named(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name.replace("&", "§"));
            if (lore != null) {
                lore.replaceAll(s -> s.replace("&", "§"));
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
