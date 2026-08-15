package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.modifier.GreedExtension;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;
import xyz.iwolfking.woldsvaults.gods.combat.VaultClockRate;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Wendarr nodes that act on the vault itself: Extender (r76), Speed Demon (r87) and
 * Quick Search (r88).
 *
 * <p>Speed Demon and Quick Search both shorten the vault second through the wave-1 clock-rate
 * primitive. Each uses a single fixed factor key, so a second player with the same node changes
 * nothing ("does not stack") while the two different nodes still multiply with each other. Clock
 * rate factors are per vault and in memory only, so the reconcile below re-applies them on a one
 * second cadence rather than only on join.
 */
public final class WendarrClockNodes {
    public static final int EXTENDER_TICKS = 4800;
    public static final float SPEED_DEMON_RATE = 0.75F;
    public static final float QUICK_SEARCH_RATE = 0.70F;
    public static final float SPEED_DEMON_STAT_MULTIPLIER = 1.1F;
    public static final ResourceLocation OMEGA_FORTUNE_SMALL = WoldsVaults.id("omega_fortune_small");

    private static final ResourceLocation SPEED_DEMON_RATE_KEY = WoldsVaults.id("wendarr_speed_demon");
    private static final ResourceLocation QUICK_SEARCH_RATE_KEY = WoldsVaults.id("wendarr_quick_search");
    private static final ResourceLocation SPEED_DEMON_DAMAGE_KEY = WoldsVaults.id("wendarr_speed_demon_damage");
    private static final UUID SPEED_DEMON_ARMOR_UUID = UUID.fromString("6b6c9a2e-4d1f-4f8c-9d2a-51f0b7c3a911");
    private static final int RECONCILE_INTERVAL_TICKS = 20;

    private static final Set<UUID> SPEED_DEMON_PLAYERS = ConcurrentHashMap.newKeySet();
    private static final Set<String> EXTENDED_GRANTED = ConcurrentHashMap.newKeySet();
    private static final Object OWNER = new Object();

    private WendarrClockNodes() {
    }

    static void register() {
        CommonEvents.LISTENER_JOIN.register(OWNER, data -> onJoin(data.getVault()));
        CommonEvents.LISTENER_LEAVE.register(OWNER, data -> {
            data.getListener().getPlayer().ifPresent(player -> EXTENDED_GRANTED.remove(extenderKey(data.getVault(), player)));
            reconcile(data.getVault());
        });
        CommonEvents.LISTENER_TICK.register(OWNER, data -> {
            Vault vault = data.getVault();
            TickClock clock = vault.get(Vault.CLOCK);
            if (clock != null && clock.get(TickClock.GLOBAL_TIME) % RECONCILE_INTERVAL_TICKS == 0) {
                reconcile(vault);
            }
        });
        registerSpeedDemonStats();
    }

    private static void onJoin(Vault vault) {
        for (ServerPlayer player : WendarrVaultTime.runners(vault)) {
            grantExtenderTime(vault, player);
        }
        reconcile(vault);
    }

    /**
     * Extender grants its time per player and stacks across a party, exactly like the shipped
     * greed vault-time node, so it reuses the base mod's own {@link GreedExtension} rather than
     * writing {@code DISPLAY_TIME} by hand.
     */
    private static void grantExtenderTime(Vault vault, ServerPlayer player) {
        if (!WendarrNodes.hasMinor(player, WendarrNodes.EXTENDER)) {
            return;
        }
        if (!EXTENDED_GRANTED.add(extenderKey(vault, player))) {
            return;
        }
        TickClock clock = vault.get(Vault.CLOCK);
        if (clock == null) {
            return;
        }
        clock.addModifier(new GreedExtension(player, EXTENDER_TICKS));
    }

    private static String extenderKey(Vault vault, ServerPlayer player) {
        return vault.get(Vault.ID) + ":" + player.getUUID();
    }

    private static void reconcile(Vault vault) {
        if (vault == null) {
            return;
        }
        List<ServerPlayer> runners = WendarrVaultTime.runners(vault);
        boolean speedDemon = false;
        boolean quickSearch = false;
        for (ServerPlayer player : runners) {
            speedDemon |= WendarrNodes.hasMinor(player, WendarrNodes.SPEED_DEMON);
            quickSearch |= WendarrNodes.hasMinor(player, WendarrNodes.QUICK_SEARCH);
        }
        applyRate(vault, SPEED_DEMON_RATE_KEY, SPEED_DEMON_RATE, speedDemon);
        applyRate(vault, QUICK_SEARCH_RATE_KEY, QUICK_SEARCH_RATE, quickSearch);
        if (quickSearch) {
            attachOmegaFortune(vault);
        }
        applySpeedDemonAura(runners, speedDemon);
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
        for (ServerPlayer player : runners) {
            boolean changed = active ? SPEED_DEMON_PLAYERS.add(player.getUUID()) : SPEED_DEMON_PLAYERS.remove(player.getUUID());
            if (active) {
                GlobalDamageMultiplierRegistry.register(player, SPEED_DEMON_DAMAGE_KEY, SPEED_DEMON_STAT_MULTIPLIER);
            } else if (changed) {
                GlobalDamageMultiplierRegistry.remove(player, SPEED_DEMON_DAMAGE_KEY);
            }
            if (changed) {
                applyArmorBonus(player, active);
            }
        }
    }

    /** Armour is a vanilla attribute, not a {@code PlayerStat}, so it needs its own modifier. */
    private static void applyArmorBonus(ServerPlayer player, boolean active) {
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor == null) {
            return;
        }
        armor.removeModifier(SPEED_DEMON_ARMOR_UUID);
        if (active) {
            armor.addTransientModifier(new AttributeModifier(SPEED_DEMON_ARMOR_UUID, "WendarrSpeedDemonArmor",
                    SPEED_DEMON_STAT_MULTIPLIER - 1.0F, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    private static void registerSpeedDemonStats() {
        for (PlayerStat stat : List.of(PlayerStat.ITEM_QUANTITY, PlayerStat.ITEM_RARITY,
                PlayerStat.TRAP_DISARM_CHANCE, PlayerStat.ABILITY_POWER_MULTIPLIER)) {
            CommonEvents.PLAYER_STAT.of(stat).register(OWNER, data -> {
                if (data.getEntity() instanceof ServerPlayer player && SPEED_DEMON_PLAYERS.contains(player.getUUID())) {
                    data.setValue(data.getValue() * SPEED_DEMON_STAT_MULTIPLIER);
                }
            });
        }
    }

    public static void clearPlayer(UUID playerId) {
        SPEED_DEMON_PLAYERS.remove(playerId);
        EXTENDED_GRANTED.removeIf(key -> key.endsWith(":" + playerId));
    }
}
