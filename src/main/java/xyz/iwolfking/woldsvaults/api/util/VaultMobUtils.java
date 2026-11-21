package xyz.iwolfking.woldsvaults.api.util;

import iskallia.vault.core.vault.modifier.spi.predicate.IModifierImmunity;
import iskallia.vault.entity.VaultBoss;
import iskallia.vault.entity.champion.ChampionLogic;
import net.minecraft.world.entity.Entity;

public class VaultMobUtils {
    public static boolean isEliteMob(Entity entity) {
        return entity instanceof IModifierImmunity;
    }

    public static boolean isSpecialMob(Entity entity) {
        return isEliteMob(entity) || ChampionLogic.isChampion(entity) || entity instanceof VaultBoss;
    }
}
