package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.core.Version;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.world.loot.generator.TieredLootTableGenerator;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.util.VaultRarity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.VaultMapTierCache;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckMapTier;
import xyz.iwolfking.woldsvaults.loot.StrongboxTierScaling;
import xyz.iwolfking.woldsvaults.loot.VaultChestSlots;

import java.util.ArrayList;
import java.util.List;

/**
 * Chest sizes, loot placement, and the mapped-strongbox tier wiring.
 *
 * <p>The strongbox half resolves the running vault's map tier when a strongbox generates loot, hands
 * it to the loot generator (tier-scaled pools/rolls), derives the displayed rarity from the tier,
 * and lets loot fill the whole 81-slot box instead of the first 27 slots. Strongboxes outside a
 * tiered vault (regular vaults, or no cache entry after a server restart) resolve tier -1 and behave
 * exactly as before.
 *
 * <p>The placement half spends the hidden slots {@code MixinVaultChestTileEntitySlots} gives every
 * in-vault chest. The base game scans only the first 27 slots for somewhere to put loot and drops
 * whatever is left once that list runs out, so a chest that rolls more stacks than the screen can
 * show throws the surplus away. The scan now covers the whole container, ordered so the visible
 * slots fill first and the hidden ones take only genuine overflow, while the stack splitter is still
 * told about the visible slots alone so what the screen shows is unchanged.
 */
@Mixin(value = VaultChestTileEntity.class, remap = false)
public class MixinVaultChestTileEntity {
    /** Map tier of the vault this chest generates in; -1 for everything but mapped strongboxes. */
    @Unique
    private int woldsvaults$mapTier = -1;

    /**
     * @author iwolfking
     * @reason Resize strongboxes to 81 slots. Strongboxes are locked (no GUI), so the vanilla
     * ChestMenu 54-slot ceiling never applies; loot only drops on break.
     */
    @Overwrite
    private int getSize(BlockState state) {
        Block block = state.getBlock();
        if (block == ModBlocks.TREASURE_CHEST || block == ModBlocks.TREASURE_CHEST_PLACEABLE) {
            return 54;
        }
        if (block == ModBlocks.WOODEN_CHEST || block == ModBlocks.WOODEN_CHEST_PLACEABLE || block == ModBlocks.WOODEN_BARREL) {
            return 36;
        }
        if (woldsvaults$isStrongbox(block)) {
            return 81;
        }
        return 45;
    }

    @Unique
    private static boolean woldsvaults$isStrongbox(Block block) {
        return block == ModBlocks.GILDED_STRONGBOX || block == ModBlocks.ORNATE_STRONGBOX || block == ModBlocks.LIVING_STRONGBOX;
    }

    /**
     * Whether this chest holds slots the player cannot see. Strongboxes have no screen at all and
     * treasure chests open a six-row one that already covers their container, so both keep placing
     * loot exactly as they did.
     */
    @Unique
    private boolean woldsvaults$hasHiddenSlots() {
        VaultChestTileEntity self = (VaultChestTileEntity) (Object) this;
        Block block = self.getBlockState().getBlock();
        return self.isVaultChest()
                && block != ModBlocks.TREASURE_CHEST
                && !woldsvaults$isStrongbox(block)
                && self.getContainerSize() > VaultChestSlots.VISIBLE;
    }

    @Inject(method = "generateLootTable", at = @At("HEAD"))
    private void resolveStrongboxMapTier(Version version, Player player, List<ItemStack> loot, RandomSource random, CallbackInfo ci) {
        VaultChestTileEntity self = (VaultChestTileEntity) (Object) this;
        this.woldsvaults$mapTier = woldsvaults$isStrongbox(self.getBlockState().getBlock())
                ? VaultUtils.getVault(self.getLevel())
                        .filter(vault -> vault.has(Vault.ID))
                        .map(vault -> VaultMapTierCache.get(vault.get(Vault.ID)))
                        .orElse(-1)
                : -1;
    }

    @ModifyExpressionValue(
            method = "generateLootTable",
            at = @At(value = "NEW", target = "iskallia/vault/core/world/loot/generator/TieredLootTableGenerator")
    )
    private TieredLootTableGenerator passMapTierToGenerator(TieredLootTableGenerator generator) {
        if (this.woldsvaults$mapTier >= 0) {
            ((DuckMapTier) (Object) generator).setMapTier(this.woldsvaults$mapTier);
        }
        return generator;
    }

    /** Strongboxes never compute the CDF percentile, so their rarity tracks the map tier instead. */
    @ModifyExpressionValue(
            method = "generateLootTable",
            at = @At(value = "INVOKE", target = "Liskallia/vault/config/VaultChestConfig;getRarity(D)Liskallia/vault/util/VaultRarity;")
    )
    private VaultRarity rarityFromMapTier(VaultRarity rarity) {
        return this.woldsvaults$mapTier >= 0 ? StrongboxTierScaling.rarityForTier(this.woldsvaults$mapTier) : rarity;
    }

    /**
     * The sweep that refills generatedStacksCount after placement. Tiered strongboxes count their
     * whole box; every other chest keeps counting the first 27 slots, as it did before hidden slots
     * existed.
     */
    @ModifyConstant(method = "generateChestLoot", constant = @Constant(intValue = 27))
    private int woldsvaults$strongboxStackSweep(int cap) {
        return this.woldsvaults$mapTier >= 0 ? ((VaultChestTileEntity) (Object) this).getContainerSize() : cap;
    }

    /**
     * The threshold above which fillLoot merges identical stacks and re-splits them at max stack
     * size. Left at 27 for every chest, so overflowing loot is still compacted into full stacks
     * before placement - that compaction is what lets a high-roll chest fit at all - while tiered
     * strongboxes keep the higher threshold they shipped with.
     */
    @ModifyConstant(method = "fillLoot", constant = @Constant(intValue = 27))
    private int woldsvaults$strongboxMergeThreshold(int cap) {
        return this.woldsvaults$mapTier >= 0 ? ((VaultChestTileEntity) (Object) this).getContainerSize() : cap;
    }

    /**
     * The slot scan. Tiered strongboxes keep their existing whole-box scan; every other in-vault
     * chest now offers its hidden slots too, so loot past the visible 27 has somewhere to go instead
     * of being dropped when the slot list runs out.
     */
    @ModifyConstant(method = "getAvailableSlots", constant = @Constant(intValue = 27))
    private int woldsvaults$slotScanLimit(int cap) {
        if (this.woldsvaults$mapTier >= 0) {
            return ((VaultChestTileEntity) (Object) this).getContainerSize();
        }
        return this.woldsvaults$hasHiddenSlots() ? ((VaultChestTileEntity) (Object) this).getContainerSize() : cap;
    }

    /**
     * fillLoot pops slots off the tail of this list, so the visible slots have to come last for loot
     * to land on screen first and reach the hidden slots only once the screen is full. The shuffle
     * the base game applies is preserved within each group.
     */
    @ModifyReturnValue(method = "getAvailableSlots", at = @At("RETURN"))
    private List<Integer> woldsvaults$visibleSlotsLast(List<Integer> slots) {
        if (!this.woldsvaults$hasHiddenSlots()) {
            return slots;
        }
        List<Integer> ordered = new ArrayList<>(slots.size());
        List<Integer> visible = new ArrayList<>(slots.size());
        for (Integer slot : slots) {
            if (slot < VaultChestSlots.VISIBLE) {
                visible.add(slot);
            } else {
                ordered.add(slot);
            }
        }
        ordered.addAll(visible);
        return ordered;
    }

    /**
     * shuffleAndSplitItems tears stacks apart until it has one per free slot, so handing it the
     * hidden slots as well would shred loot that fits on screen and scatter it out of sight. It is
     * given the visible slot count instead, which leaves fragmentation identical to before and lets
     * real overflow reach the hidden slots whole.
     */
    @ModifyArg(
            method = "fillLoot",
            at = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/VaultChestTileEntity;shuffleAndSplitItems(Ljava/util/List;ILiskallia/vault/core/random/RandomSource;)V"),
            index = 1
    )
    private int woldsvaults$splitOnlyVisibleSlots(int freeSlots) {
        return this.woldsvaults$hasHiddenSlots() ? Math.min(freeSlots, VaultChestSlots.VISIBLE) : freeSlots;
    }
}
