package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.event.ActiveFlags;
import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

import java.util.UUID;

@Mixin(value = PlayerDamageHelper.class, remap = false)
public class MixinPlayerDamageHelper {
    @Unique
    private static final UUID woldsvaults$BERSERKING_ID = UUID.fromString("125493ba-f280-4246-af3c-b603e08ce12f");

    @Unique
    private static final UUID woldsvaults$DEPLETED_ID = UUID.fromString("ed8528f4-cd3c-4aff-90b2-5471f742d4bd");

    /**
     * @author PoorMansPhysicist
     * @reason keep proc fangs from re-applying player damage multipliers (Rampage etc.)
     * already contained in their stored damage
     */
    @Inject(method = "onPlayerDamage", at = @At("HEAD"), cancellable = true)
    private static void onPlayerDamage(LivingHurtEvent event, CallbackInfo ci) {
        if(WoldActiveFlags.IS_PROC_FANG_ATTACKING.isSet()) {
            ci.cancel();
        }
    }

    /**
     * Berserking and Depleted register their bonus in the normal (attack) registry, which the base handler
     * skips for AoE, ability power and Poison Nova hits. Applies just those two terms to such hits, so the
     * talents count on every damage hit while the rest of the registry stays on normal attacks.
     */
    @Inject(method = "onPlayerDamage", at = @At("TAIL"))
    private static void woldsvaults$applyLowResourceTalentsToFlaggedHits(LivingHurtEvent event, CallbackInfo ci) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!ActiveFlags.IS_AOE_ATTACKING.isSet() && !ActiveFlags.IS_AP_ATTACKING.isSet() && !ActiveFlags.IS_POISON_NOVA_ATTACKING.isSet()) {
            return;
        }
        float bonus = woldsvaults$additiveValue(player, woldsvaults$BERSERKING_ID) + woldsvaults$additiveValue(player, woldsvaults$DEPLETED_ID);
        if (bonus != 0.0F) {
            event.setAmount(event.getAmount() * Math.max(0.0F, 1.0F + bonus));
        }
    }

    @Unique
    private static float woldsvaults$additiveValue(ServerPlayer player, UUID id) {
        PlayerDamageHelper.DamageMultiplier multiplier = PlayerDamageHelper.getMultiplier(player, id);
        return multiplier == null ? 0.0F : multiplier.getMultiplier();
    }

    /**
     * Lifts the level 8 clamp on the Totem damage etching's Vulnerable.
     */
    @ModifyConstant(method = "lambda$applyVulnerableFromTotem$6", constant = @Constant(intValue = 8))
    private static int woldsvaults$uncapTotemVulnerable(int constant) {
        return Integer.MAX_VALUE;
    }
}
