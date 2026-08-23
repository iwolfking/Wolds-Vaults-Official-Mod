package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.core.vault.objective.rune.RuneBossFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.client.hyper.ClientMagicMissileWarning;

/**
 * Shows the hyperboss Magic Missile charge on the rune-boss bar with the exact countdown
 * treatment Wave Blast gets: while the packet-fed client warning is fresh, the Wave Blast
 * warning fields are swapped for the missile values around render (and restored on return)
 * and the hardcoded "Wave Blast " label is rewritten, so VH's orange countdown fill and
 * gold/yellow timer text are reused unchanged. If both abilities telegraph at once the
 * sooner one owns the slot. The warning is only ever populated in hyper vaults, so normal
 * rune fights render untouched.
 */
@Mixin(value = RuneBossFight.class, remap = false)
public class MixinRuneBossFightClient {
    @Shadow
    private boolean waveBlastWarningActive;
    @Shadow
    private int waveBlastWarningTicks;
    @Shadow
    private int waveBlastWarningWindow;
    @Shadow
    private int waveBlastCooldownDuration;

    @Unique
    private boolean woldsVaults$missileShown;
    @Unique
    private boolean woldsVaults$savedActive;
    @Unique
    private int woldsVaults$savedTicks;
    @Unique
    private int woldsVaults$savedWindow;
    @Unique
    private int woldsVaults$savedCooldown;

    @Inject(method = "render", at = @At("HEAD"))
    private void woldsVaults$showMagicMissileCountdown(PoseStack matrixStack, Window window, float partialTicks, CallbackInfo ci) {
        if (!ClientMagicMissileWarning.isActive()) {
            return;
        }
        int missileTicks = ClientMagicMissileWarning.getRemainingTicks();
        if (this.waveBlastWarningActive && this.waveBlastWarningWindow > 0 && this.waveBlastWarningTicks <= missileTicks) {
            return;
        }
        this.woldsVaults$missileShown = true;
        this.woldsVaults$savedActive = this.waveBlastWarningActive;
        this.woldsVaults$savedTicks = this.waveBlastWarningTicks;
        this.woldsVaults$savedWindow = this.waveBlastWarningWindow;
        this.woldsVaults$savedCooldown = this.waveBlastCooldownDuration;
        this.waveBlastWarningActive = true;
        this.waveBlastWarningTicks = missileTicks;
        this.waveBlastWarningWindow = ClientMagicMissileWarning.getWindowTicks();
        this.waveBlastCooldownDuration = 0;
    }

//    @ModifyConstant(method = "render", constant = @Constant(stringValue = "Wave Blast "), remap = true)
//    private String woldsVaults$relabelMissileCountdown(String label) {
//        return this.woldsVaults$missileShown ? "Magic Missile " : label;
//    }

    @Inject(method = "render", at = @At("RETURN"))
    private void woldsVaults$restoreWaveBlastCountdown(PoseStack matrixStack, Window window, float partialTicks, CallbackInfo ci) {
        if (!this.woldsVaults$missileShown) {
            return;
        }
        this.woldsVaults$missileShown = false;
        this.waveBlastWarningActive = this.woldsVaults$savedActive;
        this.waveBlastWarningTicks = this.woldsVaults$savedTicks;
        this.waveBlastWarningWindow = this.woldsVaults$savedWindow;
        this.waveBlastCooldownDuration = this.woldsVaults$savedCooldown;
    }
}
