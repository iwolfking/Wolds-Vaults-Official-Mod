package xyz.iwolfking.woldsvaults.effect.mobeffects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * The hover after a storm-shrouded riptide dash. Movement is handled in {@code MixinLivingEntity}'s travel hook:
 * no gravity, vertical speed damped to zero and horizontal speed bled off by the hover friction.
 */
public class TidalHoverEffect extends MobEffect {
    public TidalHoverEffect(MobEffectCategory category, int color, ResourceLocation id) {
        super(category, color);
        this.setRegistryName(id);
    }
}
