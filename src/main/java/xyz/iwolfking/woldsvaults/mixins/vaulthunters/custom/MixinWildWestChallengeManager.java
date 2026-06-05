package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.block.entity.challenge.WildWestControllerProxyBlockEntity;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.core.vault.challenge.action.ChallengeAction;
import iskallia.vault.core.vault.challenge.action.ForfeitChallengeAction;
import iskallia.vault.core.vault.challenge.action.PoolChallengeAction;
import iskallia.vault.core.vault.challenge.wildwest.WildWestChallengeManager;
import iskallia.vault.init.ModConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

import java.util.Collection;
import java.util.List;

@Mixin(value = WildWestChallengeManager.class, remap = false)
public class MixinWildWestChallengeManager {
    @Shadow
    @Final
    private List<ChallengeAction<?>> pendingRewards;

    @WrapOperation(method = "spawnVictoryProxies", at = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/challenge/WildWestControllerProxyBlockEntity;setActions(Ljava/util/List;)V", ordinal = 1))
    private void whyAreWildWestRoomsSoStupidBroken(WildWestControllerProxyBlockEntity instance, List<ChallengeAction<?>> actions, Operation<Void> original) {
        if(instance.getLevel() != null && GameruleHelper.isEnabled(ModGameRules.VANILLA_WILD_WEST_ROOMS, instance.getLevel())) {
            original.call(instance, actions);
            return;
        }

        ChallengeAction<?> poolAction = ModConfigs.CHALLENGE_ACTIONS.getRandom("positive_wildwest", ChunkRandom.ofNanoTime()).orElse(null);

        if(poolAction instanceof PoolChallengeAction poolChallengeAction) {
            ChallengeAction<?> action = poolChallengeAction.flatten(ChunkRandom.ofNanoTime(), 0).findFirst().orElse(null);

            if(action != null) {
                original.call(instance, List.of(action));
                return;
            }
        }

        original.call(instance, actions);
    }

    @Inject(method = "onVictoryProxyClick", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/challenge/wildwest/WildWestChallengeManager;destroyAllProxies(Lnet/minecraft/server/level/ServerLevel;)V"))
    private void addChallengeActionsFromVictoryProxy(ServerLevel world, ServerPlayer player, BlockPos proxyPos, CallbackInfo ci) {
        if(GameruleHelper.isEnabled(ModGameRules.VANILLA_WILD_WEST_ROOMS, world)) {
            return;
        }


        BlockEntity tile = world.getBlockEntity(proxyPos);
        if(tile instanceof WildWestControllerProxyBlockEntity proxyTileEntity) {
            if(proxyTileEntity.getActions().stream().anyMatch(challengeAction -> challengeAction instanceof ForfeitChallengeAction)) {
                return;
            }

            pendingRewards.addAll(proxyTileEntity.getActions());
        }
    }

    @WrapOperation(method = "lambda$onAttach$5", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private boolean dontDoubleRewards(List<?> instance, Collection<?> es, Operation<Boolean> original, @Local(argsOnly = true) LivingHurtEvent event) {
        if(GameruleHelper.isEnabled(ModGameRules.VANILLA_WILD_WEST_ROOMS, event.getEntity().getLevel())) {
            return original.call(instance, es);
        }

        return false;
    }
}
