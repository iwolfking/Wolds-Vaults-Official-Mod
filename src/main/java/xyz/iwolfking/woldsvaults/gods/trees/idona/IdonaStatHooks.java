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
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;

/** Idona nodes that ride the base mod's player-stat and soul-shard events. */
public final class IdonaStatHooks {
    private IdonaStatHooks() {
    }

    static void register() {
        CommonEvents.PLAYER_STAT.of(PlayerStat.STUN_CHANCE).register(IdonaStatHooks.class, IdonaStatHooks::thwack);
        CommonEvents.PLAYER_STAT.of(PlayerStat.ABILITY_POWER_MULTIPLIER)
                .register(IdonaStatHooks.class, IdonaStatHooks::abilityDamage);
        CommonEvents.SOUL_SHARD_CHANCE.register(IdonaStatHooks.class, IdonaStatHooks::soulstealer);
    }

    private static void thwack(PlayerStatEvent.Data data) {
        if (!(data.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        int points = IdonaNodes.points(player, IdonaNodes.THWACK);
        if (points <= 0 || !IdonaTargeting.isBattlestaff(player.getItemBySlot(EquipmentSlot.MAINHAND))) {
            return;
        }
        float multiplier = IdonaNodeHandlers.params(IdonaNodes.THWACK,
                IdonaNodeHandlers.ThwackParams.class).multiplier();
        data.setValue(data.getValue() * (float) Math.pow(multiplier, points));
    }

    /** Grand Archmage's ability damage, on both sides, and Power Dump's, server-side only. */
    private static void abilityDamage(PlayerStatEvent.Data data) {
        if (data.getEntity() instanceof ServerPlayer player) {
            float multiplier = 1.0F;
            int archmage = IdonaNodes.points(player, IdonaNodes.GRAND_ARCHMAGE);
            if (archmage > 0) {
                multiplier *= (float) Math.pow(archmageAbilityDamage(), archmage);
            }
            int powerDump = IdonaNodes.points(player, IdonaNodes.POWER_DUMP);
            if (powerDump > 0) {
                float extra = IdonaState.getPowerDumpExtra(player);
                if (extra > 0.0F) {
                    float perMana = IdonaNodeHandlers.params(IdonaNodes.POWER_DUMP,
                            IdonaNodeHandlers.PowerDumpParams.class).per_mana();
                    multiplier *= 1.0F + extra * perMana * powerDump;
                }
            }
            if (multiplier != 1.0F) {
                data.setValue(data.getValue() * multiplier);
            }
            return;
        }
        if (data.getEntity() instanceof Player player && player.getLevel().isClientSide()) {
            int archmage = clientArchmagePoints(player);
            if (archmage > 0) {
                data.setValue(data.getValue() * (float) Math.pow(archmageAbilityDamage(), archmage));
            }
        }
    }

    private static float archmageAbilityDamage() {
        return IdonaNodeHandlers.params(IdonaNodes.GRAND_ARCHMAGE,
                IdonaNodeHandlers.GrandArchmageParams.class).ability_damage();
    }

    private static int clientArchmagePoints(Player player) {
        if (ActiveGodResolver.getActiveGod(player).orElse(null) != VaultGod.IDONA) {
            return 0;
        }
        return ClientGodAlignmentData.getSpentLedger(VaultGod.IDONA).getOrDefault(IdonaNodes.GRAND_ARCHMAGE, 0);
    }

    /** Soulstealer multiplies the soul shard chance, not the shard count. */
    private static void soulstealer(SoulShardChanceEvent.Data data) {
        ServerPlayer player = data.getKiller();
        if (player == null) {
            return;
        }
        int points = IdonaNodes.points(player, IdonaNodes.SOULSTEALER);
        if (points <= 0) {
            return;
        }
        float multiplier = IdonaNodeHandlers.params(IdonaNodes.SOULSTEALER,
                IdonaNodeHandlers.SoulstealerParams.class).multiplier();
        data.setChance(data.getChance() * (float) Math.pow(multiplier, points));
    }
}
