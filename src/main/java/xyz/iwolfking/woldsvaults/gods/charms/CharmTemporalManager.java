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
 * Server-side lifecycle for the mythic charm's toggleable temporal blessing. Activating adds the
 * charm's rolled vault modifier as its own modifier entry with the charm's remaining blessing
 * time as {@code TICKS_LEFT}; deactivating zeroes the LIVE entry's clock - the vault's own expiry
 * sweep then removes it - and banks the unspent time back onto the charm. The live context must
 * come from {@code Entry#getContext()}; {@code Modifiers#getContext(Entry)} hands out a detached
 * copy and writing the copy leaves the real entry ticking. Because every activation is an
 * independent modifier entry with its own context UUID, it composes with any other copy of the
 * same modifier already on the vault (a temporal, another player's charm, the crystal itself) and
 * removing ours can never touch theirs.
 *
 * <p>A once-per-second sweep settles the books if the player logs out, dies, leaves the vault or
 * unequips the charm while the blessing is running. The blessing follows its owner: the source
 * vault is resolved by its id across all server levels, so the entry is expired even when the
 * owner is no longer standing in that vault - otherwise a group could keep the modifier running
 * while the owner banks the time back.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class CharmTemporalManager {
    private record ActiveBlessing(UUID vaultId, UUID contextId, long activatedAt) {
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
        ACTIVE.put(player.getUUID(), new ActiveBlessing(vault.get(Vault.ID), contextId, player.getLevel().getGameTime()));
        player.displayClientMessage(new TextComponent("Blessing active: ")
                .append(modifier.getChatDisplayNameComponent(blessing.getCount()))
                .append(new TextComponent(" (" + remaining / 20 + "s)")).withStyle(ChatFormatting.GOLD), true);
    }

    private static void deactivate(ServerPlayer player, boolean announce) {
        ActiveBlessing active = ACTIVE.remove(player.getUUID());
        if (active == null) {
            return;
        }
        long elapsed = Math.max(0L, player.getLevel().getGameTime() - active.activatedAt());
        ItemStack charm = VaultCharmItem.getCharm(player).orElse(ItemStack.EMPTY);
        int remaining = 0;
        if (MythicVaultCharmItem.isMythic(charm)) {
            remaining = Math.max(0, MythicVaultCharmItem.getTemporalRemaining(charm) - (int) elapsed);
            MythicVaultCharmItem.setTemporalRemaining(charm, remaining);
        }
        expireVaultEntry(player, active);
        if (announce) {
            player.displayClientMessage(new TextComponent("Blessing withdrawn (" + remaining / 20 + "s banked).")
                    .withStyle(ChatFormatting.GOLD), true);
        }
    }

    /**
     * Zeroes the remaining time of EVERY entry carrying our context UUID so the vault's own
     * expiry sweep removes them - grouped modifiers (Rock Solid and friends) flatten into one
     * entry per child, all stamped with the same UUID at activation, so stopping at the first
     * match would leave the other children running forever. The source vault is looked up by id
     * across all server levels rather than through the player's current level, so a blessing is
     * withdrawn even when its owner has already left the vault. Touching only LIVE contexts
     * ({@code Entry#getContext()} - {@code Modifiers#getContext(Entry)} returns a detached copy)
     * of our own entries is what keeps other copies of the same modifier untouched.
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

    /**
     * Settles a running blessing when its preconditions stop holding: player left the vault, the
     * charm is gone or out of time. Runs once a second on the server tick.
     */
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
            ItemStack charm = VaultCharmItem.getCharm(player).orElse(ItemStack.EMPTY);
            long elapsed = player.getLevel().getGameTime() - active.activatedAt();
            boolean outOfTime = !MythicVaultCharmItem.isMythic(charm)
                    || MythicVaultCharmItem.getTemporalRemaining(charm) - elapsed <= 0;
            if (!inSameVault || outOfTime) {
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
