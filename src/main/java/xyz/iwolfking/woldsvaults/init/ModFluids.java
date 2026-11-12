package xyz.iwolfking.woldsvaults.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.fluids.PrismaticGlueFluid;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.level.material.MaterialColor;
import xyz.iwolfking.woldsvaults.fluids.ExtractionFluid;

public class ModFluids {
    public static final DeferredRegister<Fluid> REGISTRY = DeferredRegister.create(ForgeRegistries.FLUIDS, WoldsVaults.MOD_ID);
    public static final RegistryObject<PrismaticGlueFluid.Source> PRISMATIC_GLUE = REGISTRY.register("prismatic_glue", PrismaticGlueFluid.Source::new);
    public static final RegistryObject<PrismaticGlueFluid.Flowing> FLOWING_PRISMATIC_GLUE = REGISTRY.register("flowing_prismatic_glue", PrismaticGlueFluid.Flowing::new);
    public static final RegistryObject<FlowingFluid> MOLTEN_TRINKET =
            REGISTRY.register("molten_trinket", () ->
                    new ForgeFlowingFluid.Source(ModFluids.MOLTEN_TRINKET_PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_MOLTEN_TRINKET =
            REGISTRY.register("flowing_molten_trinket", () ->
                    new ForgeFlowingFluid.Flowing(ModFluids.MOLTEN_TRINKET_PROPERTIES));

    public static final Map<String, ExtractionFluid> EXTRACTION_FLUIDS = new LinkedHashMap<>();
    public static final ExtractionFluid MOLTEN_PAINITE = extraction("molten_painite", true, MaterialColor.COLOR_RED);
    public static final ExtractionFluid MOLTEN_ECHO = extraction("molten_echo", true, MaterialColor.COLOR_PURPLE);
    public static final ExtractionFluid MOLTEN_LARIMAR = extraction("molten_larimar", true, MaterialColor.COLOR_CYAN);
    public static final ExtractionFluid MOLTEN_ALEXANDRITE = extraction("molten_alexandrite", true, MaterialColor.COLOR_MAGENTA);
    public static final ExtractionFluid COOLANT = extraction("coolant", false, MaterialColor.COLOR_LIGHT_BLUE);

    private static ExtractionFluid extraction(String key, boolean molten, MaterialColor color) {
        ExtractionFluid fluid = new ExtractionFluid(REGISTRY, key, molten, color);
        EXTRACTION_FLUIDS.put(key, fluid);
        return fluid;
    }

    public static final ForgeFlowingFluid.Properties MOLTEN_TRINKET_PROPERTIES =
            new ForgeFlowingFluid.Properties(
                    MOLTEN_TRINKET,
                    FLOWING_MOLTEN_TRINKET,
                    FluidAttributes.builder(
                                    ResourceLocation.withDefaultNamespace("block/lava_still"),
                                    ResourceLocation.withDefaultNamespace("block/lava_flow")
                            )
                            .density(3000)
                            .viscosity(6000)
                            .temperature(1300)
                            .luminosity(15)
                            .color(0xFFFF33CC)
            )
                    .bucket(() -> ModItems.MOLTEN_TRINKET_BUCKET)
                    .block(() -> ModBlocks.MOLTEN_TRINKET_BLOCK);

}
