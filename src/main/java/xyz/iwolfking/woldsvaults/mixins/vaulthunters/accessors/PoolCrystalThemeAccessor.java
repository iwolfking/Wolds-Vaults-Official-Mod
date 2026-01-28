package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.item.crystal.theme.PoolCrystalTheme;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PoolCrystalTheme.class, remap = false)
public interface PoolCrystalThemeAccessor {
    @Accessor
    ResourceLocation getId();
}
