package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.properties.CapacityCrystalProperties;
import iskallia.vault.recipe.anvil.AnvilContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.crystal.tasks.ConcealedChaosBackfireTask;
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;
import java.util.Random;

public class ConcealedChaosItem extends Item implements IVaultCrystalModifier {

    public ConcealedChaosItem(ResourceLocation id, Properties pProperties) {
        super(pProperties);
        this.setRegistryName(id);
    }

    @Override
    public boolean applyCrystalRecipe(AnvilContext context, CrystalData data, ItemStack ingredient, ItemStack output) {
        int size = getCapacityConsumption(ingredient);

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
        if(random.nextFloat() <= 0.5F) {
            VaultCrystalItem.scheduleTask(ConcealedChaosBackfireTask.INSTANCE, output);
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

    @Override
    public int getCapacityConsumption(ItemStack stack) {
        return 50;
    }

    public boolean hasApplied(ItemStack crystalStack) {
        if (crystalStack.getItem() instanceof VaultCrystalItem && crystalStack.hasTag()) {
            CompoundTag nbt = crystalStack.getTag();
            if (nbt != null && nbt.contains("scheduledTasks")) {
                CompoundTag scheduledTasks = nbt.getCompound("scheduledTasks");

                for (String key : scheduledTasks.getAllKeys()) {
                    CompoundTag taskTag = scheduledTasks.getCompound(key);
                    String id = taskTag.contains("id") ? taskTag.getString("id") : key;

                    int suffixIndex = id.lastIndexOf('#');
                    if (suffixIndex > 0) {
                        id = id.substring(0, suffixIndex);
                    }

                    if (id.equals(ConcealedChaosBackfireTask.ID) || id.equals(VaultCrystalItem.AddModifiersTask.ID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
