package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.PoolReferenceWeightModifier;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.world.processor.tile.VaultLootTileProcessor;
import iskallia.vault.block.PlaceholderBlock;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

import java.util.List;

/**
 * The Tenos nodes that shape the vault around the player: Omega Vault (r98) and Nose for
 * Treasure (r103).
 *
 * <p>Nose for Treasure is inherently vault wide - the placeholder event carries no player, which
 * matches the sheet, and one listener covering the whole vault is also what makes "does not stack"
 * true for free. Omega Vault attaches a room weight modifier once per vault, deduplicated by id
 * for the same reason.
 */
public final class TenosWorldNodes {
    public static final double OMEGA_VAULT_WEIGHT_MULTIPLIER = 2.0;
    public static final double TREASURE_DOOR_BONUS = 0.25;
    public static final ResourceLocation OMEGA_FORTUNE_DOUBLE = WoldsVaults.id("omega_fortune_double");
    public static final ResourceLocation OMEGA_ROOMS_POOL = new ResourceLocation("the_vault", "vault/rooms/omega_rooms");

    private static final int RECONCILE_INTERVAL_TICKS = 20;
    private static final Object OWNER = new Object();

    private TenosWorldNodes() {
    }

    static void register() {
        CommonEvents.PLACEHOLDER_GENERATION.register(OWNER, data -> {
            VaultLootTileProcessor parent = data.getParent();
            if (parent == null || parent.target != PlaceholderBlock.Type.TREASURE_DOOR) {
                return;
            }
            if (!TenosVaultUtil.anyRunnerHasMinor(data.getVault(), TenosNodes.NOSE_FOR_TREASURE)) {
                return;
            }
            data.setProbability(data.getProbability() + TREASURE_DOOR_BONUS * data.getBaseProbability());
        });
        CommonEvents.LISTENER_JOIN.register(OWNER, data -> reconcile(data.getVault()));
        CommonEvents.LISTENER_TICK.register(OWNER, data -> {
            Vault vault = data.getVault();
            TickClock clock = vault.get(Vault.CLOCK);
            if (clock != null && clock.get(TickClock.GLOBAL_TIME) % RECONCILE_INTERVAL_TICKS == 0) {
                reconcile(vault);
            }
        });
    }

    private static void reconcile(Vault vault) {
        if (vault == null || !TenosVaultUtil.anyRunnerHasMinor(vault, TenosNodes.OMEGA_VAULT)) {
            return;
        }
        if (VaultModifierUtils.getCountOfModifiers(vault, OMEGA_FORTUNE_DOUBLE) > 0) {
            return;
        }
        if (resolveOmegaModifier() == null) {
            return;
        }
        VaultModifierUtils.addModifier(vault, OMEGA_FORTUNE_DOUBLE, 1);
    }

    /**
     * Omega Vault asks for +100% omega room chance and no shipped modifier carries a 2.0 weight
     * multiplier, so one is built and registered on first use. Registering lazily rather than at
     * startup is deliberate: the modifier registry is cleared on every config reload, and this
     * check re-registers it the next time a vault needs it.
     */
    private static VaultModifier<?> resolveOmegaModifier() {
        VaultModifier<?> existing = VaultModifierRegistry.get(OMEGA_FORTUNE_DOUBLE);
        if (existing != null) {
            return existing;
        }
        try {
            PoolReferenceWeightModifier modifier = new PoolReferenceWeightModifier(
                    OMEGA_FORTUNE_DOUBLE,
                    new PoolReferenceWeightModifier.Properties(List.of(OMEGA_ROOMS_POOL), OMEGA_VAULT_WEIGHT_MULTIPLIER),
                    new VaultModifier.Display("Omega Sprout", TextColor.parseColor("#6AFF00"), "2x Omega Room Chance"));
            VaultModifierRegistry.register(OMEGA_FORTUNE_DOUBLE, modifier);
            return modifier;
        } catch (RuntimeException e) {
            WoldsVaults.LOGGER.error("Omega Vault could not register its {} weight modifier; the node did nothing.",
                    OMEGA_FORTUNE_DOUBLE, e);
            return null;
        }
    }
}
