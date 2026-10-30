package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.item.AugmentItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.datagen.lib.AbstractAugmentBoxProvider;

public class ModAugmentBoxProvider extends AbstractAugmentBoxProvider {
    protected ModAugmentBoxProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("add_new_themes", builder -> {
            builder.addItems(productEntryListBuilder -> {
               productEntryListBuilder.add(AugmentItem.create(WoldsVaults.id("lunar")), 1);
               productEntryListBuilder.add(AugmentItem.create(WoldsVaults.id("red_planet")), 1);
               productEntryListBuilder.add(AugmentItem.create(WoldsVaults.id("sculk")), 1);
               productEntryListBuilder.add(AugmentItem.create(ResourceLocation.fromNamespaceAndPath("skaia", "abandoned_lab")), 1);
            });
        });
    }
}
