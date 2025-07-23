package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.core.DataTransferItem;
import iskallia.vault.item.gear.RecyclableItem;
import iskallia.vault.item.gear.TrinketItem;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.Optional;

@Mixin(value = TrinketItem.class, remap = false)
public abstract class MixinTrinketItem extends BasicItem implements ICurioItem, DataTransferItem, RecyclableItem, IdentifiableItem {
    public MixinTrinketItem(ResourceLocation id) {
        super(id);
    }

    /**
     * @author iwolfking
     * @reason Prevent equipping multiple of the same trinket.
     */
    @Inject(method = "canEquip", at = @At("HEAD"), cancellable = true)
    public void canEquip(SlotContext slotContext, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slotContext.entity() instanceof Player player) {
            LazyOptional<IItemHandlerModifiable> curioHandlerOpt = CuriosApi.getCuriosHelper().getEquippedCurios(player);
            if(curioHandlerOpt.isPresent()) {
                IItemHandlerModifiable curioHandler = curioHandlerOpt.resolve().orElse(null);
                if(curioHandler != null) {
                    for(int i = 0; i < curioHandler.getSlots(); i++) {
                        ItemStack curioStack = curioHandler.getStackInSlot(i);
                        if(curioStack.getItem() instanceof TrinketItem) {
                         TrinketEffect<?> effect = TrinketItem.getTrinket(curioStack).orElse(null);
                         if(effect != null && effect.equals(TrinketItem.getTrinket(stack).orElse(null))) {
                             cir.setReturnValue(false);
                         }
                        }
                    }
                }
            }
        }
    }

    @Redirect(method = "canUnequip", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    public boolean allowUnequip(Optional<Vault> vault, @Local(argsOnly = true) ItemStack stack) {
        return vault.isPresent() && !TrinketItem.isUsableInVault(stack, vault.get().get(Vault.ID));
    }

    @Redirect(method = "canEquip", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    public boolean allowReequip(Optional<Vault> vault, @Local(argsOnly = true) ItemStack stack) {
        return vault.isPresent() && !TrinketItem.isUsableInVault(stack, vault.get().get(Vault.ID));
    }
}
