package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.event.ActiveFlags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;
import xyz.iwolfking.woldsvaults.mixins.LivingEntityAccessor;

/**
 * Lands a flat hit that nothing in the pack is allowed to <em>reduce</em>, while leaving every
 * binary defence intact: a true hit can still be dodged, blocked or refused by an immunity window,
 * because those defences cancel the hit outright rather than shaving it, and a cancelled hit is a
 * miss, not a softened one.
 *
 * <p>Two layers carry the "cannot be reduced" half. The bypass flags on {@link TrueDamageSource}
 * handle vanilla armor and Resistance. Above that, the {@code LOWEST} {@code LivingHurtEvent}
 * listener here restores the authored amount after the hurt-stage reducers, so a handler that
 * shaves the hit to nothing cannot stop it reaching the damage stage at all, and a floor sub-stage
 * on {@link FinalDamageStage} restores it once more after every god node, gear attribute and Second
 * Chance reducer has taken its cut - the final stage runs last by construction, which is exactly
 * why the addon has it.
 *
 * <p>Both callers fire the true hit on the back of a regular melee hit, which has just armed the
 * target's invulnerability frames with its own, much larger amount; vanilla would swallow the
 * smaller follow-up outright. {@link #deal} therefore clears the frames for the duration of the
 * hit and puts them back afterwards, including the {@code lastHurt} watermark, so the follow-up
 * lands in full and the frames go on protecting the target from the next ordinary hit exactly as
 * they would have.
 *
 * <p>Thorns is suppressed for the duration of the hit. {@code ActiveFlags} is a ref-counted
 * {@code ThreadLocal} and both thorns implementations in play gate on {@code IS_THORNS_REFLECTING} -
 * the base mod's {@code GearAttributeEvents#thornsReflectDamage} directly, and the addon's own
 * {@code thornsScalingDamage} through {@code WoldEventHelper#isNormalAttack}. Setting it for the
 * true hit means a thorns build reflects once per swing rather than twice, which is the point: the
 * second event exists to pierce mitigation, not to double a build's output.</p>
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VaultTrueDamage {
    private VaultTrueDamage() {
    }

    public static void register() {
        FinalDamageStage.register(VaultMod.id("true_damage_floor"), FinalDamageStage.ORDER_FLOOR, (event, amount) -> {
            if (event.getSource() instanceof TrueDamageSource source) {
                return source.getAuthoredAmount();
            }
            return amount;
        });
    }

    /**
     * Deals {@code amount} to {@code target}, credited to {@code attacker}, with thorns muted and
     * the target's invulnerability frames cleared for the one hit. Creative and spectator players
     * are never hit. Returns whether the hit landed; a dodge, block or immunity window reports
     * {@code false}.
     *
     * <p>The pushes and pops are balanced through a finally because the flags are thread-local
     * counters: an unbalanced push from a throwing {@code hurt} would silently disable thorns for
     * that server thread for the rest of the session. The frame restore sits in the same finally
     * for the same reason - a target left with cleared frames would take every follow-up hit in
     * full.</p>
     */
    public static boolean deal(LivingEntity attacker, LivingEntity target, float amount) {
        if (attacker == null || target == null || amount <= 0.0F || target.level.isClientSide) {
            return false;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        int savedInvulnerable = target.invulnerableTime;
        int savedHurtTime = target.hurtTime;
        float savedLastHurt = ((LivingEntityAccessor) target).getLastHurt();
        ActiveFlags.IS_THORNS_REFLECTING.push();
        WoldActiveFlags.IS_TRUE_DAMAGE.push();
        try {
            target.invulnerableTime = 0;
            return target.hurt(new TrueDamageSource(attacker, amount), amount);
        } finally {
            target.invulnerableTime = savedInvulnerable;
            target.hurtTime = savedHurtTime;
            ((LivingEntityAccessor) target).setLastHurt(savedLastHurt);
            WoldActiveFlags.IS_TRUE_DAMAGE.pop();
            ActiveFlags.IS_THORNS_REFLECTING.pop();
        }
    }

    /**
     * Restores the authored amount after the hurt-stage reducers. A cancelled event is left
     * cancelled: that is a dodge, a block or an immunity, and all three are meant to beat a true
     * hit.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void refuseHurtReduction(LivingHurtEvent event) {
        if (event.getSource() instanceof TrueDamageSource source && event.getAmount() != source.getAuthoredAmount()) {
            event.setAmount(source.getAuthoredAmount());
        }
    }
}
