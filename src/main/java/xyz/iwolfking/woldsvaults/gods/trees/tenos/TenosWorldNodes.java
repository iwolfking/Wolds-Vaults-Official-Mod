package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.processor.tile.VaultLootTileProcessor;
import iskallia.vault.block.PlaceholderBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;

/**
 * Omega Vault, Nose for Treasure and Master of Chests. Omega Vault attaches its modifier once; Master
 * of Chests keeps one stack per runner with the node. Both count against the vault's own modifier list,
 * under ids declared in {@code vault_modifiers.json}.
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

    /** Attaches Omega Vault's room weight modifier to a vault that does not already carry it. */
    public static void reconcileOmegaRooms(Vault vault) {
        attachOnce(vault, OMEGA_FORTUNE_DOUBLE, "Omega Vault");
    }

    /** Tops Master of Chests up to one stack per runner holding the node. Stacks are never removed. */
    public static void reconcileCascading(Vault vault) {
        if (vault == null) {
            return;
        }
        int holders = 0;
        for (ServerPlayer player : TenosVaultUtil.runners(vault)) {
            if (TenosNodes.isActive(player, TenosNodes.MASTER_OF_CHESTS)) {
                holders++;
            }
        }
        int missing = holders - (int) VaultModifierUtils.getCountOfModifiers(vault, MASTER_OF_CHESTS_CASCADE);
        if (missing <= 0 || TenosVaultUtil.resolveModifier(MASTER_OF_CHESTS_CASCADE, "Master of Chests") == null) {
            return;
        }
        VaultModifierUtils.addModifier(vault, MASTER_OF_CHESTS_CASCADE, missing);
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
