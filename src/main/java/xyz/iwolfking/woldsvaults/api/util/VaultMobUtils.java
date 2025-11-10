package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.entity.VaultBoss;
import iskallia.vault.entity.champion.ChampionLogic;
import iskallia.vault.entity.entity.elite.EliteModifierImmunity;
import net.minecraft.world.entity.Entity;

public class VaultMobUtils {
    public static boolean isEliteMob(Entity entity) {
        return entity instanceof EliteModifierImmunity;
    }

    public static boolean isSpecialMob(Entity entity) {
        return isEliteMob(entity) || ChampionLogic.isChampion(entity) || entity instanceof VaultBoss;
    }
}
