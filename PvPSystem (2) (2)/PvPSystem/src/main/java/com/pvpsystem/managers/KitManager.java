package com.pvpsystem.managers;

import com.pvpsystem.PvPSystem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class KitManager {

    private final PvPSystem plugin;
    private final List<Kit> kits = new ArrayList<>();
    private final Map<UUID, String> selectedKits = new HashMap<>(); // playerUUID -> kitId

    public KitManager(PvPSystem plugin) {
        this.plugin = plugin;
        registerDefaultKits();
    }

    private void registerDefaultKits() {
        // ===== KIT 1: Diamond =====
        ItemStack[] diamondContents = new ItemStack[36];
        diamondContents[0] = enchanted(Material.DIAMOND_SWORD, Map.of(Enchantment.SHARPNESS, 3, Enchantment.UNBREAKING, 3));
        diamondContents[1] = new ItemStack(Material.GOLDEN_APPLE, 3);
        diamondContents[2] = new ItemStack(Material.ENDER_PEARL, 4);
        ItemStack[] diamondArmor = {
            enchanted(Material.DIAMOND_BOOTS, Map.of(Enchantment.PROTECTION, 2, Enchantment.FEATHER_FALLING, 2)),
            enchanted(Material.DIAMOND_LEGGINGS, Map.of(Enchantment.PROTECTION, 2)),
            enchanted(Material.DIAMOND_CHESTPLATE, Map.of(Enchantment.PROTECTION, 2)),
            enchanted(Material.DIAMOND_HELMET, Map.of(Enchantment.PROTECTION, 2))
        };
        kits.add(new Kit("diamond", "&b&lDiamond Kit", Material.DIAMOND_SWORD,
                "Classic diamond kit with Sharpness III sword\nand Protection II armor.",
                diamondContents, diamondArmor));

        // ===== KIT 2: Tank =====
        ItemStack[] tankContents = new ItemStack[36];
        tankContents[0] = enchanted(Material.IRON_SWORD, Map.of(Enchantment.SHARPNESS, 2, Enchantment.KNOCKBACK, 2));
        tankContents[1] = new ItemStack(Material.GOLDEN_APPLE, 6);
        tankContents[2] = new ItemStack(Material.SHIELD, 1);
        ItemStack[] tankArmor = {
            enchanted(Material.NETHERITE_BOOTS, Map.of(Enchantment.PROTECTION, 3)),
            enchanted(Material.NETHERITE_LEGGINGS, Map.of(Enchantment.PROTECTION, 3)),
            enchanted(Material.NETHERITE_CHESTPLATE, Map.of(Enchantment.PROTECTION, 3)),
            enchanted(Material.NETHERITE_HELMET, Map.of(Enchantment.PROTECTION, 3))
        };
        kits.add(new Kit("tank", "&a&lTank Kit", Material.NETHERITE_CHESTPLATE,
                "Heavy armor with Netherite Protection III.\nMore apples, slower but durable.",
                tankContents, tankArmor));

        // ===== KIT 3: Speed =====
        ItemStack[] speedContents = new ItemStack[36];
        speedContents[0] = enchanted(Material.DIAMOND_SWORD, Map.of(Enchantment.SHARPNESS, 4));
        speedContents[1] = new ItemStack(Material.GOLDEN_APPLE, 2);
        speedContents[2] = new ItemStack(Material.ENDER_PEARL, 6);
        speedContents[3] = new ItemStack(Material.SUGAR, 1); // Cosmetic/lore item
        ItemStack[] speedArmor = {
            enchanted(Material.CHAINMAIL_BOOTS, Map.of(Enchantment.PROTECTION, 1, Enchantment.FEATHER_FALLING, 4, Enchantment.SPEED_BOOST, 1)),
            enchanted(Material.CHAINMAIL_LEGGINGS, Map.of(Enchantment.PROTECTION, 1)),
            enchanted(Material.CHAINMAIL_CHESTPLATE, Map.of(Enchantment.PROTECTION, 1)),
            enchanted(Material.CHAINMAIL_HELMET, Map.of(Enchantment.PROTECTION, 1))
        };
        kits.add(new Kit("speed", "&e&lSpeed Kit", Material.SUGAR,
                "Light armor, Sharp IV sword and lots of pearls.\nAggressive and fast playstyle.",
                speedContents, speedArmor));

        // ===== KIT 4: Archer =====
        ItemStack[] archerContents = new ItemStack
