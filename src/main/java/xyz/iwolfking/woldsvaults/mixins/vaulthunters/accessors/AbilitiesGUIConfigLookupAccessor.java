package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.AbilitiesGUIConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = AbilitiesGUIConfig.class, remap = false)
public interface AbilitiesGUIConfigLookupAccessor {
    @Accessor("specializationStyleLookup")
    Map<String, AbilitiesGUIConfig.SpecializationStyle> woldsvaults$getSpecializationStyleLookup();
}
