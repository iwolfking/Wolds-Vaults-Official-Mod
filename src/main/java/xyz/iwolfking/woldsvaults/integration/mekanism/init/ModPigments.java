package xyz.iwolfking.woldsvaults.integration.mekanism.init;

import iskallia.vault.item.CardItem;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.common.registration.impl.PigmentDeferredRegister;
import mekanism.common.registration.impl.PigmentRegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;

public class ModPigments {
    public static final PigmentDeferredRegister PIGMENTS = new PigmentDeferredRegister("your_mod_id");

    public static final PigmentRegistryObject<Pigment> ENCHANTED_RED_PIGMENT = PIGMENTS.register(
        "enchanted_red",
        0xFF133B
    );

//    public static final PigmentRegistryObject<Pigment> CUSTOM_PIGMENT = PIGMENTS.register(
//        "custom_pigment",
//            PigmentBuilder.builder(ResourceLocation.fromNamespaceAndPath(WoldsVaults.MOD_ID, "chemical/enchanted_pigment"))
//                          .color(0x33FF57).getColor()
//    );

    public static void register(IEventBus eventBus) {
        PIGMENTS.register(eventBus);
    }
}

//Red Pigment
//Vault Essence (chemical)
//Red Vault Essence

//Pigment Extractor - Red Vault Essence -> Impure Enchanted Red
//Impure Enchanted Red -> chemical processing - mix with Vault Essence (chemical) ->
