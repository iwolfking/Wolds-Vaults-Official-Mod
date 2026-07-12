package xyz.iwolfking.woldsvaults.blocks;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.init.ModBlocks;

import javax.annotation.Nonnull;

public class DecoVelaraAltarBlockItem extends BlockItem {

    public DecoVelaraAltarBlockItem() {
        super(ModBlocks.DECO_VELARA_ALTAR_BLOCK, new Item.Properties().tab(ModItems.VAULT_MOD_GROUP));
    }

    public static ItemStack fromType(VaultGod god) {
        ItemStack stack = new ItemStack(ModBlocks.DECO_VELARA_ALTAR_BLOCK);
        stack.getOrCreateTag().putString("god", god.getName().toLowerCase());
        return stack;
    }

    @Nonnull
    public Component getName(ItemStack itemStack) {
        CompoundTag nbt = itemStack.getTag();
        if (nbt != null && nbt.contains("god", 8)) {
            String god = nbt.getString("god");
            String var10002 = itemStack.getItem().getDescriptionId();
            return new TranslatableComponent(var10002 + "." + god);
        } else {
            return super.getName(itemStack);
        }
    }
}