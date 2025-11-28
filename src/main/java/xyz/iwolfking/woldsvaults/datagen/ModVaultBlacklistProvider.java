package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultBlacklistProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModVaultBlacklistProvider extends AbstractVaultBlacklistProvider {
    protected ModVaultBlacklistProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("wolds_blacklist", builder -> {
           builder.blacklist("ars_nouveau:*");
           builder.blacklist("occultism:*");
           builder.blacklist("peripherals:*");
           builder.blacklist("computercraft:*");
           builder.blacklist("advancedperipherals:*");
           builder.blacklist("immersiveengineering:*");
           builder.blacklist("travel_anchors:*");
           builder.blacklist("cabletiers:*");
           builder.blacklist("littlelogistics:*");
           builder.blacklist("integratedterminals:*");
           builder.blacklist("integrateddynamics:*");
           builder.blacklist("integratedtunnels:*");
           builder.blacklist("megacells:*");
           builder.blacklist("extrastorage:*");
           builder.blacklist("ae2additions:*");
           builder.blacklist("botanicalmachinery:*");
           builder.blacklist("appbot:*");
           builder.blacklist("appmek:*");
           builder.blacklist("laserio:*");
           builder.blacklist("sfm:*");
           builder.blacklist("toms_storage:*");
           builder.blacklist("entangled:*");
           builder.blacklist("sophisticatedstorage:*");
           builder.blacklist("fluiddrawerslegacy:*");
           builder.blacklist("mysticalagriculture:*");
           builder.blacklist("industrialforegoing:*");
           builder.blacklist("pneumaticcraft:*");
           builder.blacklist("hostilenetworks:*");
           builder.blacklist("quarryplus:*");
           builder.blacklist("createaddition:*");
           builder.blacklist("createoreexcavation:*");
           builder.blacklist("createdieselgenerators:*");
           builder.blacklist("refinedstorageaddons:*");
           builder.blacklist("bonsaitrees3:*");

           builder.blacklist("alexsmobs:dimensional_carver");

           builder.blacklist("tiab:time_in_a_bottle");

           builder.blacklist("woldsvaults:mob_barrier_red");

           builder.blacklist("botania:laputa_shard");
           builder.blacklist("botania:odin_ring");
           builder.blacklist("botania:thor_ring");
           builder.blacklist("botania:loki_ring");
           builder.blacklist("botania:king_key");
           builder.blacklist("botania:infinite_fruit");
           builder.blacklist("botania:flugel_eye");
        });
    }
}
