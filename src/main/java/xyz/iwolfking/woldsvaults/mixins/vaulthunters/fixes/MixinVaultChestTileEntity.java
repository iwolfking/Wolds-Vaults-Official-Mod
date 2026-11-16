package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.core.Version;
import iskallia.vault.core.random.RandomSource;
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
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckStrongboxTier;
import xyz.iwolfking.woldsvaults.loot.StrongboxTierScaling;
import xyz.iwolfking.woldsvaults.loot.VaultChestSlots;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;

import java.util.ArrayList;
import java.util.List;

/**
 * Chest sizes, loot placement, and the strongbox tier wiring. A strongbox takes the strongbox tier of its
 * vault's greed medallion - -1 when the vault has none - for its loot pools and rarity.
 */
@Mixin(value = VaultChestTileEntity.class, remap = false)
public class MixinVaultChestTileEntity {
    /** Strongbox tier of the vault this chest generates in; -1 for everything but tiered strongboxes. */
    @Unique
    private int woldsvaults$strongboxTier = -1;

    /**
     * @author iwolfking
     * @reason Resize strongboxes to 81 slots; they are locked, so the ChestMenu ceiling never applies.
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
    private void resolveStrongboxTier(Version version, Player player, List<ItemStack> loot, RandomSource random, CallbackInfo ci) {
        VaultChestTileEntity self = (VaultChestTileEntity) (Object) this;
        this.woldsvaults$strongboxTier = woldsvaults$isStrongbox(self.getBlockState().getBlock())
                ? GreedMedallionEffects.strongboxTier(self.getLevel())
                : -1;
    }

    @ModifyExpressionValue(
            method = "generateLootTable",
            at = @At(value = "NEW", target = "iskallia/vault/core/world/loot/generator/TieredLootTableGenerator")
    )
    private TieredLootTableGenerator passStrongboxTierToGenerator(TieredLootTableGenerator generator) {
        if (this.woldsvaults$strongboxTier >= 0) {
            ((DuckStrongboxTier) (Object) generator).setStrongboxTier(this.woldsvaults$strongboxTier);
        }
        return generator;
    }

    /** Strongboxes never compute the CDF percentile, so their rarity tracks the strongbox tier instead. */
    @ModifyExpressionValue(
            method = "generateLootTable",
            at = @At(value = "INVOKE", target = "Liskallia/vault/config/VaultChestConfig;getRarity(D)Liskallia/vault/util/VaultRarity;")
    )
    private VaultRarity rarityFromStrongboxTier(VaultRarity rarity) {
        return this.woldsvaults$strongboxTier >= 0 ? StrongboxTierScaling.rarityForTier(this.woldsvaults$strongboxTier) : rarity;
    }

    /** The sweep that refills generatedStacksCount; only tiered strongboxes count their whole box. */
    @ModifyConstant(method = "generateChestLoot", constant = @Constant(intValue = 27))
    private int woldsvaults$strongboxStackSweep(int cap) {
        return this.woldsvaults$strongboxTier >= 0 ? ((VaultChestTileEntity) (Object) this).getContainerSize() : cap;
    }

    /** The threshold above which fillLoot merges and re-splits stacks; raised for tiered strongboxes. */
    @ModifyConstant(method = "fillLoot", constant = @Constant(intValue = 27))
    private int woldsvaults$strongboxMergeThreshold(int cap) {
        return this.woldsvaults$strongboxTier >= 0 ? ((VaultChestTileEntity) (Object) this).getContainerSize() : cap;
    }

    /** The slot scan: any chest with hidden slots, and any tiered strongbox, offers its whole container. */
    @ModifyConstant(method = "getAvailableSlots", constant = @Constant(intValue = 27))
    private int woldsvaults$slotScanLimit(int cap) {
        if (this.woldsvaults$strongboxTier >= 0) {
            return ((VaultChestTileEntity) (Object) this).getContainerSize();
        }
        return this.woldsvaults$hasHiddenSlots() ? ((VaultChestTileEntity) (Object) this).getContainerSize() : cap;
    }

    /** fillLoot pops slots off the tail of this list, so the visible slots are moved last. */
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

    /** shuffleAndSplitItems is given the visible slot count rather than the whole container. */
    @ModifyArg(
            method = "fillLoot",
            at = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/VaultChestTileEntity;shuffleAndSplitItems(Ljava/util/List;ILiskallia/vault/core/random/RandomSource;)V"),
            index = 1
    )
    private int woldsvaults$splitOnlyVisibleSlots(int freeSlots) {
        return this.woldsvaults$hasHiddenSlots() ? Math.min(freeSlots, VaultChestSlots.VISIBLE) : freeSlots;
    }
}
