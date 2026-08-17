package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.SigilUtils;
import xyz.iwolfking.woldsvaults.api.util.VaultMapTierCache;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckMapGod;
import xyz.iwolfking.woldsvaults.api.util.ducks.DuckMapTier;
import xyz.iwolfking.woldsvaults.maps.MapGodVaultState;

import java.util.Optional;

/**
 * Carries the map tier (0-5) on the crystal: imprinted at anvil craft (VaultMapItem), kept
 * through the NBT round-trips of CrystalData.copy()/read()/write(), and stashed into
 * {@link VaultMapTierCache} when the crystal configures its vault so strongbox loot generation
 * can read it. Crystals without a tier never write the tag and never touch the cache.
 */
@Mixin(value = CrystalData.class, remap = false)
public class MixinCrystalMapTier implements DuckMapTier, DuckMapGod {
    @Unique
    private static final String MAP_TIER_TAG = "woldsvaults:map_tier";

    @Unique
    private static final String MAP_GOD_TAG = "woldsvaults:map_god";

    @Unique
    private static final String MAP_BONUS_XP_TAG = "woldsvaults:map_bonus_xp";

    @Unique
    private int woldsvaults$mapTier = -1;

    @Unique
    private String woldsvaults$mapGod = "";

    @Unique
    private int woldsvaults$mapBonusXp = 0;

    @Override
    public int getMapTier() {
        return this.woldsvaults$mapTier;
    }

    @Override
    public void setMapTier(int tier) {
        this.woldsvaults$mapTier = tier;
    }

    @Override
    public String getMapGod() {
        return this.woldsvaults$mapGod;
    }

    @Override
    public void setMapGod(String god) {
        this.woldsvaults$mapGod = god == null ? "" : god;
    }

    @Override
    public int getMapBonusXp() {
        return this.woldsvaults$mapBonusXp;
    }

    @Override
    public void setMapBonusXp(int bonusPercent) {
        this.woldsvaults$mapBonusXp = Math.max(0, bonusPercent);
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void writeMapTier(CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        if (this.woldsvaults$mapTier >= 0) {
            cir.getReturnValue().ifPresent(nbt -> nbt.putInt(MAP_TIER_TAG, this.woldsvaults$mapTier));
        }
        if (!this.woldsvaults$mapGod.isEmpty()) {
            cir.getReturnValue().ifPresent(nbt -> {
                nbt.putString(MAP_GOD_TAG, this.woldsvaults$mapGod);
                nbt.putInt(MAP_BONUS_XP_TAG, this.woldsvaults$mapBonusXp);
            });
        }
    }

    /** HEAD, not TAIL: readNbt reassigns its nbt local to an upgraded copy, so read the original. */
    @Inject(method = "readNbt", at = @At("HEAD"))
    private void readMapTier(CompoundTag nbt, CallbackInfo ci) {
        this.woldsvaults$mapTier = nbt.contains(MAP_TIER_TAG) ? nbt.getInt(MAP_TIER_TAG) : -1;
        this.woldsvaults$mapGod = nbt.contains(MAP_GOD_TAG) ? nbt.getString(MAP_GOD_TAG) : "";
        this.woldsvaults$mapBonusXp = nbt.contains(MAP_BONUS_XP_TAG) ? nbt.getInt(MAP_BONUS_XP_TAG) : 0;
    }

    /**
     * TAIL, so the vault's difficulty multiplier is captured after every other configure step has
     * run - in particular after the medallion published at the HEAD of the same method, which is a
     * multiplicative part of that difficulty.
     */
    @Inject(method = "configure", at = @At("TAIL"))
    private void stashMapTierForVault(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        if (!vault.has(Vault.ID)) {
            return;
        }
        if (this.woldsvaults$mapTier >= 0) {
            VaultMapTierCache.put(vault.get(Vault.ID), this.woldsvaults$mapTier);
        }
        if (this.woldsvaults$mapGod.isEmpty()) {
            return;
        }
        VaultGod god = VaultGod.fromName(this.woldsvaults$mapGod);
        if (god == null) {
            WoldsVaults.LOGGER.error("Crystal carries map god '{}' which matches no vault god; the vault will award no god experience.",
                    this.woldsvaults$mapGod);
            return;
        }
        MapGodVaultState.set(vault.get(Vault.ID), god, this.woldsvaults$mapBonusXp,
                SigilUtils.getDifficultyMultiplier(sigil, vault));
    }
}
