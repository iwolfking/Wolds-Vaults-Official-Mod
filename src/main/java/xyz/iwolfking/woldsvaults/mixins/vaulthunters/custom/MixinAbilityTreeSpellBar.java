package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.spellcraft.bar.SpellBars;

@Mixin(value = AbilityTree.class, remap = false)
public abstract class MixinAbilityTreeSpellBar {

    /** VH's cast key releases the selected spell slot instead of the selected ability. */
    @Inject(method = "onKeyUp", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$slotKeyUp(SkillContext context, CallbackInfo ci) {
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (player != null && SpellBars.onTreeKey(player, false)) {
            ci.cancel();
        }
    }

    /** VH's cast key presses the selected spell slot instead of the selected ability. */
    @Inject(method = "onKeyDown", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$slotKeyDown(SkillContext context, CallbackInfo ci) {
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (player != null && SpellBars.onTreeKey(player, true)) {
            ci.cancel();
        }
    }

    /** Quick-select keys and the ability wheel select the slot casting that ability; abilities off the bar are refused. */
    @Inject(method = "onQuickSelect", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$slotQuickSelect(String id, SkillContext context, CallbackInfo ci) {
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (player != null && SpellBars.onQuickSelect(player, id)) {
            ci.cancel();
        }
    }

    /** Scrolling with the cast key held moves through filled spell slots. */
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void woldsvaults$slotScroll(int delta, SkillContext context, CallbackInfo ci) {
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (player != null && !SpellBars.isDispatching()) {
            SpellBars.onScroll(player, delta);
            ci.cancel();
        }
    }
}
