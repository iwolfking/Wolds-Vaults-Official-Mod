package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import iskallia.vault.world.VaultDifficulty;
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
import xyz.iwolfking.woldsvaults.config.ThemeModifiersConfig;

import java.util.UUID;

@Mixin(value = ValueCrystalTheme.class, remap = false)
public class MixinValueCrystalTheme {

    @Shadow 
    private ResourceLocation id;

    @Inject(method = "configure", at = @At("HEAD"), cancellable = true)
    private void overrideConfigure(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        if (this.id == null) {
            return;
        }

        UUID vaultOwnerId = vault.get(Vault.OWNER);
        VaultDifficulty difficulty = VaultDifficulty.NORMAL;

        if (vaultOwnerId != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                WorldSettings settings = WorldSettings.get(server.overworld());
                difficulty = settings.getPlayerDifficulty(vaultOwnerId);
            }
        } else {
            WoldsVaults.LOGGER.error("No owner found on vault during configuration!");
        }

        if (ThemeModifiersConfig.shouldRandomlyInfuseVaultTheme(this.id, vault.get(Vault.LEVEL).get(), difficulty)) {
            InfusedCrystalTheme infusedTheme = new InfusedCrystalTheme(this.id);
            infusedTheme.configure(vault, random, sigil);
            ci.cancel();
        }
    }
}