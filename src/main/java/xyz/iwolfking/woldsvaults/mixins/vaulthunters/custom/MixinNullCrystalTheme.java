package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import iskallia.vault.item.crystal.theme.NullCrystalTheme;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.core.theme.InfusedCrystalTheme;
import xyz.iwolfking.woldsvaults.config.ThemeModifiersConfig;

@Mixin(value = NullCrystalTheme.class, remap = false)
public class MixinNullCrystalTheme {
    @Inject(method = "configure", at = @At("HEAD"), cancellable = true)
    private void overrideConfigure(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ModConfigs.VAULT_CRYSTAL.getRandomTheme(VaultMod.id("default"),  vault.get(Vault.LEVEL).get(), random).ifPresent(id -> {
            CrystalTheme child;

            if (ThemeModifiersConfig.shouldRandomlyInfuseVaultTheme(id, vault.get(Vault.LEVEL).get())) {
                child = new InfusedCrystalTheme(id);
            } else {
                child = new ValueCrystalTheme(id);
            }

            child.configure(vault, random, sigil);
        });

        ci.cancel();
    }
}
