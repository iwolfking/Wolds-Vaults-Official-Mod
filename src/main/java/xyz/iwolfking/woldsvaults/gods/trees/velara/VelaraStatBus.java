package xyz.iwolfking.woldsvaults.gods.trees.velara;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.GrantedEffectEvent;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * The single Velara listener set on {@code CommonEvents.PLAYER_STAT} and
 * {@code CommonEvents.GRANTED_EFFECT}.
 *
 * <p>One listener per stat rather than one per node: {@code PLAYER_STAT} dispatch is O(listeners)
 * and fires on every hit, heal and thorns computation, so every Velara contribution to a stat is
 * folded inside a single handler where ordering is also explicit rather than emergent.
 */
public final class VelaraStatBus {
    static final Object LISTENER_REF = new Object();
    private static final int MALEDICTION_PRIORITY = -1000;

    private VelaraStatBus() {
    }

    static void register() {
        CommonEvents.PLAYER_STAT.of(PlayerStat.RESISTANCE).register(LISTENER_REF, VelaraStatBus::onResistance);
        CommonEvents.PLAYER_STAT.of(PlayerStat.HEALING_EFFECTIVENESS).register(LISTENER_REF, VelaraStatBus::onHealingEffectiveness);
        CommonEvents.PLAYER_STAT.of(PlayerStat.HEALING_EFFECTIVENESS).register(LISTENER_REF, VelaraStatBus::clampMalediction, MALEDICTION_PRIORITY);
        CommonEvents.PLAYER_STAT.of(PlayerStat.THORNS_DAMAGE_MULTIPLIER).register(LISTENER_REF, VelaraStatBus::onThornsMultiplier);
        CommonEvents.PLAYER_STAT.of(PlayerStat.THORNS_DAMAGE_FLAT).register(LISTENER_REF, VelaraStatBus::onThornsFlat);
        CommonEvents.PLAYER_STAT.of(PlayerStat.SPEED).register(LISTENER_REF, VelaraStatBus::onSpeed);
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
            data.setValue(data.getValue() + VelaraValues.PRESENCE_RESISTANCE * stacks);
        }
    }

    private static void onHealingEffectiveness(PlayerStatEvent.Data data) {
        ServerPlayer player = serverPlayer(data.getEntity());
        if (player == null) {
            return;
        }
        float value = data.getValue();
        value += healingFlowBonus(player);
        value += VelaraValues.PRESENCE_HEALING * VelaraAuras.getPresenceStacks(player);
        if (VelaraNodeState.isActive(player, VelaraNode.IMMORTAL)) {
            value *= VelaraValues.IMMORTAL_HEALING_MULTIPLIER;
        }
        if (VelaraNodeState.isActive(player, VelaraNode.BOUNCE_BACK)
                && player.getHealth() <= player.getMaxHealth() * VelaraValues.BOUNCE_BACK_HEALTH_THRESHOLD) {
            value *= VelaraValues.BOUNCE_BACK_MULTIPLIER;
        }
        data.setValue(value);
    }

    /**
     * Malediction's healing leg, registered last on the stat so it also caps every other Velara
     * healing source. The bus iterates priorities in descending order, so a negative priority is
     * what "runs after everyone else" means here.
     */
    private static void clampMalediction(PlayerStatEvent.Data data) {
        ServerPlayer player = serverPlayer(data.getEntity());
        if (player == null || !VelaraNodeState.isActive(player, VelaraNode.MALEDICTION)) {
            return;
        }
        data.setValue(Math.min(data.getValue(), VelaraValues.MALEDICTION_FORCED_HEALING));
    }

    private static float healingFlowBonus(ServerPlayer player) {
        if (!VelaraNodeState.isActive(player, VelaraNode.HEALING_FLOW)) {
            return 0.0F;
        }
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
        float manaRegen = snapshot.getAttributeValue(ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE,
                VaultGearAttributeTypeMerger.floatSum());
        return manaRegen * VelaraValues.HEALING_FLOW_PER_MANA_REGEN;
    }

    /**
     * Thorns multiplier. The base mod's {@code ThornsHelper.getThornsDamageMultiplier} adds this
     * event's return value to the gear sum it already seeded the event with, so a listener that
     * wants a total of {@code k x gear} has to return {@code (k - 1) x gear}. That compensation is
     * load bearing: if the base double-count is ever fixed to an assignment, this leg silently
     * becomes {@code (k - 1) x gear} and must be rewritten.
     */
    private static void onThornsMultiplier(PlayerStatEvent.Data data) {
        ServerPlayer player = serverPlayer(data.getEntity());
        if (player == null) {
            return;
        }
        float factor = thornsFactor(player);
        if (factor != 1.0F) {
            data.setValue(data.getValue() * (factor - 1.0F));
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

    /**
     * Cactus and Malediction combined. Malediction reads the raw gear healing-efficiency sum
     * rather than the live stat, because the live stat is the one Malediction itself forces down
     * to -50%; feeding that back into the cube root would make the node cancel its own payout.
     */
    private static float thornsFactor(ServerPlayer player) {
        float factor = 1.0F;
        if (VelaraNodeState.isActive(player, VelaraNode.CACTUS)) {
            factor *= VelaraValues.CACTUS_THORNS_MULTIPLIER;
        }
        if (VelaraNodeState.isActive(player, VelaraNode.MALEDICTION)) {
            AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(player);
            float healing = snapshot.getAttributeValue(ModGearAttributes.HEALING_EFFECTIVENESS,
                    VaultGearAttributeTypeMerger.floatSum());
            factor *= (float) Math.cbrt(Math.max(0.0D, 1.0D + healing));
        }
        return factor;
    }

    private static void onSpeed(PlayerStatEvent.Data data) {
        ServerPlayer player = serverPlayer(data.getEntity());
        if (player == null || !VelaraNodeState.isActive(player, VelaraNode.THE_STONEWALL)) {
            return;
        }
        data.setValue(data.getValue() * VelaraValues.STONEWALL_SPEED_MULTIPLIER);
    }

    /**
     * Regeneration levels. {@code GrantedEffects.addAmplifier} sums across sources, which is
     * exactly the "stacks with other sources of regeneration" the sheet asks for.
     */
    private static void onGrantedEffects(GrantedEffectEvent.Data data) {
        if (!(data.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (!data.getFilter().test(MobEffects.REGENERATION)) {
            return;
        }
        int levels = 0;
        if (VelaraNodeState.isActive(player, VelaraNode.INDOMITABLE)) {
            levels += VelaraValues.INDOMITABLE_REGENERATION_LEVELS;
        }
        if (VelaraNodeState.isActive(player, VelaraNode.IMMORTAL)) {
            levels += VelaraValues.IMMORTAL_REGENERATION_LEVELS;
        }
        levels += VelaraValues.PRESENCE_REGENERATION_LEVELS * VelaraAuras.getPresenceStacks(player);
        if (levels > 0) {
            data.getEffects().addAmplifier(MobEffects.REGENERATION, levels);
        }
    }
}
