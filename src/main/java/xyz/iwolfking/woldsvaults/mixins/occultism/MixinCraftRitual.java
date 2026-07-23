package xyz.iwolfking.woldsvaults.mixins.occultism;

import com.github.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity;
import com.github.klikli_dev.occultism.common.ritual.CraftRitual;
import com.github.klikli_dev.occultism.common.ritual.Ritual;
import com.github.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.integration.occultism.lib.DynamicResultRitualRecipe;

@Mixin(value = CraftRitual.class, remap = false)
public abstract class MixinCraftRitual extends Ritual {

    public MixinCraftRitual(RitualRecipe recipe) {
        super(recipe);
    }

    @Inject(
        method = "finish",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onFinish(Level level, BlockPos goldenBowlPosition, GoldenSacrificialBowlBlockEntity blockEntity, Player castingPlayer, ItemStack activationItem, CallbackInfo ci) {
        if (this.recipe instanceof DynamicResultRitualRecipe dynamicRecipe) {
            super.finish(level, goldenBowlPosition, blockEntity, castingPlayer, activationItem);

            ItemStack result = dynamicRecipe.getResultItem(activationItem);

            activationItem.shrink(1);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    goldenBowlPosition.getX() + 0.5D,
                    goldenBowlPosition.getY() + 0.5D,
                    goldenBowlPosition.getZ() + 0.5D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D
                );
            }

            this.dropResult(level, goldenBowlPosition, blockEntity, castingPlayer, result);

            ci.cancel();
        }
    }
}