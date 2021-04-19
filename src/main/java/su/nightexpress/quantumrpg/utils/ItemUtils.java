package su.nightexpress.quantumrpg.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.modules.list.identify.IdentifyManager;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;
import su.nightexpress.quantumrpg.stats.items.requirements.api.UserRequirement;
import su.nightexpress.quantumrpg.types.ItemGroup;
import su.nightexpress.quantumrpg.types.ItemSubType;

public class ItemUtils {
	
	private static QuantumRPG plugin = QuantumRPG.getInstance();
	
	public static boolean canUse(@NotNull ItemStack item, @NotNull Player player) {
		return canUse(item, player, true);
	}
	
    public static boolean canUse(@NotNull ItemStack item, @NotNull Player player, boolean msg) {
    	ItemMeta meta = item.getItemMeta();
    	if (meta == null || !meta.hasLore()) return true;
    	
    	DurabilityStat dura = ItemStats.getStat(DurabilityStat.class);
    	if (dura != null && dura.isBroken(item)) {
			if (msg) plugin.lang().Module_Item_Usage_Broken.send(player);
    		return false;
    	}
    	
    	if (!Hooks.isNPC(player)) {
    		for (UserRequirement<?> req : ItemRequirements.getUserRequirements()) {
    			if (!player.hasPermission(req.getBypassPermission()) && !req.canUse(player, item)) {
    				if (msg) req.getDenyMessage(player, item)
	    				.replace("%item%", ItemUT.getItemName(item))
	    				.replace("%player%", player.getName())
	    				.send(player);
    				return false;
    			}
    		}
    	}
		
    	IdentifyManager ide = plugin.getModuleCache().getIdentifyManager();
		if (ide != null) {
			if (ide.isUnidentified(item)) {
				if (msg) plugin.lang().Identify_Usage_Error_Unidentified.send(player);
				return false;
			}
		}
		
    	return true;
    }
	
	@NotNull
	public static EquipmentSlot getEquipmentSlotByItemType(@NotNull ItemStack item) {
		String s = item.getType().name();
		if (s.contains("HELMET") || s.contains("SKULL") || s.contains("HEAD")) {
			return EquipmentSlot.HEAD;
		}
		if (s.contains("CHESTPLATE") || s.contains("ELYTRA")) {
			return EquipmentSlot.CHEST;
		}
		if (s.contains("LEGGINGS")) {
			return EquipmentSlot.LEGS;
		}
		if (s.contains("BOOTS")) {
			return EquipmentSlot.FEET;
		}
		if (item.getType() == Material.SHIELD) {
			return EquipmentSlot.OFF_HAND;
		}
		return EquipmentSlot.HAND;
	}
	
	public static GameProfile getNonPlayerProfile(String hash) {
	    GameProfile profile = new GameProfile(UUID.randomUUID(), null);
	    profile.getProperties().put("textures", new Property("textures", new String(hash)));
	    return profile;
	}
	
	@NotNull
	public static EquipmentSlot[] getItemSlots(@NotNull ItemStack item) {
		if (isArmor(item) || !EngineCfg.ATTRIBUTES_EFFECTIVE_IN_OFFHAND) {
			return new EquipmentSlot[] {getEquipmentSlotByItemType(item)};
		}
		return new EquipmentSlot[] {EquipmentSlot.HAND, EquipmentSlot.OFF_HAND};
	}
	
	// Input: axe, sword, weapon, armor, etc
	// Output: Axe / Weapon / Null
	@Nullable
	public static String getLocalizedGroupName(@NotNull String type) {
		ItemSubType itemSubType = Config.getSubTypeById(type);
		if (itemSubType != null) {
			return itemSubType.getName();
		}
		
		ItemGroup itemGroup = CollectionsUT.getEnum(type, ItemGroup.class);
		if (itemGroup != null) {
			return itemGroup.getName();
		}
		
		Material mat = Material.getMaterial(type);
		if (mat != null) {
			return plugin.lang().getEnum(mat);
		}
		
		return null;
	}
	
	/**
	 * @param id Item material name.
	 * @return ItemSubType name, ItemGroup name, or localized Material name.
	 */
	@NotNull
	public static String getItemGroupNameFor(@NotNull Material id) {
		ItemSubType itemSubType = Config.getItemSubType(id);
		if (itemSubType != null) {
			return itemSubType.getName();
		}
		
		ItemGroup itemGroup = ItemGroup.getItemGroup(id);
		if (itemGroup != null) {
			return itemGroup.getName();
		}
		
		return plugin.lang().getEnum(id);
	}
	
	/**
	 * @param item ItemStack
	 * @return ItemSubType id, ItemGroup id, or raw Material name.
	 */
	@NotNull
	public static String getItemGroupIdFor(@NotNull ItemStack item) {
		ItemSubType ist = Config.getItemSubType(item);
		if (ist != null) {
			return ist.getId();
		}
		
		ItemGroup ig = ItemGroup.getItemGroup(item);
		if (ig != null) {
			return ig.name();
		}
		
		return item.getType().name();
	}
	
	public static boolean compareItemGroup(@NotNull ItemStack item, @NotNull String group) {
		ItemSubType ist = Config.getItemSubType(item);
		if (ist != null && ist.getId().equalsIgnoreCase(group)) {
			return true;
		}
		
		ItemGroup ig = ItemGroup.getItemGroup(item);
		if (ig != null && ig.name().equalsIgnoreCase(group)) {
			return true;
		}
		
		return item.getType().name().equalsIgnoreCase(group);
	}
	
	public static boolean compareItemGroup(@NotNull ItemStack item, @NotNull String[] group) {
		ItemSubType ist = Config.getItemSubType(item);
		if (ist != null && ArrayUtils.contains(group, ist.getId())) {
			return true;
		}
		
		ItemGroup ig = ItemGroup.getItemGroup(item);
		if (ig != null && ArrayUtils.contains(group, ig.name().toLowerCase())) {
			return true;
		}
		
		return ArrayUtils.contains(group, item.getType().name().toLowerCase());
	}

	public static boolean parseItemGroup(@NotNull String group) {
		ItemSubType ist = Config.getSubTypeById(group);
		if (ist != null) return true;
		
		try {
			ItemGroup.valueOf(group.toUpperCase());
			return true;
		}
		catch (IllegalArgumentException ex) {
			
		}
		
		Material m = Material.getMaterial(group.toUpperCase());
		if (m != null) {
			return true;
		}
		
		return false;
	}
	
	public static boolean checkEnchantConflict(@NotNull ItemStack item, @NotNull Enchantment ee) {
		for (Enchantment e2 : item.getEnchantments().keySet()) {
			if (ee.conflictsWith(e2)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isWeapon(@NotNull ItemStack item) {
		return ItemGroup.WEAPON.isItemOfThis(item) || ItemGroup.TOOL.isItemOfThis(item);
	}
	
	public static boolean isArmor(@NotNull ItemStack item) {
		return ItemGroup.ARMOR.isItemOfThis(item);
	}
	
	public static boolean isTool(@NotNull ItemStack item) {
		return ItemGroup.TOOL.isItemOfThis(item);
	}
	
	public static boolean isBow(@NotNull ItemStack item) {
		return item.getType() == Material.BOW || item.getType() == Material.CROSSBOW;
	}
	
	
	
    public static void addFlag(@NotNull ItemStack item, @NotNull ItemFlag f) {
    	ItemMeta meta = item.getItemMeta(); if (meta == null) return;
    	meta.addItemFlags(f);
    	item.setItemMeta(meta);
    }
    
    public static void delFlag(@NotNull ItemStack item, @NotNull ItemFlag f) {
    	ItemMeta meta = item.getItemMeta(); if (meta == null) return;
    	meta.removeItemFlags(f);
    	item.setItemMeta(meta);
    }
    
    public static void setName(@NotNull ItemStack item, @NotNull String name) {
    	ItemMeta meta = item.getItemMeta(); if (meta == null) return;
    	meta.setDisplayName(StringUT.color(name.trim()));
    	item.setItemMeta(meta);
    }
    
    public static void addLoreLine(@NotNull ItemStack item, @NotNull String s, int pos) {
		ItemMeta meta = item.getItemMeta(); if (meta == null) return;
		List<String> lore = meta.getLore(); if (lore == null) lore = new ArrayList<>();
    	
    	s = StringUT.color(s);
    	if (pos > 0 && pos < lore.size()) {
    		lore.add(pos, s);
    	}
    	else { 
    		lore.add(s);
    	}
    	
    	meta.setLore(lore);
    	item.setItemMeta(meta);
    }
    
    public static void delLoreLine(@NotNull ItemStack item, int pos) {
		ItemMeta meta = item.getItemMeta(); if (meta == null) return;
		List<String> lore = meta.getLore(); if (lore == null) return;
    	
    	if (pos >= lore.size() || pos < 0) {
    		pos = lore.size() - 1;
    	}
    	
    	lore.remove(pos);
    	meta.setLore(lore);
    	item.setItemMeta(meta);
    }
    
    public static void delLoreLine(@NotNull ItemStack item, @NotNull String s) {
		ItemMeta meta = item.getItemMeta(); if (meta == null) return;
		List<String> lore = meta.getLore(); if (lore == null) return;
    	
    	int pos = lore.indexOf(StringUT.color(s));
    	if (pos >= 0) {
    		delLoreLine(item, pos);
    	}
    }
    
    public static void clearLore(@NotNull ItemStack item) {
    	ItemMeta meta = item.getItemMeta();
    	if (meta == null || !meta.hasLore()) return;
    	
    	meta.setLore(null);
    	item.setItemMeta(meta);
    }
    
    public static void addEnchant(@NotNull ItemStack item, @NotNull Enchantment e, int lvl) {
    	ItemMeta meta = item.getItemMeta(); if (meta == null) return;
    	
    	if (lvl <= 0) {
    		meta.removeEnchant(e);
    	}
    	else {
    		meta.addEnchant(e, lvl, true);
    	}
    	
    	item.setItemMeta(meta);
    }
    
    public static void addPotionEffect(
    		@NotNull ItemStack item,
    		@NotNull PotionEffectType type, 
    		int lvl, int dur, 
    		boolean ambient, 
    		boolean particles) {
    	
    	ItemMeta meta2 = item.getItemMeta();
    	if (meta2 == null || !(meta2 instanceof PotionMeta)) return;
    	
    	PotionMeta meta = (PotionMeta) meta2;
    	
    	lvl = lvl - 1;
    	if (lvl < 0) {
    		meta.removeCustomEffect(type);
    	}
    	else {
    		meta.addCustomEffect(new PotionEffect(type, dur * 20, lvl, ambient, particles), true);
    	}
    	
    	item.setItemMeta(meta);
    }
    
    public static void setColor(@NotNull ItemStack item, @NotNull Color c) {
    	ItemMeta meta2 = item.getItemMeta();
    	if (meta2 == null) return;
    	
    	if (meta2 instanceof LeatherArmorMeta) {
	     	LeatherArmorMeta meta = (LeatherArmorMeta) meta2;
	    	meta.setColor(c);
    	}
    	else if (meta2 instanceof PotionMeta) {
    		PotionMeta meta = (PotionMeta) meta2;
    		meta.setColor(c);
    	}
    	else return;
    	
    	item.setItemMeta(meta2);
    }
}
