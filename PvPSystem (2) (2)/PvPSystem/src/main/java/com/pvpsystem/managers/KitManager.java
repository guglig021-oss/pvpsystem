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
    private final Map<UUID, String> selectedKits = new HashMap<>();
    public KitManager(PvPSystem plugin) {
        this.plugin = plugin;
        registerDefaultKits();
    }
    private void registerDefaultKits() {
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
                "Classic diamond kit.", diamondContents, diamondArmor));
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
                "Heavy armor kit.", tankContents, tankArmor));
        ItemStack[] speedContents = new ItemStack[36];
        speedContents[0] = enchanted(Material.DIAMOND_SWORD, Map.of(Enchantment.SHARPNESS, 4));
        speedContents[1] = new ItemStack(Material.GOLDEN_APPLE, 2);
        speedContents[2] = new ItemStack(Material.ENDER_PEARL, 6);
        ItemStack[] speedArmor = {
            enchanted(Material.CHAINMAIL_BOOTS, Map.of(Enchantment.PROTECTION, 1, Enchantment.FEATHER_FALLING, 4)),
            enchanted(Material.CHAINMAIL_LEGGINGS, Map.of(Enchantment.PROTECTION, 1)),
            enchanted(Material.CHAINMAIL_CHESTPLATE, Map.of(Enchantment.PROTECTION, 1)),
            enchanted(Material.CHAINMAIL_HELMET, Map.of(Enchantment.PROTECTION, 1))
        };
        kits.add(new Kit("speed", "&e&lSpeed Kit", Material.SUGAR,
                "Fast playstyle kit.", speedContents, speedArmor));
        ItemStack[] archerContents = new ItemStack[36];
        archerContents[0] = enchanted(Material.BOW, Map.of(Enchantment.POWER, 3, Enchantment.PUNCH, 1));
        archerContents[1] = new ItemStack(Material.ARROW, 32);
        archerContents[2] = enchanted(Material.IRON_SWORD, Map.of(Enchantment.SHARPNESS, 2));
        archerContents[3] = new ItemStack(Material.GOLDEN_APPLE, 2);
        ItemStack[] archerArmor = {
            enchanted(Material.LEATHER_BOOTS, Map.of(Enchantment.PROTECTION, 1, Enchantment.FEATHER_FALLING, 3)),
            enchanted(Material.LEATHER_LEGGINGS, Map.of(Enchantment.PROTECTION, 1)),
            enchanted(Material.LEATHER_CHESTPLATE, Map.of(Enchantment.PROTECTION, 1)),
            enchanted(Material.LEATHER_HELMET, Map.of(Enchantment.PROTECTION, 1))
        };
        kits.add(new Kit("archer", "&6&lArcher Kit", Material.BOW,
                "Ranged combat kit.", archerContents, archerArmor));
    }
    public List<Kit> getKits() { return kits; }
    public Kit getKit(String id) {
        return kits.stream().filter(k -> k.getId().equals(id)).findFirst().orElse(null);
    }
    public void selectKit(UUID uuid, String kitId) { selectedKits.put(uuid, kitId); }
    public String getSelectedKit(UUID uuid) { return selectedKits.getOrDefault(uuid, "diamond"); }
    public void giveKit(Player player) {
        applyKit(player);
    }
        String kitId = getSelectedKit(player.getUniqueId());
        Kit kit = getKit(kitId);
        if (kit == null) return;
        player.getInventory().clear();
        player.getInventory().setContents(kit.getContents());
        player.getInventory().setArmorContents(kit.getArmor());
    }
    private ItemStack enchanted(Material mat, Map<Enchantment, Integer> enchants) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            enchants.forEach((e, lvl) -> meta.addEnchant(e, lvl, true));
            item.setItemMeta(meta);
        }
        return item;
    }
}
