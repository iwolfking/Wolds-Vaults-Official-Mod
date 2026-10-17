package xyz.iwolfking.woldsvaults.items;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.recipe.anvil.AnvilContext;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.CrystalDataUtils;
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;

public class InfusionPreventionItem extends BasicItem implements IVaultCrystalModifier {
    public InfusionPreventionItem() {
        super(WoldsVaults.id("infusion_preventer"));
    }

    @Override
    public boolean applyCrystalRecipe(AnvilContext context, CrystalData data, ItemStack ingredient, ItemStack output) {
        if(hasApplied(ingredient)) {
            return false;
        }

        data.addModifierByCrafting(new VaultModifierStack(VaultModifierRegistry.getOrDefault(WoldsVaults.id("dulled"), VaultModifierRegistry.get(VaultMod.id("empty"))), 1), false, false);
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
        return 0;
    }

    @Override
    public boolean hasApplied(ItemStack crystalStack) {
        CrystalData data = CrystalData.read(crystalStack);
        return CrystalDataUtils.hasCountOfModifiers(WoldsVaults.id("dulled"), data, 1);
    }
}
