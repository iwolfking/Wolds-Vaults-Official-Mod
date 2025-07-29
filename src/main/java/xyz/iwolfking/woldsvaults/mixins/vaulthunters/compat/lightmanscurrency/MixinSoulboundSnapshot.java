package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import cofh.ensorcellation.init.EnsorcEnchantments;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.world.data.SoulboundSnapshotData;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.entity.player.Player;
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
@Mixin(SoulboundSnapshotData.class)
public class MixinSoulboundSnapshot {
    /**
     * @author iwolfking
     * @reason Add wallets to soulbound snapshot
     */
    @Overwrite(remap = false)
    protected boolean shouldSnapshotItem(Player player, ItemStack stack) {
        return !stack.isEmpty() && (stack.hasTag() && !stack.getTag().getBoolean("VaultRoyaleLootable")) && (AttributeGearData.read(stack).get(ModGearAttributes.SOULBOUND, VaultGearAttributeTypeMerger.anyTrue()) || stack.getItem() instanceof WalletItem || (stack.getItem() instanceof TrinketPouchItem && !TrinketPouchItem.isTemporary(stack)) || EnchantmentHelper.getEnchantments(stack).containsKey(EnsorcEnchantments.SOULBOUND.get()));
    }
}
