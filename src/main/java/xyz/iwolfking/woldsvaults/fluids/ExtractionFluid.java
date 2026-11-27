package xyz.iwolfking.woldsvaults.fluids;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;
import net.minecraft.world.level.material.Fluid;

/**
 * One of the extractor crafting fluids (Molten Painite, Molten Echo, Molten Larimar, Molten Alexandrite,
 * Coolant): a source and flowing fluid with dedicated textures at {@code block/fluid/<key>} and
 * {@code block/fluid/flowing_<key>}, a liquid block {@code <key>_block} and a bucket {@code <key>_bucket}.
 */
public class ExtractionFluid {
    public final String key;
    public final RegistryObject<FlowingFluid> source;
    public final RegistryObject<FlowingFluid> flowing;
    public final LiquidBlock block;
    public final BucketItem bucket;
    private ForgeFlowingFluid.Properties properties;

    public ExtractionFluid(DeferredRegister<Fluid> registry, String key, boolean molten, MaterialColor color) {
        this.key = key;
        this.source = registry.register(key, () -> new ForgeFlowingFluid.Source(this.properties));
        this.flowing = registry.register("flowing_" + key, () -> new ForgeFlowingFluid.Flowing(this.properties));
        FluidAttributes.Builder attributes = FluidAttributes.builder(WoldsVaults.id("block/fluid/" + key), WoldsVaults.id("block/fluid/flowing_" + key))
                .color(0xFFFFFFFF);
        if (molten) {
            attributes.density(3000).viscosity(6000).temperature(1300).luminosity(12);
        } else {
            attributes.density(1100).viscosity(1200).temperature(250);
        }
        this.block = new LiquidBlock(this.source, BlockBehaviour.Properties.of(molten ? Material.LAVA : Material.WATER, color)
                .noCollission().noOcclusion().randomTicks().strength(100.0F).noDrops().lightLevel(state -> molten ? 12 : 0));
        this.bucket = new BucketItem(this.source, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(ModCreativeTabs.WOLDS_VAULTS));
        this.bucket.setRegistryName(WoldsVaults.id(key + "_bucket"));
        this.properties = new ForgeFlowingFluid.Properties(this.source, this.flowing, attributes)
                .bucket(() -> this.bucket)
                .block(() -> this.block);
    }
}
