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

/** Overcrit and Luckiest Hit, called from mixins on the base lucky-hit pipeline. */
public final class IdonaLuckyHit {
    private IdonaLuckyHit() {
    }

    /** Luckiest Hit's chance side: scales the unlimited lucky hit chance down, once per point. */
    public static float scaleChance(LivingEntity entity, float chance) {
        if (!(entity instanceof ServerPlayer player)) {
            return chance;
        }
        int points = IdonaNodes.points(player, IdonaNodes.LUCKIEST_HIT);
        if (points <= 0) {
            return chance;
        }
        float scale = IdonaNodeHandlers.params(IdonaNodes.LUCKIEST_HIT,
                IdonaNodeHandlers.LuckiestHitParams.class).chance_scale();
        return chance * (float) Math.pow(scale, points);
    }

    /** Runs once per lucky hit that procced. */
    public static void onLuckyHitResolved(ServerPlayer attacker, LivingHurtEvent event) {
        applyOvercrit(attacker, event);
        applyLuckiestHit(attacker, event);
    }

    /** Overcrit: each whole 100% of lucky hit chance above the cap is a tier worth one hit's damage. */
    private static void applyOvercrit(ServerPlayer attacker, LivingHurtEvent event) {
        int points = IdonaNodes.points(attacker, IdonaNodes.OVERCRIT);
        if (points <= 0) {
            return;
        }
        float unlimited = LuckyHitHelper.getLuckyHitChanceUnlimited(attacker);
        float limit = Math.min(AttributeLimitHelper.getLuckyHitChanceLimit(attacker), 1.0F);
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

    /** Luckiest Hit's payoff: applies Fatal Strike, Mana Leech and Life Leech a second time. */
    private static void applyLuckiestHit(ServerPlayer attacker, LivingHurtEvent event) {
        if (!IdonaNodes.isActive(attacker, IdonaNodes.LUCKIEST_HIT)) {
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
