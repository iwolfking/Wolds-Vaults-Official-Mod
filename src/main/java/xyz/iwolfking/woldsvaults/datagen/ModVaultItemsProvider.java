package xyz.iwolfking.woldsvaults.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.AbstractVaultItemsProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModBlocks;
import xyz.iwolfking.woldsvaults.init.ModItems;

public class ModVaultItemsProvider extends AbstractVaultItemsProvider {
    protected ModVaultItemsProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        add("burger_tweaks", builder -> {
           builder.configure(Builder.XPItem.PLAIN_BURGER,  100, 200);
           builder.configure(Builder.XPItem.CHEESE_BURGER,  200, 400);
           builder.configure(Builder.XPItem.DOUBLE_CHEESE_BURGER,  400, 600);
           builder.configure(Builder.XPItem.DELUXE_CHEESE_BURGER,  600, 1200);
           builder.configure(Builder.XPItem.CRISPY_DELUXE_CHEESE_BURGER,  1200, 2000);
           builder.configure(Builder.XPItem.SALTY_DELUXE_CHEESE_BURGER,  2000, 3000);
           builder.configure(Builder.XPItem.CHEESE_BURGER_FEAST,  3600, 4200);
           builder.configure(Builder.XPItem.SPICY_HEARTY_BURGER,  20000, 40000);
        });
    }
}
