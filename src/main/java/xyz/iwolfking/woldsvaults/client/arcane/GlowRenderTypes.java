package xyz.iwolfking.woldsvaults.client.arcane;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** Render types for glowing effects (a subclass only to reach the protected render state shards). */
public final class GlowRenderTypes extends RenderType {
    // Additive coloured glow (lasers, the Prism pyramid, its light): tested against the world's depth but never written
    // to it, and not culled, so overlapping glows simply add up instead of hiding whatever is drawn behind them.
    public static final RenderType GLOW = create("woldsvaults_glow", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    private GlowRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean crumbling, boolean sort, Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, crumbling, sort, setup, clear);
    }
}
