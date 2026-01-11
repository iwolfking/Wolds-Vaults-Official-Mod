package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.item.crystal.layout.ClassicSpiralCrystalLayout;
import net.minecraft.world.level.block.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ClassicSpiralCrystalLayout.class, remap = false)
public interface ClassicSpiralCrystalLayoutAccessor {
    @Accessor("halfLength")
    int getHalfLength();

    @Accessor("rotation")
    Rotation getRotation();
}
