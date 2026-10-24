package xyz.iwolfking.woldsvaults.integration.bettercombat;

import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotCalculator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class DualWieldSnapshotComputer {
    private static final ThreadLocal<ItemStack> WEAPON_OVERRIDE =
            new ThreadLocal<>();

    private DualWieldSnapshotComputer() {}

    public static ItemStack getWeaponOverride() {
        return WEAPON_OVERRIDE.get();
    }

    public static void computeForWeapon(
            ServerPlayer player,
            ItemStack weapon,
            AttributeSnapshot snapshot
    ) {
        WEAPON_OVERRIDE.set(weapon);
        try {
            AttributeSnapshotCalculator.computeSnapshot(player, snapshot);
        } finally {
            WEAPON_OVERRIDE.remove();
        }
    }
}
