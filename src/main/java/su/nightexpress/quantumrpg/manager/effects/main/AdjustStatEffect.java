package su.nightexpress.quantumrpg.manager.effects.main;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import su.nightexpress.quantumrpg.manager.effects.IEffectType;
import su.nightexpress.quantumrpg.manager.effects.IExpirableEffect;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;

public class AdjustStatEffect extends IExpirableEffect {

	private Map<? extends ItemLoreStat<?>, DoubleUnaryOperator> functions;
	private Map<ItemLoreStat<?>, Integer> count;
	
	private AdjustStatEffect(@NotNull Builder builder) {
		super(builder);
		this.functions = new HashMap<>(builder.functions);
		this.count = new HashMap<>();
	}

	@Nullable
	public DoubleUnaryOperator getAdjust(@NotNull ItemLoreStat<?> stat, boolean safe) {
		DoubleUnaryOperator operator = this.functions.get(stat);
		if (operator == null) return null;
		
		// Check how many times we got the adjust effect compare to effect charges
		// so we remove that stat if adjust effect triggered more than charges amount aka expired.
		if (!safe) {
			int count = this.count.compute(stat, (k, v) -> this.count.computeIfAbsent(stat, v2 -> 0) + 1);
			if (count >= this.getCharges()) {
				this.count.remove(stat);
				this.functions.remove(stat);
			}
		}
		return operator;
	}
	
	@NotNull
	public Set<? extends ItemLoreStat<?>> getAdjustedStats() {
		return this.functions.keySet();
	}
	
	public boolean isAdjusted(@NotNull ItemLoreStat<?> stat) {
		return this.functions.containsKey(stat);
	}

	@Override
	public boolean onTrigger(boolean force) {
		return this.functions.isEmpty();
	}

	@Override
	public void onClear() {
		if (this.functions != null) {
			this.functions.clear();
			this.functions = null;
		}
	}

	@Override
	public boolean resetOnDeath() {
		return true;
	}

	@Override
	@NotNull
	public IEffectType getType() {
		return IEffectType.ADJUST_STAT;
	}
	
	public static class Builder extends IExpirableEffect.Builder<Builder> {

		private Map<ItemLoreStat<?>, DoubleUnaryOperator> functions;
		
		public Builder(double lifeTime) {
			super(lifeTime);
			this.functions = new HashMap<>();
		}
		
		@SuppressWarnings("unchecked")
		@NotNull
		public Builder withAdjust(@NotNull ItemLoreStat<?> stat, @NotNull DoubleUnaryOperator operator) {
			return this.withAdjust(Sets.newHashSet(stat), operator);
		}
		
		@NotNull
		public Builder withAdjust(@NotNull Collection<? extends ItemLoreStat<?>> stats, @NotNull DoubleUnaryOperator operator) {
			stats.forEach(stat -> this.functions.put(stat, operator));
			return this.self();
		}

		@Override
		@NotNull
		public AdjustStatEffect build() {
			return new AdjustStatEffect(this);
		}

		@Override
		@NotNull
		protected Builder self() {
			return this;
		}
	}
}
