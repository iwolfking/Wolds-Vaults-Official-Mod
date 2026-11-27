package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.card.CardModifiersConfig;
import iskallia.vault.core.card.CardEntry;
import iskallia.vault.core.util.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = CardModifiersConfig.class, remap = false)
public interface CardModifiersConfigAccessor {
    @Accessor("values")
    void setValues(Map<String, CardEntry.Config> values);

    @Accessor("pools")
    void setPools(Map<String, WeightedList<String>> pools);
}
