package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
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

        add("warehouse", builder -> {
            InscriptionData data = InscriptionData.empty();
            data.setSize(10);
            data.setModel(109);
            data.setColor(7012096);
            data.add(new InscriptionData.Entry(VaultMod.id("vault/rooms/omega/warehouse"), ArchitectRoomEntry.Type.OMEGA, 1, 7012096));
            ItemStack inscription = new ItemStack(ModItems.INSCRIPTION);
            data.write(inscription);
            builder.addRecipe(WoldsVaults.id("warehouse"), inscription, inputs -> {
                inputs.add(new ItemStack(ModItems.CARD_JUICE, 24));
                inputs.add(new ItemStack(ModItems.ECHO_GEM, 16));
                inputs.add(new ItemStack(ModItems.INSCRIPTION_PIECE, 64));
                inputs.add(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.WOLD_STAR_CHUNK, 1));
            });
        });
    }
}

