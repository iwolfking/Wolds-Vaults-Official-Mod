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

    /**
     * Greed medallions raise the vault's mob spawn rate. The spawner keeps mob pressure at a
     * constant population - it tops the world up to {@code baseMaxMobs + extraMaxMobs} every tick -
     * so lifting that ceiling is what "+X% Mobs" means in practice, and it multiplies the base and
     * modifier-granted mobs together rather than joining the additive pool that {@code mob_increase}
     * writes to.
     *
     * <p>Injected on local 4 of {@code lambda$tickServer$0}, the {@code maxMobs} sum; index verified
     * against the shipped 3.21.6 disassembly ({@code iadd; istore 4}).</p>
     */
    @ModifyVariable(method = "lambda$tickServer$0", at = @At("STORE"), index = 4)
    private int scaleMaxMobsForMedallion(int maxMobs, VirtualWorld world, Vault vault, ServerPlayer player) {
        double multiplier = GreedMedallionEffects.mobSpawnMultiplier(vault);
        return multiplier == 1.0D ? maxMobs : (int) Math.round(maxMobs * multiplier);
    }
}
