package xyz.iwolfking.woldsvaults.medallions;

import iskallia.vault.VaultMod;
import iskallia.vault.entity.entity.EternalEntity;
import iskallia.vault.init.ModConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/**
 * What assassin fury and Champion rage read from a kill: the player it is credited to, and the vault entity group of
 * the mob that died.
 */
public final class GreedKills {
    private static final ResourceLocation GROUP_TANK = VaultMod.id("tank");
    private static final ResourceLocation GROUP_HORDE = VaultMod.id("horde");
    private static final ResourceLocation GROUP_ASSASSIN = VaultMod.id("assassin");

    private GreedKills() {
    }

    /** The player credited with a kill, following an Eternal back to its owner; null for anything else. */
    public static ServerPlayer resolveKiller(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (killer instanceof EternalEntity eternal) {
            killer = eternal.getOwner().right().orElse(null);
        }
        return killer instanceof ServerPlayer player ? player : null;
    }

    public static boolean isTank(Entity entity) {
        return ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_TANK, entity);
    }

    public static boolean isHorde(Entity entity) {
        return ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_HORDE, entity);
    }

    /** Whether the mob is in VH's {@code assassin} mob group; not whether it is a greed assassin. */
    public static boolean isAssassinType(Entity entity) {
        return ModConfigs.ENTITY_GROUPS.isInGroup(GROUP_ASSASSIN, entity);
    }
}
