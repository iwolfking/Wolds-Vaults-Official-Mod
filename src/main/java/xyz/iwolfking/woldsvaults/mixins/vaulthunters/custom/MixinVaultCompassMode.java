package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.item.VaultCompassMode;
import iskallia.vault.world.VaultDifficulty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = VaultCompassMode.class, remap = false)
public class MixinVaultCompassMode {

    @Final
    @Shadow
    public static VaultCompassMode EXIT;

    @Shadow
    @Final
    private int requirement;

    /**
     * @author iwolfking
     * @reason Compass Mode does not scale with difficulty
     */
    @Overwrite
    public int getRequirement(VaultDifficulty difficulty) {
        return this.requirement;
    }
}
