package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.screen.player.AbilitiesElementContainerScreen;
import iskallia.vault.client.gui.screen.player.legacy.LegacySkillTreeElementContainerScreen;
import iskallia.vault.client.gui.screen.player.legacy.SplitTabContent;
import iskallia.vault.client.gui.screen.player.legacy.TabContent;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.AbilityDialog;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.client.spellcraft.bar.SkillMenuSpellBar;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.AbilityDialogAccessor;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.SplitTabContentAccessor;

import javax.annotation.Nullable;

@Mixin(value = LegacySkillTreeElementContainerScreen.class, remap = false)
public abstract class MixinLegacySkillTreeScreenSpellBar {
    @Shadow @Final protected TabContent content;

    @Unique
    @Nullable
    private SkillMenuSpellBar woldsvaults$spellBar() {
        Object self = this;
        if (self.getClass() != AbilitiesElementContainerScreen.class) {
            return null;
        }
        return SkillMenuSpellBar.of((AbilitiesElementContainerScreen) self, this::woldsvaults$selectedSpec);
    }

    @Unique
    @Nullable
    private String woldsvaults$selectedSpec() {
        if (this.content instanceof SplitTabContent split && ((SplitTabContentAccessor) split).woldsvaults$getDialog() instanceof AbilityDialog dialog) {
            return ((AbilityDialogAccessor) dialog).woldsvaults$getSelectedAbility();
        }
        return null;
    }

    /** Builds the spell bar's views before Vault Hunters draws the tree, so the tree's slot tags match the bar. */
    @Inject(method = "render", at = @At("HEAD"), remap = true)
    private void woldsvaults$beginSpellBar(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        SkillMenuSpellBar menu = woldsvaults$spellBar();
        if (menu != null) {
            menu.beginFrame();
        }
    }

    /** Draws the spell bar under the Abilities tab after the tree and dialog and before the point overlay. */
    @Inject(method = "render", at = @At(value = "INVOKE", remap = false,
            target = "Liskallia/vault/client/gui/screen/player/legacy/LegacySkillTreeElementContainerScreen;renderPointOverlay(Lcom/mojang/blaze3d/vertex/PoseStack;)V"),
            remap = true)
    private void woldsvaults$renderSpellBar(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        SkillMenuSpellBar menu = woldsvaults$spellBar();
        if (menu == null) {
            return;
        }
        menu.endTree();
        menu.strip().render(poseStack, mouseX, mouseY);
        RenderSystem.enableDepthTest();
    }

    /** Draws the spell bar's chooser or slot tooltip last, above everything else. */
    @Inject(method = "render", at = @At("TAIL"), remap = true)
    private void woldsvaults$renderSpellBarOverlay(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        SkillMenuSpellBar menu = woldsvaults$spellBar();
        if (menu != null) {
            menu.strip().renderOverlay(poseStack, (Screen) (Object) this, mouseX, mouseY);
        }
    }

    /** The spell bar gets first refusal on clicks so its open chooser is not clicked through to the tree and dialog. */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = true)
    private void woldsvaults$clickSpellBar(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        SkillMenuSpellBar menu = woldsvaults$spellBar();
        if (menu != null && menu.strip().mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    /** While the spell bar waits for a bind, the pressed key goes to it instead of the screen. */
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, remap = true)
    private void woldsvaults$bindSpellSlot(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        SkillMenuSpellBar menu = woldsvaults$spellBar();
        if (menu != null && menu.strip().keyPressed(keyCode, scanCode)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true, remap = true)
    private void woldsvaults$scrollSpellBar(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
        SkillMenuSpellBar menu = woldsvaults$spellBar();
        if (menu != null && menu.strip().mouseScrolled(mouseX, mouseY, delta)) {
            cir.setReturnValue(true);
        }
    }
}
