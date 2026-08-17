package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import com.google.gson.JsonObject;
import iskallia.vault.VaultMod;
import iskallia.vault.core.data.adapter.basic.TypeSupplierAdapter;
import iskallia.vault.core.data.serializable.ISerializable;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.CrystalEntry;
import iskallia.vault.item.crystal.layout.CrystalLayout;
import iskallia.vault.item.crystal.model.CrystalModel;
import iskallia.vault.item.crystal.modifiers.CrystalModifiers;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicRingsCrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicTunnelCrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicWaveCrystalLayout;
import xyz.iwolfking.woldsvaults.api.core.layout.impl.ClassicWaveLayout;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionCrystal;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionObjectiveTargets;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionTier;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionVaultState;
import xyz.iwolfking.woldsvaults.models.crystal.UnhingedCrystalModel;

import java.util.Optional;

@Mixin(value = CrystalData.class, remap = false)
public abstract class MixinCrystalData extends CrystalEntry implements ISerializable<CompoundTag, JsonObject>, GreedMedallionCrystal
{

    @Unique
    private static final String WOLDSVAULTS$MEDALLION_KEY = "GreedMedallion";

    /**
     * Rank index of the greed medallion applied to this crystal, 0 for none. Stored as a bare int
     * rather than a serialised tier so a rebalance of {@link GreedMedallionTier} never invalidates
     * existing crystals. Rides {@code writeNbt}/{@code readNbt}, which also gives {@code copy()} and
     * every workbench round-trip the right behaviour for free.
     */
    @Unique
    private int woldsvaults$medallionRank = 0;

    @Shadow public static TypeSupplierAdapter<CrystalModel> MODEL;

    @Shadow private CrystalModifiers modifiers;

    @Shadow private CrystalTheme theme;

    @Shadow
    public static TypeSupplierAdapter<CrystalLayout> LAYOUT;

    static {
        MODEL.register("unhinged", UnhingedCrystalModel.class, UnhingedCrystalModel::new);
        LAYOUT.register("tunnels", ClassicTunnelCrystalLayout.class, ClassicTunnelCrystalLayout::new);
        LAYOUT.register("rings", ClassicRingsCrystalLayout.class, ClassicRingsCrystalLayout::new);
        LAYOUT.register("wave", ClassicWaveCrystalLayout.class, ClassicWaveCrystalLayout::new);
    }

    /**
     * @author iwolfking
     * @reason Always allow catalyst fragments
     */
    @Overwrite
    public boolean canGenerateCatalystFragments() {
        boolean hasCatalystDenyingModifier = false;
        for(VaultModifierStack modStack : this.modifiers.getList()) {

            if(modStack.getModifierId().equals(VaultMod.id("gilded_cascade"))) {
                if(modStack.getSize() > 2) {
                    hasCatalystDenyingModifier = true;
                }
            }

            if(modStack.getModifierId().equals(VaultMod.id("prismatic"))) {
                hasCatalystDenyingModifier = false;
            }
            else if(modStack.getModifierId().equals(VaultMod.id("sparkling"))) {
                hasCatalystDenyingModifier = false;
            }
        }

        return !hasCatalystDenyingModifier;
    }

    @Override
    public int woldsvaults$getMedallionRank() {
        return this.woldsvaults$medallionRank;
    }

    @Override
    public void woldsvaults$setMedallionRank(int rank) {
        this.woldsvaults$medallionRank = Math.max(0, rank);
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void woldsvaults$writeMedallion(CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        if (this.woldsvaults$medallionRank > 0) {
            cir.getReturnValue().ifPresent(nbt -> nbt.putInt(WOLDSVAULTS$MEDALLION_KEY, this.woldsvaults$medallionRank));
        }
    }

    @Inject(method = "readNbt(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void woldsvaults$readMedallion(CompoundTag nbt, CallbackInfo ci) {
        this.woldsvaults$medallionRank = nbt.contains(WOLDSVAULTS$MEDALLION_KEY) ? nbt.getInt(WOLDSVAULTS$MEDALLION_KEY) : 0;
    }

    /**
     * Publishes the crystal's medallion to the vault being built from it. Runs at HEAD so the state
     * is already live when {@code super.configure} walks the crystal's children, which is what lets
     * an objective read the medallion while it is configuring itself. {@code Vault.ID} is assigned
     * in {@code VaultFactory.create} before this call, so the id is always present here.
     */
    @Inject(method = "configure", at = @At("HEAD"))
    private void woldsvaults$publishMedallion(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        if (this.woldsvaults$medallionRank <= 0 || vault == null || !vault.has(Vault.ID)) {
            return;
        }
        Optional<GreedMedallionTier> tier = GreedMedallionTier.byRankIndex(this.woldsvaults$medallionRank);
        if (tier.isEmpty()) {
            xyz.iwolfking.woldsvaults.WoldsVaults.LOGGER.error(
                    "Crystal carries greed medallion rank {} which matches no tier; the vault will run without medallion effects.",
                    this.woldsvaults$medallionRank);
            return;
        }
        GreedMedallionVaultState.set(vault.get(Vault.ID), tier.get());
        GreedMedallionObjectiveTargets.register(vault, tier.get());
    }
}
