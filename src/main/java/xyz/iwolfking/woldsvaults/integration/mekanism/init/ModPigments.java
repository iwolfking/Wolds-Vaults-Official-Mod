package xyz.iwolfking.woldsvaults.integration.mekanism.init;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.common.registration.impl.PigmentDeferredRegister;
import mekanism.common.registration.impl.PigmentRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModPigments {
    public static final PigmentDeferredRegister PIGMENTS = new PigmentDeferredRegister(WoldsVaults.MOD_ID);


    public static final PigmentRegistryObject<Pigment> IDONA_RED = PIGMENTS.register(
            "idona_red",
            0xD3112A
    );

    public static final PigmentRegistryObject<Pigment> TENOS_BLUE = PIGMENTS.register(
            "tenos_blue",
            0x1E88E5
    );

    public static final PigmentRegistryObject<Pigment> WENDARR_YELLOW = PIGMENTS.register(
            "wendarr_yellow",
            0xFFB300
    );

    public static final PigmentRegistryObject<Pigment> VELARA_GREEN = PIGMENTS.register(
            "velara_green",
            0x2E7D32
    );

    public static final PigmentRegistryObject<Pigment> FOIL_PIGMENT = PIGMENTS.register(
        "foil_pigment",
            PigmentBuilder.builder(ResourceLocation.fromNamespaceAndPath(WoldsVaults.MOD_ID, "chemical/foil_pigment"))
                          .color(0x9B59B6).getColor()
    );

    public static void register(IEventBus eventBus) {
        PIGMENTS.register(eventBus);
    }
}
