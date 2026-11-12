package xyz.iwolfking.woldsvaults.mixins.vaulthunters.medallions;

import iskallia.vault.core.vault.NaturalSpawner;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.medallions.GreedMedallionEffects;

@Mixin(value = NaturalSpawner.class, remap = false)
public class MixinVaultNaturalSpawner {

    /** Multiplies the spawner's {@code maxMobs} ceiling by the crystal's greed medallion factor. */
    @ModifyVariable(method = "lambda$tickServer$0", at = @At("STORE"), index = 4)
    private int scaleMaxMobsForMedallion(int maxMobs, VirtualWorld world, Vault vault, ServerPlayer player) {
        double multiplier = GreedMedallionEffects.mobSpawnMultiplier(vault);
        return multiplier == 1.0D ? maxMobs : (int) Math.round(maxMobs * multiplier);
    }
}
