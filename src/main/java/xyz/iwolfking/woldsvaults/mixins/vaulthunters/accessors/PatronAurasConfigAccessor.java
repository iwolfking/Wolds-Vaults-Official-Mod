package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.customisation.PatronAurasConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = PatronAurasConfig.class, remap = false)
public interface PatronAurasConfigAccessor {
    @Accessor("auras")
    Map<String, PatronAurasConfig.Aura> getAuras();
}
