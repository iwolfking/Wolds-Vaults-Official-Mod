package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Level.class, remap = true)
public class MixinBlockEntity {
}
