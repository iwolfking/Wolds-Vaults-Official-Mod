package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.entity.VaultBoss;
import iskallia.vault.entity.boss.VaultBossEntity;
import iskallia.vault.entity.champion.ChampionLogic;
import iskallia.vault.entity.entity.elite.EliteModifierImmunity;
import net.minecraft.world.entity.Entity;

public class VaultMobUtils {
    public static final String BRUTAL_BOSS_TAG = "woldsvaults_brutal_boss";

    public static boolean isEliteMob(Entity entity) {
        return entity instanceof EliteModifierImmunity;
    }

    public static boolean isSpecialMob(Entity entity) {
        return isEliteMob(entity) || ChampionLogic.isChampion(entity) || entity instanceof VaultBoss;
    }

    public static boolean isBrutalBoss(Entity entity) {
        return entity.getTags().contains(BRUTAL_BOSS_TAG);
    }

    /**
     * Checks for the heavyweight boss tier: rune boss entities (including the hyper
     * objective's hyperbosses, which are escalated rune bosses) and Brutal Bosses
     * objective spawns.
     */
    public static boolean isMajorBoss(Entity entity) {
        return entity instanceof VaultBossEntity || isBrutalBoss(entity);
    }
}
