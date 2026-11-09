package xyz.iwolfking.woldsvaults.client.screens.stats;

import iskallia.vault.client.data.ClientDamageData;
import iskallia.vault.client.data.ClientTalentData;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.talent.type.PuristTalent;
import iskallia.vault.skill.talent.type.luckyhit.DamageLuckyHitTalent;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import iskallia.vault.util.calc.LuckyHitHelper;
import iskallia.vault.util.damage.PlayerDamageHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Statistics tab values computed the way combat applies them, where the base screen reads a raw gear sum instead.
 * Talents come from the client talent data, as the base screen's damage estimate does.
 */
public final class StatDisplayValues {
    private static final float DEFAULT_LUCKY_HIT_MULTIPLIER = 1.1F;
    private static final double SPRINT_MULTIPLIER = 1.3D;

    private StatDisplayValues() {
    }

    /** Purist's damage multiplier; Wold's Vaults applies it on top of Damage Increase rather than adding to it. */
    public static float puristMultiplier(Player player) {
        float multiplier = 1.0F;
        for (TieredSkill node : ClientTalentData.getLearnedTalentNodes()) {
            LearnableSkill skill = node.getChild();
            if (skill instanceof PuristTalent purist) {
                multiplier += purist.getDamageIncrease() * (float) purist.getCount(player);
            }
        }
        return multiplier;
    }

    /** Total damage increase as shown on the screen: (1 + gear increase) x Purist, minus one. */
    public static float damageIncrease(Player player) {
        float increase = AttributeSnapshotHelper.getInstance().getSnapshot(player)
                .getAttributeValue(ModGearAttributes.DAMAGE_INCREASE, VaultGearAttributeTypeMerger.floatSum());
        return (1.0F + increase) * puristMultiplier(player) - 1.0F;
    }

    /** Movement speed above base from every source (gear, effects, abilities), with sprinting taken out. */
    public static float movementSpeed(Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null || speed.getBaseValue() <= 0.0D) {
            return 0.0F;
        }
        double value = speed.getValue();
        if (player.isSprinting()) {
            value /= SPRINT_MULTIPLIER;
        }
        return (float) (value / speed.getBaseValue() - 1.0D);
    }

    /**
     * Average damage per second: attack damage weighted by the capped lucky hit chance (gear, effects and talents),
     * times the gear damage increase and Purist multiplied as combat applies them, times the dynamic damage
     * multiplier and attack speed.
     */
    public static double averageDps(Player player) {
        double attackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
        double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double luckyHitChance = LuckyHitHelper.getLuckyHitChance(player);
        if (luckyHitChance != 0.0D) {
            float luckyHitMultiplier = DEFAULT_LUCKY_HIT_MULTIPLIER;
            for (TieredSkill node : ClientTalentData.getLearnedTalentNodes()) {
                if (node.getChild() instanceof DamageLuckyHitTalent talent) {
                    luckyHitMultiplier = 1.0F + talent.getDamageIncrease();
                }
            }
            attackDamage *= 1.0D - luckyHitChance + luckyHitMultiplier * luckyHitChance;
        }
        attackDamage *= 1.0D + damageIncrease(player);
        float dynamic = player instanceof ServerPlayer ? PlayerDamageHelper.getDamageMultiplier(player, true, false)
                : ClientDamageData.getCurrentDamageMultiplier();
        return attackDamage * dynamic * attackSpeed;
    }
}
