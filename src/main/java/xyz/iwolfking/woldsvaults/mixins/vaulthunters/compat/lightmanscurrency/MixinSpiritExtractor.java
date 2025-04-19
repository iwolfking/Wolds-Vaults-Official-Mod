package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import cofh.ensorcellation.init.EnsorcEnchantments;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import iskallia.vault.container.SpiritExtractorContainer;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.init.ModGearAttributes;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.woldsvaults.items.TrinketPouchItem;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "lightmanscurrency")
        }
)
@Mixin(SpiritExtractorContainer.class)
public class MixinSpiritExtractor {
    /**
     * @author iwolfking
     * @reason Remove wallets from spirit extractor
     */
    @Overwrite(remap = false)
    private static boolean shouldAddItem(ItemStack stack) {
        return !stack.isEmpty() && (!AttributeGearData.hasData(stack) || !(Boolean) AttributeGearData.read(stack).get(ModGearAttributes.SOULBOUND, VaultGearAttributeTypeMerger.anyTrue()) && !(stack.getItem() instanceof WalletItem) && !(stack.getItem() instanceof TrinketPouchItem) && !(EnchantmentHelper.getEnchantments(stack).containsKey(EnsorcEnchantments.SOULBOUND.get())));
    }
}
