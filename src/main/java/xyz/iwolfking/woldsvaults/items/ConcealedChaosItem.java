package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.properties.CapacityCrystalProperties;
import iskallia.vault.recipe.anvil.AnvilContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;
import java.util.Random;

public class ConcealedChaosItem extends Item implements IVaultCrystalModifier {

    public ConcealedChaosItem(ResourceLocation id, Properties pProperties) {
        super(pProperties);
        this.setRegistryName(id);
    }

    @Override
    public boolean applyCrystalRecipe(AnvilContext context, CrystalData data, ItemStack ingredient, ItemStack output) {
        int size = 50;

        if (data.getProperties() instanceof CapacityCrystalProperties properties) {
            Integer capacity = properties.getCapacity().orElse(null);
            Integer level = properties.getLevel().orElse(null);
            if (capacity == null || level == null) {
                return false;
            }

            if (capacity < size) {
                return false;
            }

            properties.setSize(properties.getSize() + size);
        }


        Random random = new Random();
        if(random.nextFloat() <= 0.33F) {
            VaultCrystalItem.scheduleTask(new VaultCrystalItem.AddModifiersTask(WoldsVaults.id("concealed_chaos_backfire"), 1), output);
            data.getProperties().setUnmodifiable(true);
        }
        else {
            VaultCrystalItem.scheduleTask(new VaultCrystalItem.AddModifiersTask(WoldsVaults.id("concealed_chaos"), 1), output);
        }

        data.write(output);
        context.setOutput(output);

        context.onTake(context.getTake().append(() -> {
            context.getInput()[0].shrink(1);
            context.getInput()[1].shrink(1);
        }));
        return true;
    }
}
