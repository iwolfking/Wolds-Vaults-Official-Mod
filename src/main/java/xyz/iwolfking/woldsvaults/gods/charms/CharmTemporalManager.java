package xyz.iwolfking.woldsvaults.gods.charms;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.custom.RandomGodVaultModifierAttribute;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.item.gear.VaultCharmItem;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.items.gear.MythicVaultCharmItem;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side lifecycle for the mythic charm's toggleable temporal blessing: activating adds the
 * charm's rolled modifier as its own vault entry, clocked with the charm's remaining blessing time,
 * and deactivating zeroes that clock and banks the unspent time back.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class CharmTemporalManager {
    /** A running blessing; {@code settledAt} is when its cost was last banked, not the activation. */
    private record ActiveBlessing(UUID vaultId, UUID contextId, long settledAt, UUID charmId) {
    }

    private static final Map<UUID, ActiveBlessing> ACTIVE = new ConcurrentHashMap<>();

    private CharmTemporalManager() {
    }

    public static void toggle(ServerPlayer player) {
        if (ACTIVE.containsKey(player.getUUID())) {
            deactivate(player, true);
        } else {
            activate(player);
        }
    }

    private static void activate(ServerPlayer player) {
        Vault vault = ServerVaults.get(player.getLevel()).orElse(null);
        if (vault == null || !vault.has(Vault.ID)) {
            player.displayClientMessage(new TextComponent("The blessing only answers inside a vault.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        ItemStack charm = VaultCharmItem.getCharm(player).orElse(ItemStack.EMPTY);
        if (!MythicVaultCharmItem.isMythic(charm)) {
            player.displayClientMessage(new TextComponent("No mythic god charm equipped.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        RandomGodVaultModifierAttribute blessing = getBlessing(charm).orElse(null);
        if (blessing == null) {
            player.displayClientMessage(new TextComponent("This charm carries no blessing.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        int remaining = MythicVaultCharmItem.getTemporalRemaining(charm);
        if (remaining < 20) {
            player.displayClientMessage(new TextComponent("The charm's blessing is spent.")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        VaultModifier<?> modifier = VaultModifierRegistry.getOpt(blessing.getModifier()).orElse(null);
        if (modifier == null) {
            WoldsVaults.LOGGER.error("Mythic charm blessing references unknown vault modifier {}; cannot activate.",
                    blessing.getModifier());
            return;
        }
        UUID contextId = UUID.randomUUID();
        ((Modifiers) vault.get(Vault.MODIFIERS)).addModifier(modifier, blessing.getCount(), true,
                JavaRandom.ofNanoTime(), context -> {
                    context.set(ModifierContext.UUID, contextId);
                    context.set(ModifierContext.TICKS_LEFT, remaining);
                });
        ACTIVE.put(player.getUUID(), new ActiveBlessing(vault.get(Vault.ID), contextId,
                player.getLevel().getGameTime(), MythicVaultCharmItem.getOrCreateBlessingId(charm)));
        player.displayClientMessage(new TextComponent("Blessing active: ")
                .append(modifier.getChatDisplayNameComponent(blessing.getCount()))
                .append(new TextComponent(" (" + remaining / 20 + "s)")).withStyle(ChatFormatting.GOLD), true);
    }

    private static void deactivate(ServerPlayer player, boolean announce) {
        ActiveBlessing active = ACTIVE.remove(player.getUUID());
        if (active == null) {
            return;
        }
        int remaining = settle(player, active);
        expireVaultEntry(player, active);
        if (announce) {
            player.displayClientMessage(new TextComponent("Blessing withdrawn ("
                    + Math.max(0, remaining) / 20 + "s banked).").withStyle(ChatFormatting.GOLD), true);
        }
    }

    /** Banks the elapsed time against the charm; returns what is left, or -1 if it is gone. */
    private static int settle(ServerPlayer player, ActiveBlessing active) {
        ItemStack charm = MythicVaultCharmItem.findByBlessingId(player, active.charmId());
        if (charm.isEmpty()) {
            WoldsVaults.LOGGER.warn("{} no longer holds the charm backing their blessing; {} ticks went unbanked.",
                    player.getGameProfile().getName(),
                    Math.max(0L, player.getLevel().getGameTime() - active.settledAt()));
            return -1;
        }
        long elapsed = Math.max(0L, player.getLevel().getGameTime() - active.settledAt());
        int remaining = Math.max(0, MythicVaultCharmItem.getTemporalRemaining(charm) - (int) elapsed);
        MythicVaultCharmItem.setTemporalRemaining(charm, remaining);
        return remaining;
    }

    /**
     * Zeroes the remaining time of every entry carrying our context UUID, for the vault's own
     * expiry sweep to remove. Writes live contexts only; {@code Modifiers#getContext} copies.
     */
    private static void expireVaultEntry(ServerPlayer player, ActiveBlessing active) {
        net.minecraft.server.MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        for (net.minecraft.server.level.ServerLevel level : server.getAllLevels()) {
            Vault vault = ServerVaults.get(level).orElse(null);
            if (vault == null || !vault.has(Vault.ID) || !vault.get(Vault.ID).equals(active.vaultId())) {
                continue;
            }
            Modifiers modifiers = vault.get(Vault.MODIFIERS);
            for (Modifiers.Entry entry : modifiers.getEntries()) {
                ModifierContext context = entry.getContext();
                if (context != null && active.contextId().equals(context.getUUID())) {
                    context.set(ModifierContext.TICKS_LEFT, 0);
                }
            }
            return;
        }
    }

    /** Settles a running blessing once a second, ending it if the vault, charm or time is gone. */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }
        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (Map.Entry<UUID, ActiveBlessing> tracked : ACTIVE.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(tracked.getKey());
            if (player == null) {
                ACTIVE.remove(tracked.getKey());
                continue;
            }
            if (player.getLevel().getGameTime() % 20L != 0L) {
                continue;
            }
            ActiveBlessing active = tracked.getValue();
            boolean inSameVault = ServerVaults.get(player.getLevel())
                    .map(vault -> vault.has(Vault.ID) && vault.get(Vault.ID).equals(active.vaultId()))
                    .orElse(false);
            int remaining = settle(player, active);
            if (remaining < 0) {
                ACTIVE.remove(tracked.getKey());
                expireVaultEntry(player, active);
                continue;
            }
            ACTIVE.put(tracked.getKey(), new ActiveBlessing(active.vaultId(), active.contextId(),
                    player.getLevel().getGameTime(), active.charmId()));
            if (!inSameVault || remaining <= 0) {
                deactivate(player, inSameVault);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            deactivate(player, false);
        }
    }

    public static Optional<RandomGodVaultModifierAttribute> getBlessing(ItemStack charm) {
        if (!MythicVaultCharmItem.isMythic(charm)) {
            return Optional.empty();
        }
        VaultGearData data = VaultGearData.read(charm);
        for (VaultGearModifier<?> modifier : data.getModifiers(VaultGearModifier.AffixType.IMPLICIT)) {
            if (modifier.getValue() instanceof RandomGodVaultModifierAttribute blessing) {
                return Optional.of(blessing);
            }
        }
        return Optional.empty();
    }
}
