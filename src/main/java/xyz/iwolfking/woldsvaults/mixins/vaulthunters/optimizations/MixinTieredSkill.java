package xyz.iwolfking.woldsvaults.mixins.vaulthunters.optimizations;

import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.base.TieredSkill;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.gods.trees.tenos.TenosAbilityCaps;

/**
 * Enforces the god-node gate on ability levels past an ability's base cap ({@link TenosAbilityCaps}) at
 * {@code updateBonusTier} - the single writer of a bonus tier, where the clamp may go negative to pull a
 * learned tier down - and at {@code canLearn}. Both no-op without a server player; nothing is written to disk.
 */
@Mixin(value = TieredSkill.class, remap = false)
public abstract class MixinTieredSkill {
    @Shadow
    protected abstract void updateBonusTier(int bonusTier, SkillContext context);

    /**
     * Re-dispatches once with the clamped value rather than editing the field, so the base mod's own transition
     * still runs; the clamp is idempotent.
     */
    @Inject(method = "updateBonusTier(ILiskallia/vault/skill/base/SkillContext;)V", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$capGodGatedBonusTier(int bonusTier, SkillContext context, CallbackInfo ci) {
        TieredSkill self = (TieredSkill) (Object) this;
        String id = self.getId();
        if (!TenosAbilityCaps.isGated(id)) {
            return;
        }
        ServerPlayer player = woldsvaults$serverPlayer(context);
        if (player == null) {
            return;
        }
        int clamped = Math.min(bonusTier, TenosAbilityCaps.cap(id, player) - self.getUnmodifiedTier());
        if (clamped != bonusTier) {
            ci.cancel();
            this.updateBonusTier(clamped, context);
        }
    }

    @Inject(method = "canLearn(Liskallia/vault/skill/base/SkillContext;)Z", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$gateLearnAtGodCap(SkillContext context, CallbackInfoReturnable<Boolean> cir) {
        if (woldsvaults$isAtGodGatedCap(context)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canLearn(Liskallia/vault/skill/base/SkillContext;Z)Z", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$gateLearnAtGodCapIgnoringMax(SkillContext context, boolean ignoreMaxTier, CallbackInfoReturnable<Boolean> cir) {
        if (woldsvaults$isAtGodGatedCap(context)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean woldsvaults$isAtGodGatedCap(SkillContext context) {
        TieredSkill self = (TieredSkill) (Object) this;
        String id = self.getId();
        if (!TenosAbilityCaps.isGated(id)) {
            return false;
        }
        ServerPlayer player = woldsvaults$serverPlayer(context);
        if (player == null) {
            return false;
        }
        return self.getUnmodifiedTier() >= TenosAbilityCaps.cap(id, player);
    }

    @Unique
    private ServerPlayer woldsvaults$serverPlayer(SkillContext context) {
        if (context == null) {
            return null;
        }
        return context.getSource().as(ServerPlayer.class).orElse(null);
    }
}
