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
 * Lands a flat hit nothing in the pack may reduce; dodges, blocks and immunity windows still cancel it
 * outright. The authored amount is restored by the {@code LOWEST} {@link LivingHurtEvent} listener here
 * and again by a {@link FinalDamageStage} floor sub-stage.
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
     * Deals {@code amount} to {@code target} for {@code attacker}, with thorns muted and the target's
     * invulnerability frames cleared and restored around the one hit. Never hits creative or spectator players.
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void refuseHurtReduction(LivingHurtEvent event) {
        if (event.getSource() instanceof TrueDamageSource source && event.getAmount() != source.getAuthoredAmount()) {
            event.setAmount(source.getAuthoredAmount());
        }
    }
}
