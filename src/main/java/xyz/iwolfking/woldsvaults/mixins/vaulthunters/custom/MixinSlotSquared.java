package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.Restrictions;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.mixins.accessors.ResultSlotAccessor;

import java.util.Set;

@Mixin(value = Slot.class, priority = 1500)
public final class MixinSlotSquared {

    @Shadow
    @Final
    public Container container;

    @TargetHandler(
        mixin = "iskallia.vault.mixin.MixinSlot",
        name = "preventRestrictedTake"
    )
    @WrapOperation(
        method = "@MixinSquared:Handler",
        at = @At(value = "INVOKE", target = "Liskallia/vault/research/ResearchTree;restrictedBy(Lnet/minecraft/world/item/ItemStack;Liskallia/vault/research/Restrictions$Type;)Ljava/lang/String;", remap = false)
    )
    private String bypassBackpackRestrictionString(
            ResearchTree instance,
            ItemStack resultStack,
            Restrictions.Type restrictionType,
            Operation<String> original
    ) {
        if (resultStack.getItem() instanceof BackpackItem) {
            if (resultStack.hasTag() && resultStack.getTag() != null) {
                if (resultStack.getTag().contains("clothColor") || resultStack.getTag().contains("borderColor")) {
                    Slot thisSlot = (Slot) (Object) this;
                    if (thisSlot instanceof ResultSlot resultSlot) {
                        for(int i = 0; i < ((ResultSlotAccessor)(resultSlot)).getCraftSlots().getContainerSize() - 1; i++) {
                            if(!((ResultSlotAccessor)(resultSlot)).getCraftSlots().getItem(i).isEmpty() && ((ResultSlotAccessor)(resultSlot)).getCraftSlots().getItem(i).getItem().getRegistryName().equals(resultStack.getItem().getRegistryName())) {
                                return null;
                            }
                        }
                    }
                }
            }
        }

        return original.call(instance, resultStack, restrictionType);
    }
}