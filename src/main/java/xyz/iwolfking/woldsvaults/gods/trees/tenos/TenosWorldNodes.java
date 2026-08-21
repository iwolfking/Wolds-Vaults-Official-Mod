package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.GroupedModifier;
import iskallia.vault.core.vault.modifier.modifier.PoolReferenceWeightModifier;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.processor.tile.VaultLootTileProcessor;
import iskallia.vault.block.PlaceholderBlock;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Tenos nodes that shape the vault around the player: Omega Vault (r98), Nose for Treasure
 * (r103) and Master of Chests (r108).
 *
 * <p>Nose for Treasure is inherently vault wide - the placeholder event carries no player, which
 * matches the sheet, and one listener covering the whole vault is also what makes "does not stack"
 * true for free, so it keeps a listener of its own here. Omega Vault attaches a room weight
 * modifier once per vault and Master of Chests attaches the pack's one-percent omega cascading
 * rows, the same ones the mythic charm suffix uses; both are driven by
 * {@link TenosVaultHandlers}, which reaches them once per runner on join and again on the shared
 * ticker, so this class holds no listener for either.
 *
 * <p>Both modifier nodes work when they land mid-vault. Rooms are chosen lazily, one region at a
 * time, as chunks generate ({@code GridGenerator.generate} -> {@code ConcurrentGridCache.getOrCreate}
 * -> {@code LAYOUT_TEMPLATE_GENERATION}), and a modifier added after the vault started still gets
 * its {@code initServer} on the next modifier tick, because {@code IVaultModifierBehaviorApply}'s
 * default {@code onVaultAdd} calls it. Regions already generated keep the rooms they were given.
 *
 * <p>Both also deduplicate against the vault's own modifier list rather than any in-memory set,
 * which is what makes them safe across a server restart: the reconcile runs again every second, so
 * anything it remembered only in a static field would be re-applied the first time the vault ticks
 * after a reload. Each node therefore adds a modifier id that belongs to it alone and refuses to
 * act while the vault already carries one.
 */
public final class TenosWorldNodes {
    public static final ResourceLocation MASTER_OF_CHESTS_CASCADE = WoldsVaults.id("tenos_master_of_chests");

    /** The pack's five one-percent cascade rows, the children of {@code woldsvaults:omega_cascading}. */
    private static final List<String> CASCADE_CHILDREN = List.of(
            "woldsvaults:omega_cascading_wooden",
            "woldsvaults:omega_cascading_gilded",
            "woldsvaults:omega_cascading_ornate",
            "woldsvaults:omega_cascading_living",
            "woldsvaults:omega_cascading_coins");
    public static final ResourceLocation OMEGA_FORTUNE_DOUBLE = WoldsVaults.id("omega_fortune_double");
    public static final ResourceLocation OMEGA_ROOMS_POOL = new ResourceLocation("the_vault", "vault/rooms/omega_rooms");

    private static final Object OWNER = new Object();

    private TenosWorldNodes() {
    }

    static void register() {
        CommonEvents.PLACEHOLDER_GENERATION.register(OWNER, data -> {
            VaultLootTileProcessor parent = data.getParent();
            if (parent == null || parent.target != PlaceholderBlock.Type.TREASURE_DOOR) {
                return;
            }
            if (!TenosVaultUtil.anyRunnerHas(data.getVault(), TenosNodes.NOSE_FOR_TREASURE)) {
                return;
            }
            double bonus = TenosNodeHandlers.params(TenosNodes.NOSE_FOR_TREASURE,
                    TenosNodeHandlers.NoseForTreasureParams.class).treasure_door_bonus();
            data.setProbability(data.getProbability() + bonus * data.getBaseProbability());
        });
    }

    /**
     * Attaches Omega Vault's room weight modifier to a vault that does not already carry it. The
     * caller has already established that a runner holds the node, so this only guards against
     * granting the modifier twice.
     */
    public static void reconcileOmegaRooms(Vault vault, double weightMultiplier) {
        if (vault == null) {
            return;
        }
        if (VaultModifierUtils.getCountOfModifiers(vault, OMEGA_FORTUNE_DOUBLE) > 0) {
            return;
        }
        if (resolveOmegaModifier(weightMultiplier) == null) {
            return;
        }
        VaultModifierUtils.addModifier(vault, OMEGA_FORTUNE_DOUBLE, 1);
    }

    /** As {@link #reconcileOmegaRooms}, for Master of Chests' cascade group. */
    public static void reconcileCascading(Vault vault, int cascadingStacks) {
        if (vault == null) {
            return;
        }
        if (VaultModifierUtils.getCountOfModifiers(vault, MASTER_OF_CHESTS_CASCADE) > 0) {
            return;
        }
        if (resolveCascadeModifier(cascadingStacks) == null) {
            return;
        }
        VaultModifierUtils.addModifier(vault, MASTER_OF_CHESTS_CASCADE, 1);
    }

    /**
     * Master of Chests carries the same cascade rows the mythic charm suffix does, but under an id
     * of its own so the node can tell its own contribution apart from the charm's - counting
     * {@code woldsvaults:omega_cascading} on the vault says nothing about who put it there. One
     * stack of this group is the node's whole effect. Registered lazily for the same reason as
     * {@link #resolveOmegaModifier(double)}.
     */
    private static VaultModifier<?> resolveCascadeModifier(int cascadingStacks) {
        VaultModifier<?> existing = VaultModifierRegistry.get(MASTER_OF_CHESTS_CASCADE);
        if (existing != null) {
            return existing;
        }
        Map<String, Integer> children = new LinkedHashMap<>();
        for (String child : CASCADE_CHILDREN) {
            if (VaultModifierRegistry.get(ResourceLocation.parse(child)) == null) {
                WoldsVaults.LOGGER.error("Master of Chests could not find the cascade row {}; the node does nothing. "
                        + "Check vault_modifiers.json.", child);
                return null;
            }
            children.put(child, cascadingStacks);
        }
        try {
            GroupedModifier modifier = new GroupedModifier(
                    MASTER_OF_CHESTS_CASCADE,
                    new GroupedModifier.Properties(children),
                    new VaultModifier.Display("Master of Chests", TextColor.parseColor("#3FFBF4"),
                            "+" + cascadingStacks + "% Cascading for all chests and coins."));
            VaultModifierRegistry.register(MASTER_OF_CHESTS_CASCADE, modifier);
            return modifier;
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("Master of Chests could not register its cascade modifier {}; the node does nothing.",
                    MASTER_OF_CHESTS_CASCADE, e);
            return null;
        }
    }

    /**
     * Omega Vault asks for +100% omega room chance and no shipped modifier carries a 2.0 weight
     * multiplier, so one is built and registered on first use. Registering lazily rather than at
     * startup is deliberate: the modifier registry is cleared on every config reload, and this
     * check re-registers it the next time a vault needs it.
     */
    private static VaultModifier<?> resolveOmegaModifier(double weightMultiplier) {
        VaultModifier<?> existing = VaultModifierRegistry.get(OMEGA_FORTUNE_DOUBLE);
        if (existing != null) {
            return existing;
        }
        try {
            PoolReferenceWeightModifier modifier = new PoolReferenceWeightModifier(
                    OMEGA_FORTUNE_DOUBLE,
                    new PoolReferenceWeightModifier.Properties(List.of(OMEGA_ROOMS_POOL), weightMultiplier),
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
