package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;


import iskallia.vault.skill.PlayerVaultStats;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PlayerVaultStats.class, remap = false)
public abstract class MixinPlayerVaultStatsData {

    @Shadow private int unspentExpertisePoints;

    @Shadow public abstract int getVaultLevel();

    @Shadow private int totalSpentExpertisePoints;

    /**
     * @author iwolfking
     * @reason Fix points given from expertise orbs not being returned.
     */
    @Overwrite
    public PlayerVaultStats resetAndReturnExpertisePoints(MinecraftServer server) {
        this.unspentExpertisePoints = totalSpentExpertisePoints + unspentExpertisePoints;
        this.totalSpentExpertisePoints = 0;
        return (PlayerVaultStats) (Object)this;
    }
}
