package xyz.iwolfking.woldsvaults.client.lightning;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

/**
 * Armor textures that animate (the Lightning Fiend set's main layer: crackling lightning and shining spots). Armor is
 * drawn with its texture bound straight by path, outside the atlas that plays .mcmeta animations, so this texture
 * stands in at the armor's own path: it reads a strip of square frames, top to bottom, from a second file and shows
 * the next frame every {@link #FRAME_TICKS} ticks. Everything that binds the path animates: the armor on the body,
 * first-person sleeves, gear previews. If the strip can't be read it shows the plain texture at its own path.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AnimatedArmorTexture extends AbstractTexture implements Tickable {
    private static final int FRAME_TICKS = 2;
    private static final Map<ResourceLocation, ResourceLocation> ANIMATED = Map.of(
            new ResourceLocation("the_vault", "textures/item/gear/armor/lightning_fiend/armor_layer1_overlay.png"),
            new ResourceLocation(WoldsVaults.MOD_ID, "textures/armor/lightning_fiend/armor_layer1_overlay_frames.png"));

    private final ResourceLocation location;
    private final ResourceLocation frames;
    @Nullable
    private NativeImage strip;
    private int size;
    private int count;
    private int shown = -1;
    private int ticks;

    private AnimatedArmorTexture(ResourceLocation location, ResourceLocation frames) {
        this.location = location;
        this.frames = frames;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        NativeImage image;
        boolean animated = true;
        try (Resource resource = resourceManager.getResource(this.frames)) {
            image = NativeImage.read(resource.getInputStream());
        } catch (IOException missing) {
            WoldsVaults.LOGGER.error("Lightning Fiend armor frame strip {} could not be read; drawing {} unanimated.", this.frames, this.location, missing);
            animated = false;
            try (Resource resource = resourceManager.getResource(this.location)) {
                image = NativeImage.read(resource.getInputStream());
            }
        }
        NativeImage loaded = image;
        int frameSize = loaded.getWidth();
        int frameCount = animated ? Math.max(1, loaded.getHeight() / frameSize) : 1;
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this.setUp(loaded, frameSize, frameCount));
        } else {
            this.setUp(loaded, frameSize, frameCount);
        }
    }

    private void setUp(NativeImage image, int frameSize, int frameCount) {
        if (this.strip != null) {
            this.strip.close();
        }
        this.strip = image;
        this.size = frameSize;
        this.count = frameCount;
        TextureUtil.prepareImage(this.getId(), 0, frameSize, frameSize);
        this.show(0);
    }

    private void show(int frame) {
        if (this.strip == null) {
            return;
        }
        this.bind();
        this.strip.upload(0, 0, 0, 0, frame * this.size, this.size, this.size, false, false, false, false);
        this.shown = frame;
    }

    @Override
    public void tick() {
        if (this.count < 2) {
            return;
        }
        this.ticks++;
        int frame = this.ticks / FRAME_TICKS % this.count;
        if (frame != this.shown) {
            this.show(frame);
        }
    }

    @Override
    public void close() {
        if (this.strip != null) {
            this.strip.close();
            this.strip = null;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || mc.level == null || mc.getOverlay() != null) {
            return;
        }
        TextureManager textures = mc.getTextureManager();
        ANIMATED.forEach((location, frames) -> {
            if (!(textures.getTexture(location, null) instanceof AnimatedArmorTexture)) {
                textures.register(location, new AnimatedArmorTexture(location, frames));
            }
        });
    }
}
