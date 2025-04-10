package xyz.iwolfking.woldsvaults.mixins.cofhcore;

import cofh.core.init.CoreEnchantments;
import cofh.ensorcellation.init.EnsorcEnchantments;
import cofh.lib.enchantment.EnchantmentCoFH;
import iskallia.vault.util.EnchantmentUtil;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "cofhcore")
        }
)
@Mixin(value = EnchantmentCoFH.class, remap = false)
public class MixinCofhEnchantment extends Enchantment{

    protected MixinCofhEnchantment(Enchantment.Rarity rarityIn, EnchantmentCategory typeIn, EquipmentSlot[] slots) {
        super(rarityIn, typeIn, slots);
    }

    @Inject(
            method = {"canApplyAtEnchantingTable"},
            at = {@At("HEAD")},
            cancellable = true,
            remap = false
    )
    private void canGetAtEnchantingTable(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        if(((EnchantmentCoFH)(Object)this).equals(EnsorcEnchantments.SOULBOUND.get())) {
            ci.setReturnValue(false);
        }
        if (EnchantmentUtil.isEnchantmentBlocked(this, stack)) {
            ci.setReturnValue(false);
        }
    }

    /**
     * @author iwolfking
     * @reason Always disallow trading for Ensorcellation enchants
     */
    @Overwrite @Override
    public boolean isTradeable() {
        return false;
    }

    /**
     * @author iwolfking
     * @reason Always disallow books for Ensorcellation enchants
     */
    @Overwrite @Override
    public boolean isAllowedOnBooks() {
        return false;
    }
}
