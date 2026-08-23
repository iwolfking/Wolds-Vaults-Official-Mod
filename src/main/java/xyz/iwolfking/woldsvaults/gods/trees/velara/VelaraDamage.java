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

/**
 * Every Velara node that changes damage the player takes, on {@link FinalDamageStage}. The syphon
 * runs at {@link FinalDamageStage#ORDER_SPLIT}, ahead of every reduction.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VelaraDamage {
    private static final ResourceLocation ADAPTIVE_ARMOR = WoldsVaults.id("velara_adaptive_armor");
    private static final ResourceLocation FLEETING_PHYSICALITY = WoldsVaults.id("velara_fleeting_physicality");
    private static final ResourceLocation MAGIC_ARMOR = WoldsVaults.id("velara_magic_armor");
    private static final ResourceLocation SACRIFICE_SYPHON = WoldsVaults.id("velara_sacrifice_syphon");

    private VelaraDamage() {
    }

    static void register() {
        FinalDamageStage.register(ADAPTIVE_ARMOR, FinalDamageStage.ORDER_REDUCTION, VelaraDamage::adaptiveArmor);
        FinalDamageStage.register(FLEETING_PHYSICALITY, FinalDamageStage.ORDER_REDUCTION, VelaraDamage::fleetingPhysicality);
        FinalDamageStage.register(MAGIC_ARMOR, FinalDamageStage.ORDER_REDUCTION, VelaraDamage::magicArmor);
        FinalDamageStage.register(SACRIFICE_SYPHON, FinalDamageStage.ORDER_SPLIT, VelaraDamage::sacrificeSyphon);
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
     * Sacrifice's syphon: moves a share of the unmitigated hit onto the shepherd, where it runs
     * through their own mitigation. A delivery that throws leaves the whole hit behind.
     */
    private static float sacrificeSyphon(LivingDamageEvent event, float amount) {
        if (VelaraActiveFlags.IS_SACRIFICE_SYPHONING.isSet() || amount <= 0.0F) {
            return amount;
        }
        ServerPlayer player = defender(event);
        if (player == null || VelaraNodes.isActive(player, VelaraNodes.IMMORTAL)) {
            return amount;
        }
        ServerPlayer shepherd = VelaraSacrificeFlocks.getShepherd(player);
        if (shepherd == null) {
            return amount;
        }
        float syphoned = amount * VelaraValues.sacrificeSyphon();
        float delivered = syphoned * (1.0F - VelaraValues.sacrificeResistance());
        if (!deliver(shepherd, event.getSource(), delivered)) {
            return amount;
        }
        return amount - syphoned;
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
            WoldsVaults.LOGGER.error("Sacrifice failed to syphon {} damage onto {}; the hit stays with its original target.",
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
