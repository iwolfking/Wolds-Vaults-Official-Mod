package xyz.iwolfking.woldsvaults.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.vhapi.api.util.ConditionalModUtils;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.integration.arsnouveau.init.ArsRecipeSerializers;
import xyz.iwolfking.woldsvaults.recipes.lib.InfuserRecipe;

public class ModRecipeSerializers {

    public static final RecipeSerializer<InfuserRecipe> INFUSER = new InfuserRecipe.Serializer();

    @SubscribeEvent
    public void onRegisterSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        var registry = event.getRegistry();

        registry.register(INFUSER.setRegistryName(WoldsVaults.id("infuser")));
        if(ConditionalModUtils.isModPresent("ars_nouveau")) {
            registry.register(ArsRecipeSerializers.VAULT_GEAR_ENCHANTING_APPARATUS.setRegistryName(WoldsVaults.id("vault_gear_enchanting")));
            registry.register(ArsRecipeSerializers.VAULT_CATALYST_INFUSION.setRegistryName(WoldsVaults.id("vault_catalyst_infusion")));
        }
    }
}