package xyz.iwolfking.woldsvaults.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.integration.dungeon_mobs.DungeonMobsBellHighlighting;

@Mixin(BellBlockEntity.class)
public class MixinBellBlockEntity {
    @Shadow private int resonationTicks;

    @WrapOperation(method = "isRaiderWithinRange", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean highlightAllRaiders(EntityType<?> entityType, TagKey<EntityType<?>> tag, Operation<Boolean> original,
                                               @Local(argsOnly = true) LivingEntity entity){
        if (entity instanceof Vex vex && vex.getOwner() == null) {
            return true; // only vexes not owned by evoker
        }

        if (DungeonMobsBellHighlighting.shouldHighlight(entityType)) {
            return true;
        }
        return original.call(entityType, tag);
    }
}
