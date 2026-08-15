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
 * Every Velara node that changes damage the player takes.
 *
 * <p>All four reducers ride {@link FinalDamageStage} rather than {@code PlayerStat.RESISTANCE}:
 * resistance is hard capped at 50% (95% with cap gear), which would silently swallow Adaptive
 * Armor's -60% and make Fleeting Physicality's x3 and Sacrifice's syphon incoherent. The stage's
 * deterministic id ordering also puts the Sacrifice syphon last, so a shepherd absorbs the amount
 * that survived the protected player's own mitigation rather than the raw hit.
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
        FinalDamageStage.register(SACRIFICE_SYPHON, FinalDamageStage.ORDER_REDUCTION + 90, VelaraDamage::sacrificeSyphon);
    }

    private static ServerPlayer defender(LivingDamageEvent event) {
        LivingEntity entity = event.getEntityLiving();
        return entity instanceof ServerPlayer player ? player : null;
    }

    private static float adaptiveArmor(LivingDamageEvent event, float amount) {
        ServerPlayer player = defender(event);
        if (player == null || !VelaraNodeState.isActive(player, VelaraNode.ADAPTIVE_ARMOR)) {
            return amount;
        }
        return amount * AdaptiveArmor.advanceAndGetMultiplier(player, event.getSource());
    }

    /**
     * The vulnerable half of Fleeting Physicality. Damage syphoned onto a shepherd is exempt - 
     * tripling it would turn a Sacrifice plus Fleeting pairing into a suicide button rather than a
     * trade-off.
     */
    private static float fleetingPhysicality(LivingDamageEvent event, float amount) {
        if (VelaraActiveFlags.IS_SACRIFICE_SYPHONING.isSet()) {
            return amount;
        }
        ServerPlayer player = defender(event);
        if (player == null || !VelaraNodeState.isActive(player, VelaraNode.FLEETING_PHYSICALITY)) {
            return amount;
        }
        return FleetingPhysicality.isVulnerable(player) ? amount * VelaraValues.FLEETING_DAMAGE_MULTIPLIER : amount;
    }

    /**
     * Magic Armor. Vanilla skips armour entirely for {@code bypassArmor} sources, so this applies
     * the base mod's own armour curve at half the player's armour value to exactly the hits that
     * were skipped. Note that virtually nothing in this pack is magic damage -  every vault ability
     * hits with {@code DamageSource.playerAttack} -  so the node is close to inert until "magic
     * damage" is redefined.
     */
    private static float magicArmor(LivingDamageEvent event, float amount) {
        ServerPlayer player = defender(event);
        if (player == null || !VelaraNodeState.isActive(player, VelaraNode.MAGIC_ARMOR)) {
            return amount;
        }
        DamageSource source = event.getSource();
        if (!source.isMagic() || !source.isBypassArmor() || source.isBypassInvul()) {
            return amount;
        }
        float armor = player.getArmorValue() * VelaraValues.MAGIC_ARMOR_EFFICIENCY;
        if (armor <= 0.0F) {
            return amount;
        }
        return amount * StatUtils.getArmorMultiplier(armor);
    }

    /**
     * Sacrifice's syphon. Two thirds of what would have landed on a flock member is moved onto
     * their shepherd, arriving a further third lighter. The shepherd receives it through
     * {@code hurt} with the original source so their own armour, resistance and Adaptive Armor
     * still count, which is what "additional 33% resistance" implies; the guard flag stops that
     * second pass from syphoning again and stops mutual Sacrifice pairs from looping.
     */
    private static float sacrificeSyphon(LivingDamageEvent event, float amount) {
        if (VelaraActiveFlags.IS_SACRIFICE_SYPHONING.isSet() || amount <= 0.0F) {
            return amount;
        }
        ServerPlayer player = defender(event);
        if (player == null || VelaraNodeState.isActive(player, VelaraNode.IMMORTAL)) {
            return amount;
        }
        ServerPlayer shepherd = SacrificeFlocks.getShepherd(player);
        if (shepherd == null) {
            return amount;
        }
        float syphoned = amount * VelaraValues.SACRIFICE_SYPHON;
        float delivered = syphoned * (1.0F - VelaraValues.SACRIFICE_RESISTANCE);
        deliver(shepherd, event.getSource(), delivered);
        return amount - syphoned;
    }

    private static void deliver(ServerPlayer shepherd, DamageSource source, float delivered) {
        int savedInvulnerable = shepherd.invulnerableTime;
        int savedHurtTime = shepherd.hurtTime;
        VelaraActiveFlags.IS_SACRIFICE_SYPHONING.push();
        try {
            shepherd.invulnerableTime = 0;
            shepherd.hurt(source, delivered);
        } catch (RuntimeException e) {
            WoldsVaults.LOGGER.error("Sacrifice failed to syphon {} damage onto {}; the hit stays with its original target.",
                    delivered, shepherd.getGameProfile().getName(), e);
        } finally {
            shepherd.invulnerableTime = savedInvulnerable;
            shepherd.hurtTime = savedHurtTime;
            VelaraActiveFlags.IS_SACRIFICE_SYPHONING.pop();
        }
        if (shepherd.isDeadOrDying()) {
            SacrificeFlocks.rebuildFor(shepherd);
        }
    }

    /**
     * The immune half of Fleeting Physicality, cancelled at the same seam and with the same
     * {@code bypassInvul} carve-out the base mod's Immortality effect uses, so the vault timer and
     * anything else built to ignore invulnerability still lands.
     */
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
        if (source.isBypassInvul() || VelaraActiveFlags.IS_SACRIFICE_SYPHONING.isSet()) {
            return false;
        }
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }
        return VelaraNodeState.isActive(player, VelaraNode.FLEETING_PHYSICALITY) && FleetingPhysicality.isImmune(player);
    }
}
