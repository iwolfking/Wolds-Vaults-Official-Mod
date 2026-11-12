package xyz.iwolfking.woldsvaults.mixins.cast.deferred;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.skill.ability.effect.DashDamageAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.augments.effect.SpellHits;

/**
 * Dash Damage hits during the skill ticks after the dash. A dash cast as a spell step keeps its cast tag for that
 * window, so the window's hits run under the step's glyphs.
 */
@Mixin(value = DashDamageAbility.class, remap = false)
public class MixinDashDamageGlyphWindow {
    @Unique
    private static final int WOLDSVAULTS$WINDOW_TICKS = 21;

    @Unique
    private CastTag woldsvaults$window;

    @Unique
    private int woldsvaults$windowEnd;

    @WrapMethod(method = "doAction")
    private Ability.ActionResult woldsvaults$openWindow(SkillContext context, Operation<Ability.ActionResult> original) {
        Ability.ActionResult result = original.call(context);
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (player != null && result.isSuccess()) {
            CastTag tag = CastContext.current();
            this.woldsvaults$window = tag != null && tag.glyphs() != null ? tag : null;
            this.woldsvaults$windowEnd = player.server.getTickCount() + WOLDSVAULTS$WINDOW_TICKS;
        }
        return result;
    }

    @WrapMethod(method = "onTick")
    private void woldsvaults$tickInWindow(SkillContext context, Operation<Void> original) {
        CastTag window = this.woldsvaults$window;
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (window == null || player == null || player.server.getTickCount() > this.woldsvaults$windowEnd) {
            this.woldsvaults$window = null;
            original.call(context);
            return;
        }
        CastContext.runTask(window, SpellHits.Carrier.EVENT, () -> original.call(context));
    }
}
