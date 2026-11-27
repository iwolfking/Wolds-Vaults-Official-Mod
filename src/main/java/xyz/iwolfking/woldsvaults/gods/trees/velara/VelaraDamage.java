package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.util.StatUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Every Velara node that changes damage the player takes. Reductions run on {@link FinalDamageStage};
 * Sacrifice's syphon runs on {@link LivingHurtEvent}, before armor and resistance, so each share of a
 * hit is mitigated once, by the player who takes it.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VelaraDamage {
    private static final ResourceLocation ADAPTIVE_ARMOR = WoldsVaults.id("velara_adaptive_armor");
    private static final ResourceLocation FLEETING_PHYSICALITY = WoldsVaults.id("velara_fleeting_physicality");
    private static final ResourceLocation MAGIC_ARMOR = WoldsVaults.id("velara_magic_armor");
    private static final Map<LivingHurtEvent, Float> RAW_HITS = new WeakHashMap<>();

    private VelaraDamage() {
    }

    static void register() {
        FinalDamageStage.register(ADAPTIVE_ARMOR, FinalDamageStage.ORDER_REDUCTION, VelaraDamage::adaptiveArmor);
        FinalDamageStage.register(FLEETING_PHYSICALITY, FinalDamageStage.ORDER_REDUCTION, VelaraDamage::fleetingPhysicality);
        FinalDamageStage.register(MAGIC_ARMOR, FinalDamageStage.ORDER_REDUCTION, VelaraDamage::magicArmor);
    }

    private static ServerPlayer defender(LivingDamageEvent event) {
        LivingEntity entity = event.getEntityLiving();
        return entity instanceof ServerPlayer player ? player : null;
    }

    private static float adaptiveArmor(LivingDamageEvent event, float amount) {
        ServerPlayer player = defender(event);
        if (player == null || !VelaraNodes.isActive(player, VelaraNodes.ADAPTIVE_ARMOR)) {
            return amount;
        }
        return amount * VelaraAdaptiveArmor.advanceAndGetMultiplier(player, event.getSource());
    }

    /** The vulnerable half of Fleeting Physicality. Damage syphoned onto a shepherd is exempt. */
    private static float fleetingPhysicality(LivingDamageEvent event, float amount) {
        if (VelaraActiveFlags.IS_SACRIFICE_SYPHONING.isSet()) {
            return amount;
        }
        ServerPlayer player = defender(event);
        if (player == null || !VelaraNodes.isActive(player, VelaraNodes.FLEETING_PHYSICALITY)) {
            return amount;
        }
        return VelaraFleetingPhysicality.isVulnerable(player) ? amount * VelaraValues.fleetingDamageMultiplier() : amount;
    }

    /** Magic Armor: applies the armour curve, at reduced armour, to armour-bypassing magic damage. */
    private static float magicArmor(LivingDamageEvent event, float amount) {
        ServerPlayer player = defender(event);
        if (player == null || !VelaraNodes.isActive(player, VelaraNodes.MAGIC_ARMOR)) {
            return amount;
        }
        DamageSource source = event.getSource();
        if (!source.isMagic() || !source.isBypassArmor() || source.isBypassInvul()) {
            return amount;
        }
        float armor = player.getArmorValue() * VelaraValues.magicArmorEfficiency();
        if (armor <= 0.0F) {
            return amount;
        }
        return amount * StatUtils.getArmorMultiplier(armor);
    }

    /**
     * Records the raw hit on a protected player before any attacker or defender modifier changes it.
     * The shepherd's share is cut from this amount, and their own hurt pipeline applies the modifiers once.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void recordRawHit(LivingHurtEvent event) {
        if (protectedPlayer(event) != null) {
            RAW_HITS.put(event, event.getAmount());
        }
    }

    /**
     * Sacrifice's syphon: once the hit has survived every cancellation, moves a share of it onto the
     * shepherd, who takes the raw share through their own mitigation. A delivery that throws leaves the
     * whole hit behind.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void sacrificeSyphon(LivingHurtEvent event) {
        Float raw = RAW_HITS.remove(event);
        if (raw == null || event.isCanceled()) {
            return;
        }
        ServerPlayer player = protectedPlayer(event);
        ServerPlayer shepherd = player == null ? null : VelaraSacrificeFlocks.getShepherd(player);
        if (shepherd == null) {
            return;
        }
        float share = VelaraValues.sacrificeSyphon();
        float delivered = raw * share * (1.0F - VelaraValues.sacrificeResistance());
        if (deliver(shepherd, event.getSource(), delivered)) {
            event.setAmount(event.getAmount() * (1.0F - share));
        }
    }

    private static ServerPlayer protectedPlayer(LivingHurtEvent event) {
        if (VelaraActiveFlags.IS_SACRIFICE_SYPHONING.isSet() || event.getAmount() <= 0.0F
                || !(event.getEntityLiving() instanceof ServerPlayer player)
                || VelaraNodes.isActive(player, VelaraNodes.IMMORTAL)) {
            return null;
        }
        return VelaraSacrificeFlocks.getShepherd(player) == null ? null : player;
    }

    /** Hands the shepherd their share. Returns false only if the delivery threw. */
    private static boolean deliver(ServerPlayer shepherd, DamageSource source, float delivered) {
        int savedInvulnerable = shepherd.invulnerableTime;
        int savedHurtTime = shepherd.hurtTime;
        boolean landed = true;
        VelaraActiveFlags.IS_SACRIFICE_SYPHONING.push();
        try {
            shepherd.invulnerableTime = 0;
            shepherd.hurt(source, delivered);
        } catch (RuntimeException e) {
            landed = false;
            WoldLogger.error("Sacrifice failed to syphon {} damage onto {}; the hit stays with its original target.",
                    delivered, shepherd.getGameProfile().getName(), e);
        } finally {
            shepherd.invulnerableTime = savedInvulnerable;
            shepherd.hurtTime = savedHurtTime;
            VelaraActiveFlags.IS_SACRIFICE_SYPHONING.pop();
        }
        if (shepherd.isDeadOrDying()) {
            VelaraSacrificeFlocks.rebuildFor(shepherd);
        }
        return landed;
    }

    /** The immune half of Fleeting Physicality. Sources that bypass invulnerability still land. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAttack(LivingAttackEvent event) {
        if (shouldBlockEntirely(event.getEntityLiving(), event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHurt(LivingHurtEvent event) {
        if (shouldBlockEntirely(event.getEntityLiving(), event.getSource())) {
            event.setCanceled(true);
        }
    }

    private static boolean shouldBlockEntirely(LivingEntity entity, DamageSource source) {
        if (source.isBypassInvul()) {
            return false;
        }
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }
        return VelaraNodes.isActive(player, VelaraNodes.FLEETING_PHYSICALITY) && VelaraFleetingPhysicality.isImmune(player);
    }
}
