package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.greed.GreedTree;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import iskallia.vault.world.VaultDifficulty;
import iskallia.vault.world.data.PlayerGreedTreeData;
import iskallia.vault.world.data.WorldSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.theme.InfusedCrystalTheme;
import xyz.iwolfking.woldsvaults.api.util.InfusedThemeHelper;
import xyz.iwolfking.woldsvaults.config.ThemeModifiersConfig;

import java.util.UUID;

@Mixin(value = ValueCrystalTheme.class, remap = false)
public class MixinValueCrystalTheme {

    @Shadow 
    private ResourceLocation id;

    @Inject(method = "configure", at = @At("HEAD"), cancellable = true)
    private void overrideConfigure(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        if(InfusedThemeHelper.handleThemeInfusion(vault, this.id, random, sigil)) {
            ci.cancel();
        }
    }
}