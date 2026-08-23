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
 * The party-wide half of Speed Demon and Quick Search, each under one fixed clock-rate factor key
 * so neither stacks with itself. Both vault marks are one-way: once set, {@link #reconcile} keeps
 * applying them for the rest of the run.
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

    /** Reconciles each vault with at least one online runner, once per pass. */
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

    /** Marks the vault for whichever node a runner holds and applies both marks. Idempotent. */
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

    /** Takes the Speed Demon aura off one player. The vault's own mark is untouched. */
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

    /** Sets the clock rate factor unless the vault already carries that key. */
    private static void applyRate(Vault vault, ResourceLocation key, float factor) {
        if (VaultClockRate.view(vault).containsKey(key)) {
            return;
        }
        VaultClockRate.setRateFactor(vault, key, factor);
    }

    /** Quick Search's room half: attaches the shipped omega weight modifier once per vault. */
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

    /** The non-damage half of Speed Demon: item quantity, item rarity and trap disarm. */
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
