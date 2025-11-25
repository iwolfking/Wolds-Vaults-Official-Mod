package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.core.world.roll.DoubleRoll;
import iskallia.vault.core.world.roll.FloatRoll;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.tool.ToolMaterial;
import net.minecraft.data.DataGenerator;
import xyz.iwolfking.vhapi.api.datagen.AbstractPrebuiltToolProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;

public class ModPrebuiltToolsProvider extends AbstractPrebuiltToolProvider {
    protected ModPrebuiltToolsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

        add("wolds_prebuilts", builder -> {
            builder.addTool("royale_stick", toolAffixBuilder -> {
                toolAffixBuilder
                        .implicit(ModGearAttributes.GILDED_AFFINITY, true)
                        .implicit(ModGearAttributes.WOODEN_AFFINITY, true)
                        .implicit(ModGearAttributes.ORNATE_AFFINITY, true)
                        .implicit(ModGearAttributes.LIVING_AFFINITY, true)
                        .implicit(ModGearAttributes.COIN_AFFINITY, true)
                        .prefix(ModGearAttributes.MINING_SPEED, FloatRoll.ofUniformedStep(60F, 145F, 1.0F))
                        .prefix(ModGearAttributes.ITEM_QUANTITY, FloatRoll.ofUniformedStep(0.1F, 0.2F, 0.01F))
                        .prefix(ModGearAttributes.REACH, FloatRoll.ofUniformedStep(0.1F, 0.25F, 0.1F))
                        .prefix(ModGearAttributes.TRAP_DISARMING, FloatRoll.ofUniformedStep(0.25F, 1.25F, 0.01F))
                        .material(ToolMaterial.ROYALE)
                        .build();
            }).build();
        });

    }
}
