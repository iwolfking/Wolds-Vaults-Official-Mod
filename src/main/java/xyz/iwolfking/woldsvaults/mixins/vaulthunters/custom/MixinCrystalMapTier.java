package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.util.VaultMapTierCache;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckMapTier;

import java.util.Optional;

/**
 * Carries the map tier (0-5) on the crystal: imprinted at anvil craft (VaultMapItem), kept
 * through the NBT round-trips of CrystalData.copy()/read()/write(), and stashed into
 * {@link VaultMapTierCache} when the crystal configures its vault so strongbox loot generation
 * can read it. Crystals without a tier never write the tag and never touch the cache.
 */
@Mixin(value = CrystalData.class, remap = false)
public class MixinCrystalMapTier implements DuckMapTier {
    @Unique
    private static final String MAP_TIER_TAG = "woldsvaults:map_tier";

    @Unique
    private int woldsvaults$mapTier = -1;

    @Override
    public int getMapTier() {
        return this.woldsvaults$mapTier;
    }

    @Override
    public void setMapTier(int tier) {
        this.woldsvaults$mapTier = tier;
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void writeMapTier(CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        if (this.woldsvaults$mapTier >= 0) {
            cir.getReturnValue().ifPresent(nbt -> nbt.putInt(MAP_TIER_TAG, this.woldsvaults$mapTier));
        }
    }

    // HEAD, not TAIL: readNbt reassigns its nbt local to an upgraded copy, so read the original.
    @Inject(method = "readNbt", at = @At("HEAD"))
    private void readMapTier(CompoundTag nbt, CallbackInfo ci) {
        this.woldsvaults$mapTier = nbt.contains(MAP_TIER_TAG) ? nbt.getInt(MAP_TIER_TAG) : -1;
    }

    @Inject(method = "configure", at = @At("TAIL"))
    private void stashMapTierForVault(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        if (this.woldsvaults$mapTier >= 0 && vault.has(Vault.ID)) {
            VaultMapTierCache.put(vault.get(Vault.ID), this.woldsvaults$mapTier);
        }
    }
}
