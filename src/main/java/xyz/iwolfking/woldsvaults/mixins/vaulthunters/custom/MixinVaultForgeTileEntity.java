package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.VaultForgeTileEntity;
import iskallia.vault.config.gear.VaultGearCraftingConfig;
import iskallia.vault.util.SidedHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.milestones.Milestones;

@Mixin(value = VaultForgeTileEntity.class, remap = false)
public class MixinVaultForgeTileEntity {
    /**
     * Feeds the "Master Smith" milestone. Grandmaster is the top step of the gear crafting config
     * and sits at relative proficiency 1.0, which is absolute proficiency at the cap for the
     * player's vault level. TAIL is only reached when scrap was actually sacrificed - both early
     * returns bail before it - so the state is checked exactly on the sacrifices that moved
     * proficiency, including the one that lands on the cap.
     */
    @Inject(method = "increaseProficiency", at = @At("TAIL"))
    private void countGrandmasterForge(ServerPlayer player, CallbackInfo ci) {
        int cap = VaultGearCraftingConfig.getProficiencyCap(SidedHelper.getVaultLevel(player));
        if (SidedHelper.getPlayerProficiency(player) >= cap) {
            Milestones.onVaultForgeMaxed(player);
        }
    }
}
