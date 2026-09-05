package xyz.iwolfking.woldsvaults.mixins.occultism;

import com.github.alexthe666.alexsmobs.entity.EntityCosmaw;
import com.github.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity;
import com.github.klikli_dev.occultism.common.entity.spirit.AfritEntity;
import com.github.klikli_dev.occultism.common.entity.spirit.AfritWildEntity;
import com.github.klikli_dev.occultism.common.ritual.CraftRitual;
import com.github.klikli_dev.occultism.common.ritual.Ritual;
import com.github.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import iskallia.vault.world.data.ServerVaults;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.integration.occultism.lib.DynamicResultRitualRecipe;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "occultism")
        }
)
@Mixin(value = AfritWildEntity.class, remap = false)
public abstract class MixinAfritWildEntity extends Monster {

    protected MixinAfritWildEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "finalizeSpawn", at = @At("HEAD"), cancellable = true, remap = true)
  private void cancelBlazeSpawnsInVaults(ServerLevelAccessor level, DifficultyInstance difficultyIn, MobSpawnType reason, SpawnGroupData spawnDataIn, CompoundTag dataTag, CallbackInfoReturnable<SpawnGroupData> cir) {
      if(ServerVaults.get(level.getLevel()).isPresent()) {
          cir.setReturnValue(super.finalizeSpawn(level, difficultyIn, reason, spawnDataIn, dataTag));
      }
  }
}