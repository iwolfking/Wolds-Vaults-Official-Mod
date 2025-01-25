package xyz.iwolfking.woldsvaults.mixins.industrialforegoing;

import com.buuz135.industrial.block.misc.tile.EnchantmentApplicatorTile;
import com.buuz135.industrial.block.tile.IndustrialProcessingTile;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.data.enchantments.BannedEnchantmentsData;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "industrialforegoing")
        }
)
@Mixin(value = EnchantmentApplicatorTile.class)
public abstract class MixinEnchantmentApplicatorTile extends IndustrialProcessingTile<EnchantmentApplicatorTile> {



    public MixinEnchantmentApplicatorTile(Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> basicTileBlock, int x, int y, BlockPos blockPos, BlockState blockState) {
        super(basicTileBlock, x, y, blockPos, blockState);

    }


    @Redirect(method = "updateRepairOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean canEnchantInApplicator(Enchantment instance, ItemStack pStack) {

        return !BannedEnchantmentsData.BANNED_ENCHANT_REGISTRY_NAMES.contains(instance.getRegistryName().toString()) && !BannedEnchantmentsData.BANNED_APPLICATOR_ENCHANTMENTS.contains(instance.getRegistryName().toString());
    }
}
