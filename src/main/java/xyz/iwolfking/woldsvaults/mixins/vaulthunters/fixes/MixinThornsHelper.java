package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.util.calc.PlayerStat;
import iskallia.vault.util.calc.ThornsHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fixes the base mod's double count of gear thorns damage.
 *
 * <p>{@code getThornsDamageMultiplier} seeds the player-stat event with the gear sum and then adds
 * the event's result back onto that same sum, so the gear contribution lands twice and every
 * player's thorns reflect (and Shell Quill) has always paid {@code 2 x gear + listeners} instead of
 * {@code gear + listeners}. Its two sibling methods assign the event result rather than adding it,
 * which is the shape this restores. Verified against the shipped 3.21.6 bytecode: the multiplier
 * method is the only one of the three carrying the extra {@code fload}/{@code fadd} pair.
 *
 * <p>The subtraction is what makes the surrounding {@code +=} behave as an assignment, so it is
 * load bearing in the other direction: if the base mod ever fixes this itself, this wrap turns into
 * an under-count and must be deleted rather than kept.
 */
@Mixin(value = ThornsHelper.class, remap = false)
public abstract class MixinThornsHelper {
    @WrapOperation(method = "getThornsDamageMultiplier",
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/core/event/common/PlayerStatEvent;invoke(Liskallia/vault/util/calc/PlayerStat;Lnet/minecraft/world/entity/LivingEntity;F)Liskallia/vault/core/event/common/PlayerStatEvent$Data;"))
    private static PlayerStatEvent.Data woldsvaults$dropEchoedGearThorns(
            PlayerStatEvent instance, PlayerStat stat, LivingEntity entity, float seed,
            Operation<PlayerStatEvent.Data> original) {
        PlayerStatEvent.Data data = original.call(instance, stat, entity, seed);
        data.setValue(data.getValue() - seed);
        return data;
    }
}
