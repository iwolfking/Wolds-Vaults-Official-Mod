package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.entity.VaultThrownJavelin;
import iskallia.vault.mana.ManaPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.abilities.flexible.FlexibleAbility;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GlyphGrants;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GearGlyphs;

import java.util.concurrent.atomic.AtomicBoolean;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

@Mixin(value = VaultThrownJavelin.class, remap = false)
public abstract class MixinJavelinAbility {
    @Shadow
    public abstract Player getThrower();

    @Unique
    private static final AtomicBoolean woldsvaults$WARNED_NO_CAST = new AtomicBoolean();

    /**
     * Imploding Javelin: a javelin hit casts a free Implode on the target when the throw carried the Implosion glyph.
     * It deals the mana the throw paid (a sneaking Power Dump throw pays the whole bar), or the current bar for a
     * standing throw, scaled by {@code 1 + potency} (the imploding javelin damage the glyph came from).
     */
    @Inject(method = "doPostHurtEffects", at = @At(value = "INVOKE", target = "Liskallia/vault/util/EntityHelper;knockbackIgnoreResist(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;F)V", remap = false), remap = true)
    private void implodeJavelin(LivingEntity pLiving, CallbackInfo ci) {
        Player thrower = this.getThrower();
        GlyphGrants.Grant implosion = thrower == null ? null : GearGlyphs.carried((Entity) (Object) this, thrower).get(AugmentType.IMPLOSION);
        if(implosion != null) {
            float mana = ((ManaPlayer) thrower).getMana();
            CastTag tag = CastContext.current();
            if (tag == null || !(thrower instanceof ServerPlayer serverThrower) || !tag.isOwnedBy(serverThrower)) {
                if (woldsvaults$WARNED_NO_CAST.compareAndSet(false, true)) {
                    WoldLogger.error("Imploding Javelin hit found no cast tag for its thrower; the free Implode uses the current mana bar.");
                }
            } else if (tag.manaConsumed() > 0.0F) {
                mana = tag.manaConsumed();
            }
            float damageScale = 1.0F + implosion.potency();
            new FlexibleAbility().castImplode(thrower, pLiving, mana, damageScale);
        }
     }

}
