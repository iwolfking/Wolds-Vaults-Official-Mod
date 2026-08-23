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
 * Armor's -60% and make Fleeting Physicality's x3 and Sacrifice's syphon incoherent. The Sacrifice
 * syphon registers at {@link FinalDamageStage#ORDER_SPLIT}, ahead of every reduction, so the hit is
 * divided while it is still unmitigated and each of the two players then mitigates only their own
 * share of it.
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
        if (player == null || !VelaraNodes.isActive(player, VelaraNodes.FLEETING_PHYSICALITY)) {
            return amount;
        }
        return VelaraFleetingPhysicality.isVulnerable(player) ? amount * VelaraValues.fleetingDamageMultiplier() : amount;
    }

    /**
     * Magic Armor. This node reduces magic damage dealt TO the player, not anything the player
     * deals. Splash potions of harming ({@code DamageSource.indirectMagic}, or
     * {@code DamageSource.MAGIC} when nothing owns the throw), the rune boss wave blast and the
     * addon's own {@code DamageSource.MAGIC} effects all build their source as magic that bypasses
     * armour. Vanilla skips armour entirely for {@code bypassArmor} sources, so this applies the
     * base mod's own armour curve at half the player's armour value to exactly the hits that were
     * skipped. Magic that does not bypass armour is deliberately excluded: armour already applied
     * to it once and this node would otherwise double dip.
     */
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
     * Sacrifice's syphon. Two thirds of the unmitigated hit is moved onto the flock member's
     * shepherd, arriving a further third lighter, and the third that stays behind is what the
     * flock member's own reducers then run against - the split is a division of raw damage, not of
     * what one of the two had already survived. The shepherd receives their share through
     * {@code hurt} with the original source so their own armour, resistance and Adaptive Armor
     * count on top of the 33%; the guard flag is what stops that second pass from syphoning again,
     * which is the only loop that can arise - the partition never puts a Sacrifice user in a flock,
     * so two Sacrifice users can never be each other's shepherd.
     *
     * <p>A delivery that throws gives the hit back: the flock member keeps the full amount rather
     * than the syphoned share evaporating. A delivery the shepherd survives untouched - their own
     * Fleeting immunity cancelling it, say - still counts as delivered, because that is the
     * shepherd's own mitigation doing its job on damage that did reach them.
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

    /**
     * Hands the shepherd their share, and reports whether it reached them. Only a thrown delivery
     * is a failure; the caller then leaves the whole hit with the flock member rather than
     * destroying the syphoned share.
     */
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

    /**
     * The immune half of Fleeting Physicality, cancelled at the same seam and with the same
     * {@code bypassInvul} carve-out the base mod's Immortality effect uses, so the vault timer and
     * anything else built to ignore invulnerability still lands.
     *
     * <p>Unlike the vulnerable half, this one has no exemption for a syphoned Sacrifice hit: the
     * whole point of immunity is that damage arriving at this player stops, and damage routed here
     * by someone else's Sacrifice is still damage arriving at this player. The syphon guard flag
     * exists to stop the second pass syphoning again, not to make a shepherd unable to defend
     * themselves.
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
        if (source.isBypassInvul()) {
            return false;
        }
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }
        return VelaraNodes.isActive(player, VelaraNodes.FLEETING_PHYSICALITY) && VelaraFleetingPhysicality.isImmune(player);
    }
}
