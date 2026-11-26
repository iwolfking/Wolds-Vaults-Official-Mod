package xyz.iwolfking.woldsvaults.integration.shadered;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class MirrorStructureFiles {
    private static final String FOLDER = "shadered_structures";

    private MirrorStructureFiles() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        Path folder = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(FOLDER);
        for (ResourceLocation id : server.getResourceManager().listResources(FOLDER, path -> path.endsWith(".nbt"))) {
            Path target = folder.resolve(id.getPath().substring(id.getPath().lastIndexOf('/') + 1));
            if (Files.isRegularFile(target)) {
                continue;
            }
            try (Resource resource = server.getResourceManager().getResource(id); InputStream stream = resource.getInputStream()) {
                Files.createDirectories(folder);
                Files.copy(stream, target);
            } catch (IOException e) {
                WoldLogger.error("Could not copy the mirror structure {} into the world", id, e);
            }
        }
    }
}
