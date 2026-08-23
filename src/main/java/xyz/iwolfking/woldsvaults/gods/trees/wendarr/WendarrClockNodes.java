package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.util.calc.PlayerStat;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.GodVanillaAttributes;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;
import xyz.iwolfking.woldsvaults.gods.combat.VaultClockRate;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeTicker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The party-wide half of Speed Demon (r87) and Quick Search (r88).
 *
 * <p>Both shorten the vault second through the wave-1 clock-rate primitive, and each uses a single
 * fixed factor key, so a second player with the same node changes nothing ("does not stack") while
 * the two different nodes still multiply with each other. Speed Demon's compensation - the stat
 * multiplier, the global damage factor and the armour bonus - is an aura on every runner of the
 * vault, not only on the holder.
 *
 * <p>Both are vault-sticky by design. The first live holder marks the vault in the per-vault
 * scratch of {@link GodNodeState} and applies the clock rate, the aura and Quick Search's room
 * modifier; nothing takes any of that off again for the rest of the run. A holder who leaves, logs
 * out, respecs or swaps a charm does not get to slow a clock the whole party has already been
 * racing against, and the minutes a faster clock has already spent cannot be handed back. The
 * vault dies with its clock, and vault end drops the scratch with it.
 *
 * <p>{@link #reconcile} is that one pass. It is driven per vault, not per holder: a
 * {@link GodNodeTicker.TreePass} runs it once a second for every vault that has an online runner,
 * so a marked vault keeps applying long after its last holder has gone - a runner who joins a
 * marked vault after the holder left still gets the aura, because the mark belongs to the vault.
 * Every application it makes is conditional on that application being absent, so a repeat pass is
 * a no-op and only genuinely missing state is restored - the aura on a late joiner, or a clock rate
 * factor, which is per vault and in memory only. What the pass never does is remove; the marks are
 * one-way.
 *
 * <p>The aura's per-player half is still cleaned up per player, by {@link #clearAura} on
 * vault-listener leave and on the holder's own deactivation: the damage factor and the armour
 * modifier are transient state on the player, not on the vault, and would otherwise follow the
 * player out of the vault.
 */
public final class WendarrClockNodes {
    public static final ResourceLocation OMEGA_FORTUNE_SMALL = WoldsVaults.id("omega_fortune_small");

    private static final ResourceLocation SPEED_DEMON_DAMAGE_KEY = WoldsVaults.id("wendarr_speed_demon_damage");
    private static final UUID SPEED_DEMON_ARMOR_UUID = GodVanillaAttributes.modifierId(
            "wendarr_speed_demon", Attributes.ARMOR, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final Object OWNER = new Object();

    private WendarrClockNodes() {
    }

    static void register() {
        CommonEvents.LISTENER_LEAVE.register(OWNER,
                data -> data.getListener().getPlayer().ifPresent(WendarrClockNodes::clearAura));
        GodNodeTicker.registerTreePass(WendarrClockNodes::pass);
        registerSpeedDemonStats();
    }

    /**
     * The ticker pass: reconciles each vault that has at least one online runner, once, whether or
     * not anyone in it still holds either node.
     */
    private static void pass(MinecraftServer server, List<ServerPlayer> players) {
        Set<UUID> seen = new HashSet<>();
        for (ServerPlayer player : players) {
            Vault vault = ServerVaults.get(player.level).orElse(null);
            if (vault == null || !vault.has(Vault.ID) || !seen.add(vault.get(Vault.ID))) {
                continue;
            }
            reconcile(vault);
        }
    }

    /**
     * Marks one vault for whichever of the two nodes a runner currently holds, then makes sure
     * everything either mark stands for is in place. Idempotent, and safe to call with no vault - a
     * holder who is not in a vault has nothing to reconcile.
     */
    static void reconcile(Vault vault) {
        UUID vaultId = vault != null && vault.has(Vault.ID) ? vault.get(Vault.ID) : null;
        if (vaultId == null) {
            return;
        }
        List<ServerPlayer> runners = WendarrVaultTime.runners(vault);
        for (ServerPlayer player : runners) {
            if (WendarrNodes.isActive(player, WendarrNodes.SPEED_DEMON)) {
                mark(vaultId, WendarrNodes.SPEED_DEMON);
            }
            if (WendarrNodes.isActive(player, WendarrNodes.QUICK_SEARCH)) {
                mark(vaultId, WendarrNodes.QUICK_SEARCH);
            }
        }
        if (isMarked(vaultId, WendarrNodes.SPEED_DEMON)) {
            applyRate(vault, WendarrNodes.key(WendarrNodes.SPEED_DEMON),
                    WendarrNodeHandlers.params(WendarrNodes.SPEED_DEMON,
                            WendarrNodeHandlers.SpeedDemonParams.class).rate());
            applySpeedDemonAura(runners);
        }
        if (isMarked(vaultId, WendarrNodes.QUICK_SEARCH)) {
            applyRate(vault, WendarrNodes.key(WendarrNodes.QUICK_SEARCH),
                    WendarrNodeHandlers.params(WendarrNodes.QUICK_SEARCH,
                            WendarrNodeHandlers.QuickSearchParams.class).rate());
            attachOmegaFortune(vault);
        }
    }

    /**
     * Takes the Speed Demon aura off one player unconditionally. The removals are idempotent on
     * purpose: the shared teardown may already have dropped this player's scratch by the time a
     * leave or a logout reaches here, and a conditional removal would then leave the damage factor
     * and the armour modifier applied with nothing left to notice them. The vault's own mark is
     * untouched, so a runner who is still in it has the aura back on the next tick.
     */
    static void clearAura(ServerPlayer player) {
        GodNodeState.clear(player.getUUID(), WendarrNodes.SPEED_DEMON);
        GlobalDamageMultiplierRegistry.remove(player, SPEED_DEMON_DAMAGE_KEY);
        applyArmorBonus(player, 1.0F);
    }

    private static void mark(UUID vaultId, String effectId) {
        GodNodeState.getVault(vaultId, effectId, () -> Boolean.TRUE);
    }

    private static boolean isMarked(UUID vaultId, String effectId) {
        return GodNodeState.peekVault(vaultId, effectId).isPresent();
    }

    /**
     * Clock rate factors are per vault and in memory only, so the pass sets the factor when the
     * vault is not already carrying it and leaves it alone when it is.
     */
    private static void applyRate(Vault vault, ResourceLocation key, float factor) {
        if (VaultClockRate.view(vault).containsKey(key)) {
            return;
        }
        VaultClockRate.setRateFactor(vault, key, factor);
    }

    /** Quick Search's room half is the shipped 4x omega weight modifier, attached once per vault. */
    private static void attachOmegaFortune(Vault vault) {
        if (VaultModifierUtils.getCountOfModifiers(vault, OMEGA_FORTUNE_SMALL) > 0) {
            return;
        }
        VaultModifier<?> modifier = VaultModifierRegistry.get(OMEGA_FORTUNE_SMALL);
        if (modifier == null) {
            WoldsVaults.LOGGER.error("Quick Search could not find vault modifier {}; the omega room half did nothing.", OMEGA_FORTUNE_SMALL);
            return;
        }
        VaultModifierUtils.addModifier(vault, OMEGA_FORTUNE_SMALL, 1);
    }

    private static void applySpeedDemonAura(List<ServerPlayer> runners) {
        float multiplier = WendarrNodeHandlers.params(WendarrNodes.SPEED_DEMON,
                WendarrNodeHandlers.SpeedDemonParams.class).stat_multiplier();
        for (ServerPlayer player : runners) {
            if (GodNodeState.peek(player.getUUID(), WendarrNodes.SPEED_DEMON).isPresent()) {
                continue;
            }
            GodNodeState.put(player.getUUID(), WendarrNodes.SPEED_DEMON, Boolean.TRUE);
            GlobalDamageMultiplierRegistry.register(player, SPEED_DEMON_DAMAGE_KEY, multiplier);
            applyArmorBonus(player, multiplier);
        }
    }

    /** Armour is a vanilla attribute, not a {@code PlayerStat}, so it needs its own modifier. */
    private static void applyArmorBonus(ServerPlayer player, float multiplier) {
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor == null) {
            return;
        }
        armor.removeModifier(SPEED_DEMON_ARMOR_UUID);
        if (multiplier != 1.0F) {
            armor.addTransientModifier(new AttributeModifier(SPEED_DEMON_ARMOR_UUID, "WendarrSpeedDemonArmor",
                    multiplier - 1.0F, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    /**
     * The non-damage half of Speed Demon. Ability power is deliberately absent: the aura already
     * registers a factor in the global damage registry, which scales every hit whose true source
     * is a player, so listening on {@code ABILITY_POWER_MULTIPLIER} as well squared the multiplier
     * on every ability.
     */
    private static void registerSpeedDemonStats() {
        for (PlayerStat stat : List.of(PlayerStat.ITEM_QUANTITY, PlayerStat.ITEM_RARITY,
                PlayerStat.TRAP_DISARM_CHANCE)) {
            CommonEvents.PLAYER_STAT.of(stat).register(OWNER, data -> {
                if (!(data.getEntity() instanceof ServerPlayer player)) {
                    return;
                }
                if (GodNodeState.peek(player.getUUID(), WendarrNodes.SPEED_DEMON).isEmpty()) {
                    return;
                }
                data.setValue(data.getValue() * WendarrNodeHandlers.params(WendarrNodes.SPEED_DEMON,
                        WendarrNodeHandlers.SpeedDemonParams.class).stat_multiplier());
            });
        }
    }
}
