package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;
import xyz.iwolfking.woldsvaults.gods.combat.VaultClockRate;

import java.util.List;
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
 * <p>Neither of those is a per-player quantity, so neither can be expressed by a handler acting on
 * the player it ticks for. {@link #reconcile} is the one pass that recomputes the whole vault from
 * its current runners; the two tick contributors in {@link WendarrTimeHandlers} drive it on the
 * shared cadence, their deactivation drives it when a holder loses the node or logs out, and the
 * listener-leave hook below drives it when a holder walks out of the vault. Clock rate factors are
 * per vault and in memory only, which is why the pass re-applies rather than only sets.
 */
public final class WendarrClockNodes {
    public static final ResourceLocation OMEGA_FORTUNE_SMALL = WoldsVaults.id("omega_fortune_small");

    private static final ResourceLocation SPEED_DEMON_DAMAGE_KEY = WoldsVaults.id("wendarr_speed_demon_damage");
    private static final UUID SPEED_DEMON_ARMOR_UUID = UUID.fromString("6b6c9a2e-4d1f-4f8c-9d2a-51f0b7c3a911");
    private static final Object OWNER = new Object();

    private WendarrClockNodes() {
    }

    static void register() {
        CommonEvents.LISTENER_LEAVE.register(OWNER, data -> {
            data.getListener().getPlayer().ifPresent(WendarrClockNodes::clearAura);
            reconcile(data.getVault());
        });
        registerSpeedDemonStats();
    }

    /**
     * Recomputes one vault's clock rate factors and Speed Demon aura from the runners currently in
     * it. Idempotent, and safe to call with no vault - a holder who is not in a vault has nothing
     * to reconcile.
     */
    static void reconcile(Vault vault) {
        if (vault == null) {
            return;
        }
        List<ServerPlayer> runners = WendarrVaultTime.runners(vault);
        boolean speedDemon = false;
        boolean quickSearch = false;
        for (ServerPlayer player : runners) {
            speedDemon |= WendarrNodes.isActive(player, WendarrNodes.SPEED_DEMON);
            quickSearch |= WendarrNodes.isActive(player, WendarrNodes.QUICK_SEARCH);
        }
        applyRate(vault, WendarrNodes.key(WendarrNodes.SPEED_DEMON),
                WendarrNodeHandlers.params(WendarrNodes.SPEED_DEMON,
                        WendarrNodeHandlers.SpeedDemonParams.class).rate(), speedDemon);
        applyRate(vault, WendarrNodes.key(WendarrNodes.QUICK_SEARCH),
                WendarrNodeHandlers.params(WendarrNodes.QUICK_SEARCH,
                        WendarrNodeHandlers.QuickSearchParams.class).rate(), quickSearch);
        if (quickSearch) {
            attachOmegaFortune(vault);
        }
        applySpeedDemonAura(runners, speedDemon);
    }

    /**
     * Takes the Speed Demon aura off one player unconditionally. The removals are idempotent on
     * purpose: the shared teardown may already have dropped this player's scratch by the time a
     * leave or a logout reaches here, and a conditional removal would then leave the damage factor
     * and the armour modifier applied with nothing left to notice them.
     */
    static void clearAura(ServerPlayer player) {
        GodNodeState.clear(player.getUUID(), WendarrNodes.SPEED_DEMON);
        GlobalDamageMultiplierRegistry.remove(player, SPEED_DEMON_DAMAGE_KEY);
        applyArmorBonus(player, 1.0F);
    }

    private static void applyRate(Vault vault, ResourceLocation key, float factor, boolean active) {
        if (active) {
            VaultClockRate.setRateFactor(vault, key, factor);
        } else {
            VaultClockRate.removeRateFactor(vault, key);
        }
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

    private static void applySpeedDemonAura(List<ServerPlayer> runners, boolean active) {
        float multiplier = WendarrNodeHandlers.params(WendarrNodes.SPEED_DEMON,
                WendarrNodeHandlers.SpeedDemonParams.class).stat_multiplier();
        for (ServerPlayer player : runners) {
            boolean had = GodNodeState.peek(player.getUUID(), WendarrNodes.SPEED_DEMON).isPresent();
            if (active) {
                GodNodeState.put(player.getUUID(), WendarrNodes.SPEED_DEMON, Boolean.TRUE);
                GlobalDamageMultiplierRegistry.register(player, SPEED_DEMON_DAMAGE_KEY, multiplier);
                if (!had) {
                    applyArmorBonus(player, multiplier);
                }
            } else if (had) {
                clearAura(player);
            }
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

    private static void registerSpeedDemonStats() {
        for (PlayerStat stat : List.of(PlayerStat.ITEM_QUANTITY, PlayerStat.ITEM_RARITY,
                PlayerStat.TRAP_DISARM_CHANCE, PlayerStat.ABILITY_POWER_MULTIPLIER)) {
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
