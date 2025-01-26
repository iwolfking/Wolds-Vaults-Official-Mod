package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.ensorcellation;


import cofh.ensorcellation.init.EnsorcEnchantments;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.gear.WandItem;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.data.enchantments.AllowedEnchantmentsData;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "cofh_core"),
                @Condition(type = Condition.Type.MOD, value = "ensorcellation"),
        }
)
@Mixin(value = WandItem.class, remap = false)
public abstract class MixinVaultWand extends BasicItem implements VaultGearItem {
    public MixinVaultWand(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(AllowedEnchantmentsData.isAllowedUtilityEnchantment(enchantment)) {
            return true;
        }
        else {
            return super.canApplyAtEnchantingTable(stack, enchantment);
        }

    }
}
