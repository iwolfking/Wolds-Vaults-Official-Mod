package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.GrantedEffectEvent;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;

/**
 * The Velara listener set on {@code PLAYER_STAT} and {@code GRANTED_EFFECT}. The bus dispatches in
 * descending priority: additive, then multiplicative, then Malediction's cap.
 */
public final class VelaraStatBus {
    static final Object LISTENER_REF = new Object();
    private static final int MULTIPLICATIVE_PRIORITY = -500;
    private static final int MALEDICTION_PRIORITY = -1000;

    private VelaraStatBus() {
    }

    static void register() {
        CommonEvents.PLAYER_STAT.of(PlayerStat.RESISTANCE).register(LISTENER_REF, VelaraStatBus::onResistance);
        CommonEvents.PLAYER_STAT.of(PlayerStat.HEALING_EFFECTIVENESS).register(LISTENER_REF, VelaraStatBus::onHealingEffectiveness);
        CommonEvents.PLAYER_STAT.of(PlayerStat.HEALING_EFFECTIVENESS).register(LISTENER_REF, VelaraStatBus::multiplyHealingEffectiveness, MULTIPLICATIVE_PRIORITY);
        CommonEvents.PLAYER_STAT.of(PlayerStat.HEALING_EFFECTIVENESS).register(LISTENER_REF, VelaraStatBus::clampMalediction, MALEDICTION_PRIORITY);
        CommonEvents.PLAYER_STAT.of(PlayerStat.THORNS_DAMAGE_MULTIPLIER).register(LISTENER_REF, VelaraStatBus::onThornsMultiplier, MULTIPLICATIVE_PRIORITY);
        CommonEvents.PLAYER_STAT.of(PlayerStat.THORNS_DAMAGE_FLAT).register(LISTENER_REF, VelaraStatBus::onThornsFlat, MULTIPLICATIVE_PRIORITY);
        CommonEvents.GRANTED_EFFECT.register(LISTENER_REF, VelaraStatBus::onGrantedEffects);
    }

    private static ServerPlayer serverPlayer(LivingEntity entity) {
        return entity instanceof ServerPlayer player ? player : null;
    }

    private static void onResistance(PlayerStatEvent.Data data) {
        ServerPlayer player = serverPlayer(data.getEntity());
        if (player == null) {
            return;
        }
        int stacks = VelaraAuras.getPresenceStacks(player);
        if (stacks > 0) {
            data.setValue(data.getValue() + VelaraValues.presenceResistance() * stacks);
        }
    }

    /** Healing Flow and Presence, both flat additions. */
    private static void onHealingEffectiveness(PlayerStatEvent.Data data) {
        ServerPlayer player = serverPlayer(data.getEntity());
        if (player == null) {
            return;
        }
        float value = data.getValue();
        value += healingFlowBonus(player);
        value += VelaraValues.presenceHealing() * VelaraAuras.getPresenceStacks(player);
        data.setValue(value);
    }

    /** Immortal and Bounce Back, both multipliers on the running total. */
    private static void multiplyHealingEffectiveness(PlayerStatEvent.Data data) {
        ServerPlayer player = serverPlayer(data.getEntity());
        if (player == null) {
            return;
        }
        float value = data.getValue();
        if (VelaraNodes.isActive(player, VelaraNodes.IMMORTAL)) {
            value *= VelaraValues.immortalHealingMultiplier();
        }
        if (VelaraNodes.isActive(player, VelaraNodes.BOUNCE_BACK)
                && player.getHealth() <= player.getMaxHealth() * VelaraValues.bounceBackHealthThreshold()) {
            value *= VelaraValues.bounceBackMultiplier();
        }
        data.setValue(value);
    }

    /** Malediction's healing leg, registered last on the stat so it caps every other source too. */
    private static void clampMalediction(PlayerStatEvent.Data data) {
        ServerPlayer player = serverPlayer(data.getEntity());
        if (player == null || !VelaraNodes.isActive(player, VelaraNodes.MALEDICTION)) {
            return;
        }
        data.setValue(Math.min(data.getValue(), VelaraValues.maledictionForcedHealing()));
    }

    private static float healingFlowBonus(ServerPlayer player) {
        if (!VelaraNodes.isActive(player, VelaraNodes.HEALING_FLOW)) {
            return 0.0F;
        }
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        float manaRegen = snapshot.getAttributeValue(ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE,
                VaultGearAttributeTypeMerger.floatSum());
        return manaRegen * VelaraValues.healingFlowPerManaRegen();
    }

    private static void onThornsMultiplier(PlayerStatEvent.Data data) {
        ServerPlayer player = serverPlayer(data.getEntity());
        if (player == null) {
            return;
        }
        float factor = thornsFactor(player);
        if (factor != 1.0F) {
            data.setValue(data.getValue() * factor);
        }
    }

    private static void onThornsFlat(PlayerStatEvent.Data data) {
        ServerPlayer player = serverPlayer(data.getEntity());
        if (player == null) {
            return;
        }
        float factor = thornsFactor(player);
        if (factor != 1.0F) {
            data.setValue(data.getValue() * factor);
        }
    }

    /** Cactus and Malediction combined, the latter off the raw gear healing-efficiency sum. */
    private static float thornsFactor(ServerPlayer player) {
        float factor = 1.0F;
        if (VelaraNodes.isActive(player, VelaraNodes.CACTUS)) {
            factor *= VelaraValues.cactusThornsMultiplier();
        }
        if (VelaraNodes.isActive(player, VelaraNodes.MALEDICTION)) {
            factor *= maledictionFactor(gearHealingEfficiency(player));
        }
        return factor;
    }

    private static float gearHealingEfficiency(ServerPlayer player) {
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        return snapshot.getAttributeValue(ModGearAttributes.HEALING_EFFECTIVENESS, VaultGearAttributeTypeMerger.floatSum());
    }

    private static float maledictionFactor(float healing) {
        return (float) Math.cbrt(Math.max(0.0D, 1.0D + healing));
    }

    static GodNodePreviews.Preview previewMalediction(ServerPlayer player) {
        float healing = gearHealingEfficiency(player);
        float factor = maledictionFactor(healing);
        return new GodNodePreviews.Working(VaultGod.VELARA)
                .formula("Thorns multiplier", "cubeRoot(1 + healing efficiency)")
                .input("healing efficiency", "your gear's healing efficiency, before this star forces it to -50%",
                        GodNodePreviews.signedPercent(healing))
                .result("cubeRoot(" + GodNodePreviews.number(Math.max(0.0D, 1.0D + healing)) + ")", factor)
                .inactive(!VelaraNodes.isActive(player, VelaraNodes.MALEDICTION))
                .build(factor);
    }

    /** Regeneration levels from Indomitable, Immortal and Presence, summed across other sources. */
    private static void onGrantedEffects(GrantedEffectEvent.Data data) {
        if (!(data.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (!data.getFilter().test(MobEffects.REGENERATION)) {
            return;
        }
        int levels = 0;
        if (VelaraNodes.isActive(player, VelaraNodes.INDOMITABLE)) {
            levels += VelaraValues.indomitableRegenerationLevels();
        }
        if (VelaraNodes.isActive(player, VelaraNodes.IMMORTAL)) {
            levels += VelaraValues.immortalRegenerationLevels();
        }
        levels += VelaraValues.presenceRegenerationLevels() * VelaraAuras.getPresenceStacks(player);
        if (levels > 0) {
            data.getEffects().addAmplifier(MobEffects.REGENERATION, levels);
        }
    }
}
