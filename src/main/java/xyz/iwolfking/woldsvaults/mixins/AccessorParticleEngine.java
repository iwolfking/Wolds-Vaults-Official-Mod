package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ParticleEngine.class)
public interface AccessorParticleEngine {

    @Accessor
    TextureAtlas getTextureAtlas();
}
