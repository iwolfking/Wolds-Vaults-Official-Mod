package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import iskallia.vault.block.entity.VaultChestTileEntity;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.world.loot.generator.TieredLootTableGenerator;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.util.VaultRarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.VaultMapTierCache;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckMapTier;
import xyz.iwolfking.woldsvaults.loot.StrongboxTierScaling;

/**
 * Chest sizes plus the mapped-strongbox tier wiring: resolves the running vault's map tier when
 * a strongbox generates loot, hands it to the loot generator (tier-scaled pools/rolls), derives
 * the displayed rarity from the tier, and lets loot fill the whole 81-slot box instead of the
 * first 27 slots. Strongboxes outside a tiered vault (regular vaults, or no cache entry after a
 * server restart) resolve tier -1 and behave exactly as before.
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

    @Inject(method = "generateLootTable", at = @At("HEAD"))
    private void resolveStrongboxMapTier(CallbackInfo ci) {
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

    /** Tiered strongboxes fill their full 81 slots; every other non-treasure chest keeps the 27 cap. */
    @ModifyConstant(method = {"generateChestLoot", "fillLoot", "getAvailableSlots"}, constant = @Constant(intValue = 27))
    private int raiseStrongboxFillCap(int fillCap) {
        return this.woldsvaults$mapTier >= 0 ? ((VaultChestTileEntity) (Object) this).getContainerSize() : fillCap;
    }
}
