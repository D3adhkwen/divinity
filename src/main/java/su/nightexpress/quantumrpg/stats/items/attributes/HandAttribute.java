package su.nightexpress.quantumrpg.stats.items.attributes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;

public class HandAttribute extends ItemLoreStat<String> {

	private HandAttribute.Type type;
	
	public HandAttribute(
			@NotNull Type type,
			@NotNull String name,
			@NotNull String format
			) {
		super(type.name(), name, format, ItemTags.PLACEHOLDER_ITEM_HAND, ItemTags.TAG_ITEM_HAND, PersistentDataType.STRING);
		this.type = type;
	}
	
	public static enum Type {
		ONE,
		TWO,
		;
	}
	
	@Override
	protected boolean isSingle() {
		return true;
	}
	
	@NotNull
	public HandAttribute.Type getType() {
		return this.type;
	}
	
	@Override
	public boolean add(@NotNull ItemStack item, @NotNull String value, int line) {
		return this.add(item, line);
	}
	
	public boolean add(@NotNull ItemStack item, int line) {
		return super.add(item, this.getType().name(), line);
	}
	
	@Override
	@NotNull
	public String formatValue(@NotNull ItemStack item, @NotNull String value) {
		return value;
	}
}
