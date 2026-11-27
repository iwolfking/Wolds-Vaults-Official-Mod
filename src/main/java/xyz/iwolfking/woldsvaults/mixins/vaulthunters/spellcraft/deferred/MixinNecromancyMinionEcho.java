package xyz.iwolfking.woldsvaults.mixins.vaulthunters.spellcraft.deferred;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.skill.ability.effect.NecromancyAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredGlyphs;
import xyz.iwolfking.woldsvaults.spellcraft.scope.DeferredReach;

/**
 * Echo on Necromancy: while the player's last Necromancy cast carries Echo, every minion hit (and its splash) is
 * followed by one more at Echo power that ignores hurt immunity. It is not a cast: no cost, no curse, no Power Dump.
 */
@Mixin(value = NecromancyAbility.class, remap = false)
public class MixinNecromancyMinionEcho {

    @WrapMethod(method = "damageTarget")
    private static void woldsvaults$echoMinionHit(ServerPlayer owner, LivingEntity target, float damage, float aoeRadius, float aoeMultiplier,
                                                  Operation<Void> original) {
        original.call(owner, target, damage, aoeRadius, aoeMultiplier);
        DeferredGlyphs glyphs = DeferredReach.minionGlyphs(owner);
        if (glyphs == null || glyphs.modifiers().echoPower() <= 0.0F || DeferredReach.echoingMinionHit()) {
            return;
        }
        float echoDamage = damage * glyphs.modifiers().echoPower();
        DeferredReach.echoMinionHit(() -> original.call(owner, target, echoDamage, aoeRadius, aoeMultiplier));
    }
}
