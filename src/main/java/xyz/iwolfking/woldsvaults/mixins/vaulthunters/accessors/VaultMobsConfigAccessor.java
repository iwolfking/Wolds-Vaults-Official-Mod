package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.config.VaultMobsConfig;
import iskallia.vault.core.world.data.entity.EntityPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;
import java.util.Map;

@Mixin(value = VaultMobsConfig.class, remap = false)
public interface VaultMobsConfigAccessor {
    @Accessor("ATTRIBUTE_OVERRIDES")
    Map<EntityPredicate, List<VaultMobsConfig.Mob.AttributeOverride>> getAttributeOverrides();
}
