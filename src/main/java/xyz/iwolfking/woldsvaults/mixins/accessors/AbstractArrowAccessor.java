package xyz.iwolfking.woldsvaults.mixins.accessors;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Nullable
    @Accessor("piercingIgnoreEntityIds")
    IntOpenHashSet getPiercingIgnoreEntityIds();

    @Accessor("piercingIgnoreEntityIds")
    void setPiercingIgnoreEntityIds(IntOpenHashSet ids);
}
