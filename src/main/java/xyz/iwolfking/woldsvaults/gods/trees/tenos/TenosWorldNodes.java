package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.GroupedModifier;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import xyz.iwolfking.woldsvaults.gods.GodNodeValues;

/**
 * The Tenos nodes that shape the vault around the player: Omega Vault (r98), Nose for Treasure
 * (r103) and Master of Chests (r108).
 *
 * <p>Nose for Treasure is inherently vault wide - the placeholder event carries no player, which
 * matches the sheet, and one listener covering the whole vault is also what makes "does not stack"
 * true for free. Omega Vault attaches a room weight modifier once per vault, deduplicated by id
 * for the same reason. Master of Chests attaches fifteen stacks of the pack's one-percent omega
 * cascading modifier, the same one the mythic charm suffix uses.
 *
 * <p>Both modifier nodes work when they land mid-vault. Rooms are chosen lazily, one region at a
 * time, as chunks generate ({@code GridGenerator.generate} -> {@code ConcurrentGridCache.getOrCreate}
 * -> {@code LAYOUT_TEMPLATE_GENERATION}), and a modifier added after the vault started still gets
 * its {@code initServer} on the next modifier tick, because {@code IVaultModifierBehaviorApply}'s
 * default {@code onVaultAdd} calls it. Regions already generated keep the rooms they were given.
 *
 * <p>Both also deduplicate against the vault's own modifier list rather than any in-memory set,
 * which is what makes them safe across a server restart: this reconcile runs on a 20-tick timer,
 * so anything it remembers only in a static field is re-applied the first time the vault ticks
 * after a reload. Each node therefore adds a modifier id that belongs to it alone and refuses to
 * act while the vault already carries one.
 */
public final class TenosWorldNodes {
    public static double omegaVaultWeightMultiplier() {
        return GodNodeValues.precise(TenosNodes.OMEGA_VAULT, "weight_multiplier");
    }
    public static double treasureDoorBonus() {
        return GodNodeValues.precise(TenosNodes.NOSE_FOR_TREASURE, "treasure_door_bonus");
    }
    public static int masterOfChestsCascadingStacks() {
        return GodNodeValues.count(TenosNodes.MASTER_OF_CHESTS, "cascading_stacks");
    }
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
            data.setProbability(data.getProbability() + treasureDoorBonus() * data.getBaseProbability());
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
        if (vault == null) {
            return;
        }
        reconcileOmegaRooms(vault);
        reconcileCascading(vault);
    }

    private static void reconcileOmegaRooms(Vault vault) {
        if (!TenosVaultUtil.anyRunnerHasMinor(vault, TenosNodes.OMEGA_VAULT)) {
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

    private static void reconcileCascading(Vault vault) {
        if (!TenosVaultUtil.anyRunnerHasMinor(vault, TenosNodes.MASTER_OF_CHESTS)) {
            return;
        }
        if (VaultModifierUtils.getCountOfModifiers(vault, MASTER_OF_CHESTS_CASCADE) > 0) {
            return;
        }
        if (resolveCascadeModifier() == null) {
            return;
        }
        VaultModifierUtils.addModifier(vault, MASTER_OF_CHESTS_CASCADE, 1);
    }

    /**
     * Master of Chests carries the same cascade rows the mythic charm suffix does, but under an id
     * of its own so the node can tell its own contribution apart from the charm's - counting
     * {@code woldsvaults:omega_cascading} on the vault says nothing about who put it there. One
     * stack of this group is the node's whole effect. Registered lazily for the same reason as
     * {@link #resolveOmegaModifier()}.
     */
    private static VaultModifier<?> resolveCascadeModifier() {
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
            children.put(child, masterOfChestsCascadingStacks());
        }
        try {
            GroupedModifier modifier = new GroupedModifier(
                    MASTER_OF_CHESTS_CASCADE,
                    new GroupedModifier.Properties(children),
                    new VaultModifier.Display("Master of Chests", TextColor.parseColor("#3FFBF4"),
                            "+" + masterOfChestsCascadingStacks() + "% Cascading for all chests and coins."));
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
    private static VaultModifier<?> resolveOmegaModifier() {
        VaultModifier<?> existing = VaultModifierRegistry.get(OMEGA_FORTUNE_DOUBLE);
        if (existing != null) {
            return existing;
        }
        try {
            PoolReferenceWeightModifier modifier = new PoolReferenceWeightModifier(
                    OMEGA_FORTUNE_DOUBLE,
                    new PoolReferenceWeightModifier.Properties(List.of(OMEGA_ROOMS_POOL), omegaVaultWeightMultiplier()),
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
