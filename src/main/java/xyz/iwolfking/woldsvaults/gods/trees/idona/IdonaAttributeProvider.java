package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.gods.GodNodeAttributeSource;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Every Idona node whose whole effect is a gear attribute contribution, folded into the player's
 * attribute snapshot by {@code GodCarryover}.
 *
 * <p>{@link GodNodeAttributeSource.Scope#BASIC} returns only the stat nodes, because those are the
 * subset that carries over to a foreign tree at 25%; minors and majors are {@code ALL} only.
 * Minor nodes selected through another god's transfer slots come back through
 * {@link #getMinorTransferAttributes}, which silently ignores ids belonging to other trees.
 */
public final class IdonaAttributeProvider implements GodTreeAttributeProviders.Provider {
    @Override
    public List<VaultGearAttributeInstance<?>> getGearAttributes(ServerPlayer player, GodNodeAttributeSource.Scope scope) {
        List<VaultGearAttributeInstance<?>> values = new ArrayList<>();
        addStatNodes(player, values);
        if (scope == GodNodeAttributeSource.Scope.BASIC) {
            return values;
        }
        addWeaponmaster(player, IdonaNodes.minorPoints(player, IdonaNodes.WEAPONMASTER), values);
        addGrandArchmage(player, values);
        return values;
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getMinorTransferAttributes(ServerPlayer player, Collection<String> nodeIds) {
        List<VaultGearAttributeInstance<?>> values = new ArrayList<>();
        if (nodeIds.contains(IdonaNodes.WEAPONMASTER)) {
            addWeaponmaster(player, IdonaNodes.minorPoints(player, IdonaNodes.WEAPONMASTER), values);
        }
        return values;
    }

    private void addStatNodes(ServerPlayer player, List<VaultGearAttributeInstance<?>> values) {
        float hardHitter = banded(player, IdonaNodes.HARD_HITTER, IdonaNodes.V_HARD_HITTER,
                IdonaNodes.HARD_HITTER_II, IdonaNodes.V_HARD_HITTER_II);
        if (hardHitter > 0.0F) {
            add(values, ModGearAttributes.DAMAGE_INCREASE, hardHitter);
        }
        float eliteCaster = banded(player, IdonaNodes.ELITE_CASTER, IdonaNodes.V_ELITE_CASTER,
                IdonaNodes.ELITE_CASTER_II, IdonaNodes.V_ELITE_CASTER_II);
        if (eliteCaster > 0.0F) {
            add(values, ModGearAttributes.ABILITY_POWER_PERCENT, eliteCaster);
        }
        int fullOfSoul = IdonaNodes.ledgerPoints(player, IdonaNodes.FULL_OF_SOUL);
        if (fullOfSoul > 0) {
            add(values, ModGearAttributes.SOUL_QUANTITY_PERCENTILE, IdonaNodes.valueAt(IdonaNodes.V_FULL_OF_SOUL, fullOfSoul));
        }
        float fortunate = banded(player, IdonaNodes.FORTUNATE, IdonaNodes.V_FORTUNATE,
                IdonaNodes.FORTUNATE_II, IdonaNodes.V_FORTUNATE_II);
        if (fortunate > 0.0F) {
            add(values, ModGearAttributes.LUCKY_HIT_CHANCE, fortunate);
        }
        int stacks = IdonaNodes.ledgerPoints(player, IdonaNodes.STACK_STACK_STACK);
        if (stacks > 0) {
            values.add(new VaultGearAttributeInstance<>(xyz.iwolfking.woldsvaults.init.ModGearAttributes.ADDITIONAL_STACKING_STACKS,
                    Math.round(IdonaNodes.valueAt(IdonaNodes.V_STACK_STACK_STACK, stacks))));
        }
        int kingHunter = IdonaNodes.ledgerPoints(player, IdonaNodes.KING_HUNTER);
        if (kingHunter > 0) {
            float value = IdonaNodes.valueAt(IdonaNodes.V_KING_HUNTER, kingHunter);
            add(values, ModGearAttributes.DAMAGE_CHAMPION, value);
            add(values, ModGearAttributes.DAMAGE_TANK, value);
        }
        int enforcer = IdonaNodes.ledgerPoints(player, IdonaNodes.ENFORCER);
        if (enforcer > 0) {
            float value = IdonaNodes.valueAt(IdonaNodes.V_ENFORCER, enforcer);
            add(values, ModGearAttributes.DAMAGE_HORDE, value);
            add(values, ModGearAttributes.DAMAGE_ASSASSIN, value);
        }
    }

    /**
     * Weaponmaster's dual-wield branch. The two-handed branch is a per-hit damage multiplier and
     * lives in the hit handler; only the attack-speed half is a gear attribute, and it is granted
     * conditionally the same way a gear-attribute talent is -  re-evaluated whenever the snapshot
     * is rebuilt.
     */
    private void addWeaponmaster(ServerPlayer player, int points, List<VaultGearAttributeInstance<?>> values) {
        if (points <= 0 || !isDualWielding(player)) {
            return;
        }
        values.add(new VaultGearAttributeInstance<>(ModGearAttributes.ATTACK_SPEED_PERCENT,
                IdonaNodes.WEAPONMASTER_DUAL_WIELD_ATTACK_SPEED * points));
    }

    private void addGrandArchmage(ServerPlayer player, List<VaultGearAttributeInstance<?>> values) {
        if (!IdonaNodes.isMajorActive(player, IdonaNodes.GRAND_ARCHMAGE)) {
            return;
        }
        add(values, ModGearAttributes.MANA_ADDITIVE_PERCENTILE, IdonaNodes.ARCHMAGE_MANA_PERCENTILE);
        values.add(new VaultGearAttributeInstance<>(ModGearAttributes.MANA_ADDITIVE, IdonaNodes.ARCHMAGE_MANA_FLAT));
        add(values, ModGearAttributes.MANA_REGEN_ADDITIVE_PERCENTILE, IdonaNodes.ARCHMAGE_MANA_REGEN);
        add(values, ModGearAttributes.COOLDOWN_REDUCTION_CAP, IdonaNodes.ARCHMAGE_CDR_CAP);
    }

    /**
     * Two mainhand-intended vault gear items, one in each hand -  the same test the addon's Better
     * Combat snapshot mixin already uses, so the two Weaponmaster branches stay mutually exclusive
     * with the offhand-stat suppression that applies to battlestaffs.
     */
    public static boolean isDualWielding(ServerPlayer player) {
        ItemStack main = player.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack off = player.getItemBySlot(EquipmentSlot.OFFHAND);
        return main.getItem() instanceof VaultGearItem mainGear
                && off.getItem() instanceof VaultGearItem offGear
                && mainGear.isIntendedForSlot(main, EquipmentSlot.MAINHAND)
                && offGear.isIntendedForSlot(off, EquipmentSlot.MAINHAND);
    }

    /**
     * Total value of a banded stat: the shallow placements and the deep ones are separate ledger
     * entries, and each band pays its own value for every star owned in it.
     */
    private static float banded(ServerPlayer player, String lesser, float[] lesserValue,
                                String greater, float[] greaterValue) {
        return IdonaNodes.valueAt(lesserValue, IdonaNodes.ledgerPoints(player, lesser))
                + IdonaNodes.valueAt(greaterValue, IdonaNodes.ledgerPoints(player, greater));
    }

    private static void add(List<VaultGearAttributeInstance<?>> values, VaultGearAttribute<Float> attribute, float value) {
        if (value != 0.0F) {
            values.add(new VaultGearAttributeInstance<>(attribute, value));
        }
    }
}
