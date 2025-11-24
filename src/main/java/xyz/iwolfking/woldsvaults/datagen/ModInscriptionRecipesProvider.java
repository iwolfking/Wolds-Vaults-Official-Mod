package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.core.world.generator.layout.ArchitectRoomEntry;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.data.InscriptionData;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.iwolfking.vhapi.api.datagen.recipes.AbstractInscriptionRecipesProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class ModInscriptionRecipesProvider extends AbstractInscriptionRecipesProvider {
    protected ModInscriptionRecipesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {

        add("test", builder -> {
            InscriptionData data = InscriptionData.empty();
            data.setSize(10);
            data.setModel(5);
            data.setColor(3);
            data.add(ArchitectRoomEntry.Type.OMEGA, 1, 3);
            ItemStack inscription = new ItemStack(ModItems.INSCRIPTION);
            data.write(inscription);
            builder.addRecipe(WoldsVaults.id("test_recipe"), inscription, inputs -> {
                inputs.add(new ItemStack(Items.APPLE, 3));
            });
        });
    }
}

