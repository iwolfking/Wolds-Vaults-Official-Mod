package xyz.iwolfking.woldsvaults.mixins.cast.deferred;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.skill.ability.effect.ShieldBashRetributionAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.SkillContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.augments.effect.SpellHits;

/**
 * Retribution retaliates on the skill tick its charge timer runs out. A cast as a spell step keeps its cast tag while
 * the timer runs, so the retaliation (its hits and its radius) runs under the step's glyphs.
 */
@Mixin(value = ShieldBashRetributionAbility.class, remap = false)
public class MixinRetributionGlyphWindow {
    @Shadow
    private int timeLeft;

    @Unique
    private CastTag woldsvaults$window;

    @WrapMethod(method = "doAction")
    private Ability.ActionResult woldsvaults$openWindow(SkillContext context, Operation<Ability.ActionResult> original) {
        Ability.ActionResult result = original.call(context);
        if (result.isSuccess()) {
            CastTag tag = CastContext.current();
            this.woldsvaults$window = tag != null && tag.glyphs() != null ? tag : null;
        }
        return result;
    }

    @WrapMethod(method = "onTick")
    private void woldsvaults$tickInWindow(SkillContext context, Operation<Void> original) {
        CastTag window = this.woldsvaults$window;
        if (window == null || this.timeLeft <= 0) {
            this.woldsvaults$window = null;
            original.call(context);
            return;
        }
        CastContext.runTask(window, SpellHits.Carrier.EVENT, () -> original.call(context));
    }
}
