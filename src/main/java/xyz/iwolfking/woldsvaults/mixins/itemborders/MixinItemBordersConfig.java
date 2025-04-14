package xyz.iwolfking.woldsvaults.mixins.itemborders;

import com.anthonyhilyard.itemborders.ItemBordersConfig;
import com.electronwill.nightconfig.core.Config;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.item.VaultGearItem;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.Map;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "itemborders")
        }
)
@Mixin(value = ItemBordersConfig.class, remap = false)
public class MixinItemBordersConfig {
    @Shadow @Final public static ItemBordersConfig INSTANCE;

    @Inject(method = "getBorderColorForItem", at = @At("HEAD"), cancellable = true)
    public void getBorderColorForItem(ItemStack item, CallbackInfoReturnable<TextColor> cir) {
        if(item.getItem() instanceof VaultGearItem vaultGearItem) {
            GearDataCache cache = GearDataCache.of(item);
            cir.setReturnValue(cache.getRarity().getColor());
        }
    }
}
