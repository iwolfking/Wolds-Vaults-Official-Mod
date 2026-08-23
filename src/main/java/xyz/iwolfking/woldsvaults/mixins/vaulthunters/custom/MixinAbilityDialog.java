package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.AbilityDialog;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.gods.ultimates.UltimateIds;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.AbstractDialogAccessor;

import java.util.Optional;

@Mixin(value = AbilityDialog.class, remap = false)
public class MixinAbilityDialog {

    @Shadow @Final private AbilityTree abilityTree;
    @Shadow private String selectedAbility;

    @Redirect(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Optional;isPresent()Z",
            ordinal = 0
        )
    )
    private boolean checkVaultPausedOne(Optional<Vault> optional) {
        return woldsVaults$forceActiveWhilePaused(optional);
    }

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Optional;isPresent()Z",
                    ordinal = 1
            )
    )
    private boolean checkVaultPausedTwo(Optional<Vault> optional) {
        return woldsVaults$forceActiveWhilePaused(optional);
    }

    @Unique
    private boolean woldsVaults$forceActiveWhilePaused(Optional<Vault> optional) {
        if (optional.isPresent()) {
            boolean isPaused = optional.get().getOptional(Vault.CLOCK)
                    .map(clock -> clock.has(TickClock.PAUSED))
                    .orElse(false);
            if (isPaused) return false;
        }
        return optional.isPresent();
    }

    /**
     * Locks the learn, unlearn and specialization buttons on the god ultimate node. Which ultimate
     * Stirrings of Power is pointed at follows the equipped god charm and the player's god level,
     * and the server re-resolves it on a timer, so a click here could only ever be undone a second
     * later; the buttons are disabled rather than left to flicker.
     */
    @Inject(method = "update", at = @At("TAIL"))
    private void woldsVaults$lockGodUltimate(CallbackInfo ci) {
        if (this.selectedAbility == null || this.abilityTree == null) {
            return;
        }
        Skill skill = this.abilityTree.getForId(this.selectedAbility).orElse(null);
        if (skill == null || skill.getParent() == null
                || !UltimateIds.STIRRINGS_OF_POWER.equals(skill.getParent().getId())) {
            return;
        }
        AbstractDialogAccessor buttons = (AbstractDialogAccessor) this;
        Button learn = buttons.woldsvaults$getLearnButton();
        if (learn != null) {
            learn.active = false;
        }
        buttons.woldsvaults$setRegretButton(null);
    }
}
