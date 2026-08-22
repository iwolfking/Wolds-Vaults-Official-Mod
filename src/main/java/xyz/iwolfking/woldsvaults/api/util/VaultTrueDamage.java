package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.VaultMod;
import iskallia.vault.event.ActiveFlags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;

/**
 * Lands a flat hit that nothing in the pack is allowed to reduce.
 *
 * <p>Three layers, because there are three separate ways a hit gets softened. The bypass flags on
 * {@link TrueDamageSource} handle vanilla armor, Resistance and invulnerability frames. The two
 * {@code LOWEST} listeners here undo a cancellation, since a cancelled {@code LivingAttackEvent} or
 * {@code LivingHurtEvent} would stop the damage before the final stage ever ran. And a floor
 * sub-stage on {@link FinalDamageStage} restores the authored amount after every god node, gear
 * attribute and Second Chance reducer has taken its cut - the final stage runs last by construction,
 * which is exactly why the addon has it.
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
     * Deals {@code amount} to {@code target}, credited to {@code attacker}, with thorns muted.
     *
     * <p>The push and pop are balanced through a finally because the flag is a thread-local counter:
     * an unbalanced push from a throwing {@code hurt} would silently disable thorns for that server
     * thread for the rest of the session.</p>
     */
    public static void deal(LivingEntity attacker, LivingEntity target, float amount) {
        if (attacker == null || target == null || amount <= 0.0F || target.level.isClientSide) {
            return;
        }
        ActiveFlags.IS_THORNS_REFLECTING.push();
        WoldActiveFlags.IS_TRUE_DAMAGE.push();
        try {
            target.hurt(new TrueDamageSource(attacker, amount), amount);
        } finally {
            WoldActiveFlags.IS_TRUE_DAMAGE.pop();
            ActiveFlags.IS_THORNS_REFLECTING.pop();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void refuseAttackCancel(LivingAttackEvent event) {
        if (event.getSource() instanceof TrueDamageSource && event.isCanceled()) {
            event.setCanceled(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void refuseHurtReduction(LivingHurtEvent event) {
        if (!(event.getSource() instanceof TrueDamageSource source)) {
            return;
        }
        if (event.isCanceled()) {
            event.setCanceled(false);
        }
        if (event.getAmount() != source.getAuthoredAmount()) {
            event.setAmount(source.getAuthoredAmount());
        }
    }
}
