package xyz.iwolfking.woldsvaults.gods.trees.idona;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.core.event.common.SoulShardChanceEvent;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodCarryover;
import xyz.iwolfking.woldsvaults.gods.PietyBonusSource;

/**
 * Idona nodes that ride the base mod's player-stat and soul-shard events rather than the damage
 * pipeline. Registration is explicit (from {@link IdonaTree}) instead of a static initialiser so
 * the whole tree comes up in one place at a known point in mod setup.
 */
public final class IdonaStatHooks {
    private IdonaStatHooks() {
    }

    static void register() {
        CommonEvents.PLAYER_STAT.of(PlayerStat.STUN_CHANCE).register(IdonaStatHooks.class, IdonaStatHooks::thwack);
        CommonEvents.PLAYER_STAT.of(PlayerStat.ABILITY_POWER_MULTIPLIER)
                .register(IdonaStatHooks.class, IdonaStatHooks::abilityDamage);
        CommonEvents.SOUL_SHARD_CHANCE.register(IdonaStatHooks.class, IdonaStatHooks::soulstealer);
        PietyBonusSource.register(IdonaStatHooks::piousDevotion);
    }

    private static void thwack(PlayerStatEvent.Data data) {
        if (!(data.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        int points = IdonaNodes.minorPoints(player, IdonaNodes.THWACK);
        if (points <= 0 || !IdonaTargeting.isBattlestaff(player.getItemBySlot(EquipmentSlot.MAINHAND))) {
            return;
        }
        data.setValue(data.getValue() * (float) Math.pow(IdonaNodes.thwackMultiplier(), points));
    }

    /**
     * Grand Archmage's ability-damage doubling and Power Dump's mana-to-damage conversion both land
     * on the ability-power multiplier, which the base mod applies multiplicatively on a base of
     * 1.0 -  exactly the semantics both nodes are written in. The statistics screen invokes this
     * same player-stat event on the CLIENT, so the Archmage multiplier is also computed there from
     * the synced ledger - otherwise the screen under-reports while real damage is boosted. Power
     * Dump's transient surplus stays server-only by design.
     */
    private static void abilityDamage(PlayerStatEvent.Data data) {
        if (data.getEntity() instanceof ServerPlayer player) {
            float multiplier = 1.0F;
            int archmage = IdonaNodes.majorPoints(player, IdonaNodes.GRAND_ARCHMAGE);
            if (archmage > 0) {
                multiplier *= (float) Math.pow(IdonaNodes.archmageAbilityDamage(), archmage);
            }
            int powerDump = IdonaNodes.minorPoints(player, IdonaNodes.POWER_DUMP);
            if (powerDump > 0) {
                float extra = IdonaState.getPowerDumpExtra(player);
                if (extra > 0.0F) {
                    multiplier *= 1.0F + extra * IdonaNodes.powerDumpPerMana() * powerDump;
                }
            }
            if (multiplier != 1.0F) {
                data.setValue(data.getValue() * multiplier);
            }
            return;
        }
        if (data.getEntity() instanceof net.minecraft.world.entity.player.Player player
                && player.getLevel().isClientSide()) {
            int archmage = clientArchmagePoints(player);
            if (archmage > 0) {
                data.setValue(data.getValue() * (float) Math.pow(IdonaNodes.archmageAbilityDamage(), archmage));
            }
        }
    }

    private static int clientArchmagePoints(net.minecraft.world.entity.player.Player player) {
        if (xyz.iwolfking.woldsvaults.gods.ActiveGodResolver.getActiveGod(player).orElse(null)
                != iskallia.vault.core.vault.influence.VaultGod.IDONA) {
            return 0;
        }
        return xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData
                .getSpentLedger(iskallia.vault.core.vault.influence.VaultGod.IDONA)
                .getOrDefault(IdonaNodes.GRAND_ARCHMAGE, 0);
    }

    /**
     * Soulstealer multiplies the soul chance, not the shard count: the count is rolled from a
     * private lambda inside the mob-death logic with no event of its own, while the chance is a
     * first-class event and is what feeds that roll.
     */
    private static void soulstealer(SoulShardChanceEvent.Data data) {
        ServerPlayer player = data.getKiller();
        if (player == null) {
            return;
        }
        int points = IdonaNodes.minorPoints(player, IdonaNodes.SOULSTEALER);
        if (points <= 0) {
            return;
        }
        data.setChance(data.getChance() * (float) Math.pow(IdonaNodes.soulstealerMultiplier(), points));
    }

    /**
     * Pious Devotion grants piety toward Idona. Like every other Idona stat node it carries over
     * when another god is equipped, at the same quarter rate the attribute fold uses, so the value
     * of investing in the tree does not vanish the moment the charm changes.
     */
    private static int piousDevotion(Player player, VaultGod god) {
        if (god != IdonaNodes.GOD || !(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        int points = IdonaNodes.ledgerPoints(serverPlayer, IdonaNodes.PIOUS_DEVOTION);
        if (points <= 0) {
            return 0;
        }
        float total = IdonaNodes.valueAt(IdonaNodes.PIOUS_DEVOTION, points);
        if (ActiveGodResolver.isActive(serverPlayer, IdonaNodes.GOD)) {
            return Math.round(total);
        }
        return Math.round(total * GodCarryover.FOREIGN_TREE_SCALE);
    }
}
