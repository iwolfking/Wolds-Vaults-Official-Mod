package xyz.iwolfking.woldsvaults.datagen.lib;

import iskallia.vault.config.UniqueGearConfig;
import iskallia.vault.core.util.WeightedList;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultConfigDataProvider;
import xyz.iwolfking.vhapi.api.datagen.lib.VaultConfigBuilder;
import xyz.iwolfking.vhapi.mixin.accessors.UniqueGearConfigAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.UniqueGearConfigEntryAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Writes unique gear registry entries into {@code vault_configs/gear/unique_gear} with no pools, so VHAPI's
 * {@code UniqueGearConfigLoader} replaces those base entries by id and changes nothing else. VHAPI's
 * {@code AbstractUniqueGearProvider} cannot do this: every entry it writes also adds pool weight, a codex page,
 * models, model rolls and a discovery hint.
 */
public abstract class AbstractUniqueGearOverridesProvider extends AbstractVaultConfigDataProvider<AbstractUniqueGearOverridesProvider.Builder> {
    protected AbstractUniqueGearOverridesProvider(DataGenerator generator, String modid) {
        super(generator, modid, "gear/unique_gear", Builder::new);
    }

    @Override
    public String getName() {
        return modid + " Unique Gear Overrides Data Provider";
    }

    public static class Builder extends VaultConfigBuilder<UniqueGearConfig> {
        private final Map<ResourceLocation, UniqueGearConfig.Entry> entries = new LinkedHashMap<>();

        public Builder() {
            super(UniqueGearConfig::new);
        }

        public Builder entry(String id, String name, Item item, Consumer<EntryBuilder> consumer) {
            EntryBuilder builder = new EntryBuilder();
            consumer.accept(builder);
            UniqueGearConfig.Entry entry = new UniqueGearConfig.Entry(name, builder.models);
            UniqueGearConfigEntryAccessor access = (UniqueGearConfigEntryAccessor) entry;
            access.setItem(item);
            access.setLore(builder.lore);
            access.setModifierIdentifiers(builder.identifiers);
            entries.put(ResourceLocation.parse(id), entry);
            return this;
        }

        @Override
        protected void configureConfig(UniqueGearConfig config) {
            ((UniqueGearConfigAccessor) config).setEntries(entries);
            ((UniqueGearConfigAccessor) config).setPools(new LinkedHashMap<>());
        }
    }

    public static class EntryBuilder {
        private final WeightedList<ResourceLocation> models = new WeightedList<>();
        private final Map<UniqueGearConfig.AffixTargetType, List<ResourceLocation>> identifiers = new LinkedHashMap<>();
        private String lore;

        private EntryBuilder() {
            for (UniqueGearConfig.AffixTargetType type : UniqueGearConfig.AffixTargetType.values()) {
                identifiers.put(type, new ArrayList<>());
            }
        }

        public EntryBuilder model(String model) {
            models.add(ResourceLocation.parse(model), 1);
            return this;
        }

        public EntryBuilder base(String... ids) {
            return add(UniqueGearConfig.AffixTargetType.BASE_ATTRIBUTE, ids);
        }

        public EntryBuilder implicit(String... ids) {
            return add(UniqueGearConfig.AffixTargetType.IMPLICIT, ids);
        }

        public EntryBuilder prefix(String... ids) {
            return add(UniqueGearConfig.AffixTargetType.PREFIX, ids);
        }

        public EntryBuilder suffix(String... ids) {
            return add(UniqueGearConfig.AffixTargetType.SUFFIX, ids);
        }

        public EntryBuilder lore(String lore) {
            this.lore = lore;
            return this;
        }

        private EntryBuilder add(UniqueGearConfig.AffixTargetType type, String... ids) {
            Arrays.stream(ids).map(ResourceLocation::parse).forEach(identifiers.get(type)::add);
            return this;
        }
    }
}
