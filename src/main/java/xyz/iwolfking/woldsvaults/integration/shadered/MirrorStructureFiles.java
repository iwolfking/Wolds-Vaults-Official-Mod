package xyz.iwolfking.woldsvaults.integration.shadered;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/*
 * Shadered's structure mirrors only read their tiles from <world>/data/shadered_structures/<sha256>.nbt, which a
 * player normally fills by uploading a file. Rooms that ship mirrors (the Inverted Pyramid's desert) keep their tiles
 * at data/<namespace>/shadered_structures/ instead, and every server start copies the missing ones into the world.
 */
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
                WoldsVaults.LOGGER.error("Could not copy the mirror structure {} into the world", id, e);
            }
        }
    }
}
