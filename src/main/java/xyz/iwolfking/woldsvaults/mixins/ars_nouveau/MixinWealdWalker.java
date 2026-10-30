package xyz.iwolfking.woldsvaults.mixins.ars_nouveau;

import com.hollingsworth.arsnouveau.api.client.ITooltipProvider;
import com.hollingsworth.arsnouveau.api.item.IWandable;
import com.hollingsworth.arsnouveau.common.block.tile.IAnimationListener;
import com.hollingsworth.arsnouveau.common.entity.WealdWalker;
import iskallia.vault.world.data.ServerVaults;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.ars_nouveau.geckolib3.core.IAnimatable;

import javax.annotation.Nullable;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "ars_nouveau")
    }
)
@Mixin(WealdWalker.class)
public abstract class MixinWealdWalker extends AgeableMob implements IAnimatable, IAnimationListener, RangedAttackMob, IWandable, ITooltipProvider {

    protected MixinWealdWalker(EntityType<? extends AgeableMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"), remap = true)
    private void adjustTargetingGoals(CallbackInfo ci) {
        WealdWalker walker = (WealdWalker) (Object) this;

        walker.targetSelector.getAvailableGoals().removeIf(prioritizedGoal -> 
            prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal
        );

        walker.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
            walker,
            Player.class,
            10,
            true,
            false,
            (target) -> {
                if (!(target instanceof Player player)) return false;
                if (player.isCreative() || player.isSpectator()) return false;
                return ServerVaults.get(walker.level).isPresent();
            }
        ));

        walker.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
            walker, 
            Mob.class, 
            false, 
            (entity) -> entity instanceof Enemy && ServerVaults.get(walker.level).isEmpty()
        ));
    }

    @Inject(method = "tick", at = @At("TAIL"), remap = true)
    private void reduceAttackCooldownsInVault(CallbackInfo ci) {
        WealdWalker walker = (WealdWalker) (Object) this;

        if (!walker.level.isClientSide && ServerVaults.get(walker.level).isPresent()) {
            if (walker.smashCooldown > 0) {
                walker.smashCooldown--;
            }
            if (walker.castCooldown > 0) {
                walker.castCooldown--;
            }
        }
    }

    @Inject(method = "onFinishedConnectionFirst", at = @At("HEAD"), cancellable = true, remap = false)
    private void preventWandUsageInVault(
        @Nullable BlockPos storedPos,
        @Nullable LivingEntity storedEntity,
        Player playerEntity,
        CallbackInfo ci
    ) {
        WealdWalker walker = (WealdWalker) (Object) this;

        if (ServerVaults.get(walker.level).isPresent()) {
            ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void preventBaby(DamageSource source, CallbackInfo ci) {
        WealdWalker walker = (WealdWalker) (Object) this;
        if (!walker.level.isClientSide && ServerVaults.get(walker.level).isPresent()) {
            super.die(source);
            ci.cancel();
        }
    }
}