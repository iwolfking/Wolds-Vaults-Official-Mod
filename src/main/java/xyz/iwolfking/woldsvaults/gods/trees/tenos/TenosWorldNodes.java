package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.processor.tile.VaultLootTileProcessor;
import iskallia.vault.block.PlaceholderBlock;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

/**
 * The Tenos nodes that shape the vault around the player: Omega Vault (r98), Nose for Treasure
 * (r103) and Master of Chests (r108).
 *
 * <p>Nose for Treasure is inherently vault wide - the placeholder event carries no player, which
 * matches the sheet, and one listener covering the whole vault is also what makes "does not stack"
 * true for free, so it keeps a listener of its own here. Omega Vault attaches a room weight
 * modifier once per vault and Master of Chests attaches a cascade group of its own built from the
 * pack's one-percent omega cascading rows, the same rows the mythic charm suffix uses; both are
 * driven by {@link TenosVaultHandlers}, which reaches them once per runner on join and again on
 * the shared ticker, so this class holds no listener for either.
 *
 * <p>Both modifier nodes work when they land mid-vault. Rooms are chosen lazily, one region at a
 * time, as chunks generate ({@code GridGenerator.generate} -> {@code ConcurrentGridCache.getOrCreate}
 * -> {@code LAYOUT_TEMPLATE_GENERATION}), and a modifier added after the vault started still gets
 * its {@code initServer} on the next modifier tick, because {@code IVaultModifierBehaviorApply}'s
 * default {@code onVaultAdd} calls it. Regions already generated keep the rooms they were given.
 *
 * <p>Both deduplicate against the vault's own modifier list rather than any in-memory set, and each
 * adds an id that belongs to it alone so its own contribution is distinguishable from the charm's
 * or the base mod's. That count is only trustworthy while the id resolves: {@code
 * Modifiers.getModifiers()} silently drops entries whose id is not in the modifier registry, and
 * the registry is cleared and rebuilt from {@code vault_modifiers.json} on every config reload.
 * Both ids are therefore declared in that file, next to the rest of the pack's modifiers, and are
 * only looked up here - a node that registered its own id would stop counting its own modifier
 * after a reload and attach a second one. Because they are declared, the guard holds for a second
 * player joining, for a relog and across a server restart.
 */
public final class TenosWorldNodes {
    public static final ResourceLocation MASTER_OF_CHESTS_CASCADE = WoldsVaults.id("tenos_master_of_chests");
    public static final ResourceLocation OMEGA_FORTUNE_DOUBLE = WoldsVaults.id("omega_fortune_double");

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
    public static void reconcileOmegaRooms(Vault vault) {
        attachOnce(vault, OMEGA_FORTUNE_DOUBLE, "Omega Vault");
    }

    /** As {@link #reconcileOmegaRooms}, for Master of Chests' cascade group. */
    public static void reconcileCascading(Vault vault) {
        attachOnce(vault, MASTER_OF_CHESTS_CASCADE, "Master of Chests");
    }

    private static void attachOnce(Vault vault, ResourceLocation modifier, String node) {
        if (vault == null) {
            return;
        }
        if (VaultModifierUtils.getCountOfModifiers(vault, modifier) > 0) {
            return;
        }
        if (TenosVaultUtil.resolveModifier(modifier, node) == null) {
            return;
        }
        VaultModifierUtils.addModifier(vault, modifier, 1);
    }
}
