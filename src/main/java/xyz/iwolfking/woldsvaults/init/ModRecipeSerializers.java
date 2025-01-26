package xyz.iwolfking.woldsvaults.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.recipes.lib.InfuserRecipe;

public class ModRecipeSerializers {

    public static final RecipeSerializer<InfuserRecipe> INFUSER = new InfuserRecipe.Serializer();

    @SubscribeEvent
    public void onRegisterSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        System.out.println("REGISTERING INFUSER RECIPES");
        var registry = event.getRegistry();

        registry.register(INFUSER.setRegistryName(new ResourceLocation(WoldsVaults.MOD_ID, "infuser")));
    }
}