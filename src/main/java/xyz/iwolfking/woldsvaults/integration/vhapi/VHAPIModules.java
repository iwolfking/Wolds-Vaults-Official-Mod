package xyz.iwolfking.woldsvaults.integration.vhapi;

import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.util.VHAPIProcesserUtils;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.io.IOException;
import java.io.InputStream;

public class VHAPIModules {

    public static void init() {
    }


    public static void registerManualConfigFile(String filePath, ResourceLocation vhapiRegistryId) {
        try (InputStream stream = WoldsVaults.class.getResourceAsStream(filePath)) {
            if (stream == null) {
                throw new IOException();
            }
            VHAPIProcesserUtils.addManualConfigFile(stream, vhapiRegistryId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
