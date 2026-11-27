package xyz.iwolfking.woldsvaults.mixins.cast;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.skill.ability.effect.NecromancyAbility;
import iskallia.vault.skill.ability.effect.spi.core.Ability;
import iskallia.vault.skill.ability.effect.spi.core.InstantAbility;
import iskallia.vault.skill.base.SkillContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.spellcraft.ability.AbilityLookup;
import xyz.iwolfking.woldsvaults.spellcraft.buff.BuffGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredReach;

/**
 * Opens a cast context for every instant ability a player casts, covering the mana check, the hits and the payment, so
 * per-cast effects can tell the hits of one cast apart from the next. A cast run as a spell step carries that step's
 * frozen glyphs on its tag; a cast that went through starts its buff activation (Intensify, Extend) and, for
 * Necromancy, sets the glyphs its minions follow.
 */
@Mixin(value = InstantAbility.class, remap = false)
public class MixinInstantAbilityCast {

    @WrapMethod(method = "onAction")
    private Ability.ActionResult woldsvaults$openCastContext(SkillContext context, Operation<Ability.ActionResult> original) {
        ServerPlayer player = context.getSource().as(ServerPlayer.class).orElse(null);
        if (player == null) {
            return original.call(context);
        }
        InstantAbility self = (InstantAbility) (Object) this;
        String spec = AbilityLookup.specId(self);
        CastTag tag = new CastTag(player, self);
        tag.setGlyphs(DeferredGlyphs.fromScope(player, spec));
        Ability.ActionResult result = CastContext.run(tag, () -> original.call(context));
        if (result.isSuccess()) {
            BuffGlyphs.onInstantCast(player, self, tag.glyphs());
            if (self instanceof NecromancyAbility) {
                DeferredReach.noteMinionCast(player, tag.glyphs());
            }
        }
        return result;
    }
}
