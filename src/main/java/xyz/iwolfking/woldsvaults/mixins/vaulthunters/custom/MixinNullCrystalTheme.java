package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import iskallia.vault.item.crystal.theme.NullCrystalTheme;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import iskallia.vault.world.VaultDifficulty;
import iskallia.vault.world.data.ServerVaults;
import iskallia.vault.world.data.WorldSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.theme.InfusedCrystalTheme;
import xyz.iwolfking.woldsvaults.config.ThemeModifiersConfig;
import java.util.UUID;

@Mixin(value = NullCrystalTheme.class, remap = false)
public class MixinNullCrystalTheme {
    @Inject(method = "configure", at = @At("HEAD"), cancellable = true)
    private void overrideConfigure(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ModConfigs.VAULT_CRYSTAL.getRandomTheme(VaultMod.id("default"),  vault.get(Vault.LEVEL).get(), random).ifPresent(id -> {
            CrystalTheme child;

            UUID vaultOwnerId = vault.get(Vault.OWNER);
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server == null) {
                WoldsVaults.LOGGER.info("No server!");
                return;
            }

            VaultDifficulty difficulty = WorldSettings.get(server.overworld()).getPlayerDifficulty(vaultOwnerId);

            if (ThemeModifiersConfig.shouldRandomlyInfuseVaultTheme(id, vault.get(Vault.LEVEL).get(), difficulty)) {
                child = new InfusedCrystalTheme(id);
            } else {
                child = new ValueCrystalTheme(id);
            }

            child.configure(vault, random, sigil);
        });

        ci.cancel();
    }
}
