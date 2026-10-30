package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.item.bottle.ManaFlatBottleEffect;
import iskallia.vault.item.bottle.PotionBottleEffect;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PotionBottleEffect.class, remap = false)
public interface PotionBottleEffectAccessor {
    @Accessor("potion")
    MobEffect getPotion();

    @Accessor("duration")
    int getDuration();

    @Accessor("amplifier")
    int getAmplifier();
}
