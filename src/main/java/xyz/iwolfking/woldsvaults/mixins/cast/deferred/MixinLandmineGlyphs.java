package xyz.iwolfking.woldsvaults.mixins.cast.deferred;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.block.entity.LandmineBlockEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.augments.effect.SpellHits;

/**
 * A landmine is triggered by whatever steps on it, outside its own tick. A mine a spell step placed triggers under that
 * step's cast tag, so the explosion it schedules (its hits, its radius, the chain it sets off) keeps the step's glyphs.
 */
@Mixin(value = LandmineBlockEntity.class, remap = false)
public class MixinLandmineGlyphs {

    @WrapMethod(method = "trigger")
    private void woldsvaults$triggerUnderCast(LivingEntity entity, Operation<Void> original) {
        CastContext.runTask(woldsvaults$spellTag(), SpellHits.Carrier.BLOCK_ENTITY, () -> original.call(entity));
    }

    @WrapMethod(method = "triggerChainReaction")
    private void woldsvaults$chainUnderCast(Operation<Void> original) {
        CastContext.runTask(woldsvaults$spellTag(), SpellHits.Carrier.BLOCK_ENTITY, () -> original.call());
    }

    @Unique
    private CastTag woldsvaults$spellTag() {
        CastTag tag = CastContext.tagOrRestore((LandmineBlockEntity) (Object) this);
        return tag != null && tag.glyphs() != null ? tag : null;
    }
}
