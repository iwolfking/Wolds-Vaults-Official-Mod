package xyz.iwolfking.woldsvaults.datagen.lib;

import com.google.gson.JsonObject;
import iskallia.vault.config.card.CardModifiersConfig;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.card.CardNeighborType;
import iskallia.vault.core.card.modifier.card.GearCardModifier;
import iskallia.vault.core.card.modifier.card.GreedCardModifier;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.world.roll.FloatRoll;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.TextComponent;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultConfigDataProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.VaultConfigBuilder;
import xyz.iwolfking.vhapi.api.datagen.lib.WeightedListBuilder;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.CardModifiersConfigAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.GreedCardModifierConfigAccessor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Writes card modifier overlays into {@code vault_configs/card/modifiers}, merged by VHAPI's
 * {@code CardModifiersConfigLoader}. A file whose name contains {@code remove} removes its pool entries from the
 * base pools instead of adding them. VHAPI's own {@code AbstractCardModifiersProvider} cannot be used: its builder
 * never initialises its maps and never writes them into the config.
 */
public abstract class AbstractWoldsCardModifiersProvider extends AbstractVaultConfigDataProvider<AbstractWoldsCardModifiersProvider.Builder> {
    protected AbstractWoldsCardModifiersProvider(DataGenerator generator, String modid) {
        super(generator, modid, "card/modifiers", Builder::new);
    }

    @Override
    public String getName() {
        return modid + " Card Modifiers Data Provider";
    }

    /**
     * A gear-attribute card. Each tier maps to a generator config string, as in the base card config.
     */
    public static <T> CardEntry.Config gearCard(String name, String model, List<String> groups, VaultGearAttribute<T> attribute, int maxTier, Map<Integer, String> tiers) {
        GearCardModifier.Config<T> config = new GearCardModifier.Config<>(attribute, new LinkedHashMap<>(tiers));
        config.maxTier = maxTier;
        return new CardEntry.Config(new TextComponent(name), new LinkedHashSet<>(), new LinkedHashSet<>(groups), model, new GearCardModifier<>(config), null, null);
    }

    /**
     * A greed card that multiplies one neighbour of one colour; tier {@code i + 1} rolls {@code tierRolls[i]}.
     */
    public static CardEntry.Config greedCard(String name, List<String> groups, CardNeighborType neighbor, CardEntry.Color color, float... tierRolls) {
        GreedCardModifier.Config config = new GreedCardModifier.Config();
        GreedCardModifierConfigAccessor access = (GreedCardModifierConfigAccessor) config;
        access.getTargetNeighborTypes().add(neighbor);
        access.setTargetColorFilter(new LinkedHashSet<>(Set.of(color)));
        for (int i = 0; i < tierRolls.length; i++) {
            access.getMultiplierPool().put(i + 1, FloatRoll.ofConstant(tierRolls[i]));
        }
        config.maxTier = tierRolls.length;
        return new CardEntry.Config(new TextComponent(name), new LinkedHashSet<>(), new LinkedHashSet<>(groups), null, new GreedCardModifier(config), null, null);
    }

    /**
     * A float-range generator config string for one gear card tier.
     */
    public static String range(double min, double max, double step) {
        JsonObject json = new JsonObject();
        json.addProperty("min", min);
        json.addProperty("max", max);
        json.addProperty("step", step);
        return json.toString();
    }

    public static class Builder extends VaultConfigBuilder<CardModifiersConfig> {
        private final Map<String, CardEntry.Config> values = new LinkedHashMap<>();
        private final Map<String, WeightedList<String>> pools = new LinkedHashMap<>();

        public Builder() {
            super(CardModifiersConfig::new);
        }

        public Builder value(String key, CardEntry.Config config) {
            values.put(key, config);
            return this;
        }

        public Builder pool(String poolId, Consumer<WeightedListBuilder<String>> consumer) {
            WeightedListBuilder<String> builder = new WeightedListBuilder<>();
            consumer.accept(builder);
            pools.put(poolId, builder.build());
            return this;
        }

        @Override
        protected void configureConfig(CardModifiersConfig config) {
            ((CardModifiersConfigAccessor) config).setValues(values);
            ((CardModifiersConfigAccessor) config).setPools(pools);
        }
    }
}
