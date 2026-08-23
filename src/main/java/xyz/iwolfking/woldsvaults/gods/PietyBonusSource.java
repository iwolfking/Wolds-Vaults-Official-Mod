package xyz.iwolfking.woldsvaults.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Additive piety beyond reputation and god level, summed into {@link GodAlignmentData#piety}. */
@FunctionalInterface
public interface PietyBonusSource {
    List<PietyBonusSource> SOURCES = new CopyOnWriteArrayList<>();

    int getBonusPiety(Player player, VaultGod god);

    static void register(PietyBonusSource source) {
        SOURCES.add(source);
    }

    static int total(Player player, VaultGod god) {
        int total = 0;
        for (PietyBonusSource source : SOURCES) {
            total += source.getBonusPiety(player, god);
        }
        return total;
    }
}
