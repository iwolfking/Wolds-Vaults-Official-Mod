package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.talent.type.luckyhit.DamageLuckyHitTalent;
import iskallia.vault.skill.talent.type.luckyhit.HealthLeechLuckyHitTalent;
import iskallia.vault.skill.talent.type.luckyhit.LuckyHitTalent;
import iskallia.vault.skill.talent.type.luckyhit.ManaLeechLuckyHitTalent;
import iskallia.vault.skill.tree.TalentTree;
import iskallia.vault.util.calc.AttributeLimitHelper;
import iskallia.vault.util.calc.LuckyHitHelper;
import iskallia.vault.world.data.PlayerTalentsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;

/**
 * Overcrit and Luckiest Hit -  the two Idona nodes that change what a lucky hit is. Both are driven
 * from mixins on the base lucky-hit pipeline; this class holds the logic so that a missing mixin
 * registration leaves the nodes inert rather than breaking anything.
 */
public final class IdonaLuckyHit {
    private IdonaLuckyHit() {
    }

    /**
     * Luckiest Hit's chance side: lucky hits become an order of magnitude rarer. Applied to the
     * unlimited chance so the clamp still sees a sane value, and so Overcrit reads the reduced
     * number too -  with both nodes taken, overcritting is meant to be nearly unreachable.
     */
    public static float scaleChance(LivingEntity entity, float chance) {
        if (!(entity instanceof ServerPlayer player)) {
            return chance;
        }
        int points = IdonaNodes.minorPoints(player, IdonaNodes.LUCKIEST_HIT);
        if (points <= 0) {
            return chance;
        }
        return chance * (float) Math.pow(IdonaNodes.LUCKIEST_HIT_CHANCE_SCALE, points);
    }

    /**
     * Runs once per lucky hit that actually procced. Applies Overcrit's stacked-lucky-hit damage
     * multiplier and re-runs the three talents Luckiest Hit doubles.
     */
    public static void onLuckyHitResolved(ServerPlayer attacker, LivingHurtEvent event) {
        applyOvercrit(attacker, event);
        applyLuckiestHit(attacker, event);
    }

    /**
     * Lucky hit chance above the cap is wasted today -  the roll is a single comparison against the
     * clamped value. Overcrit turns that waste into tiers: every whole 100% of excess chance is a
     * guaranteed extra tier, the fractional remainder rolls for one more, and the hit is multiplied
     * by {@code 1 + tier}. At exactly the cap the excess is zero and the node does nothing, which
     * is what "lucky hit chance over 100" means in a pack whose cap is 100%.
     */
    private static void applyOvercrit(ServerPlayer attacker, LivingHurtEvent event) {
        int points = IdonaNodes.minorPoints(attacker, IdonaNodes.OVERCRIT);
        if (points <= 0) {
            return;
        }
        float unlimited = LuckyHitHelper.getLuckyHitChanceUnlimited(attacker);
        float limit = AttributeLimitHelper.getLuckyHitChanceLimit(attacker);
        float excess = unlimited - limit;
        if (excess <= 0.0F || !Float.isFinite(excess)) {
            return;
        }
        int tier = (int) excess;
        if (attacker.getRandom().nextFloat() < excess - tier) {
            tier++;
        }
        if (tier <= 0) {
            return;
        }
        event.setAmount(event.getAmount() * (1.0F + (float) tier * points));
    }

    /**
     * Luckiest Hit's payoff: exactly three lucky-hit talents apply a second time -  Fatal Strike
     * (damage), Mana Leech and Life Leech. The list is an explicit allowlist rather than "every
     * lucky hit effect", because doubling Fanged Strike or echoing damage re-opens the known
     * exponential damage loop.
     */
    private static void applyLuckiestHit(ServerPlayer attacker, LivingHurtEvent event) {
        if (!IdonaNodes.isMinorActive(attacker, IdonaNodes.LUCKIEST_HIT)) {
            return;
        }
        TalentTree tree = PlayerTalentsData.get(attacker.getLevel()).getTalents(attacker);
        List<LuckyHitTalent> talents = tree.getAll(LuckyHitTalent.class, Skill::isUnlocked);
        for (LuckyHitTalent talent : talents) {
            if (!isDoubled(talent)) {
                continue;
            }
            try {
                talent.onLuckyHit(event);
            } catch (RuntimeException e) {
                WoldsVaults.LOGGER.error("Luckiest Hit failed to re-apply {}; its second application was skipped.",
                        talent.getClass().getSimpleName(), e);
            }
        }
    }

    private static boolean isDoubled(LuckyHitTalent talent) {
        return talent instanceof DamageLuckyHitTalent
                || talent instanceof ManaLeechLuckyHitTalent
                || talent instanceof HealthLeechLuckyHitTalent;
    }
}
