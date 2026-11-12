package xyz.iwolfking.woldsvaults.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.items.gear.*;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Mixin(value = ItemStack.class, priority = 1501)
public abstract class MixinItemStack extends net.minecraftforge.common.capabilities.CapabilityProvider<ItemStack> implements net.minecraftforge.common.extensions.IForgeItemStack  {
    @Unique
    private static final Set<ResourceLocation> woldsvaults$warnedNoDurability = new HashSet<>();

    protected MixinItemStack(Class<ItemStack> baseClass) {
        super(baseClass);
    }

    /**
     * Keeps vault gear that has no durability from breaking. With a max durability of 0, a hit whose wear is fully
     * negated (Immortal Gear) still ends {@code hurt} with {@code 0 >= 0}, and {@code hurtAndBreak} then deletes the
     * stack instead of marking it broken.
     */
    @Dynamic(mixin = iskallia.vault.mixin.MixinItemStack.class)
    @Inject(method = "hurt", at = @At("RETURN"), cancellable = true)
    private void keepGearWithoutDurability(int amount, Random random, @Nullable ServerPlayer user, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!cir.getReturnValueZ() || !(stack.getItem() instanceof VaultGearItem) || stack.getMaxDamage() > 0) {
            return;
        }
        cir.setReturnValue(false);
        if (woldsvaults$warnedNoDurability.add(stack.getItem().getRegistryName())) {
            WoldLogger.warn("Vault gear {} has no durability; kept it from breaking instead of deleting it.",
                    stack.getItem().getRegistryName());
        }
    }



    @Dynamic(mixin = iskallia.vault.mixin.MixinItemStack.class)
    @WrapOperation(
        method = "hurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"),
        slice = @Slice(from = @At(value = "INVOKE", target = "iskallia/vault/util/calc/DurabilityWearReductionHelper.getDurabilityWearReduction (Lnet/minecraft/world/entity/LivingEntity;)F", remap = false))
    )
    public Item gearDurabilityPrestigeForWoldItems(ItemStack instance, Operation<Item> original) {
        Item item = original.call(instance);
        if (item instanceof VaultBattleStaffItem
            || item instanceof VaultTridentItem
            || item instanceof VaultPlushieItem
            || item instanceof VaultLootSackItem
            || item instanceof VaultRangItem
            || item instanceof VaultScepterItem) {
            return ModItems.FOCUS; // just pretend that wold items are focuses that already use the expertise
        }
        return item;
    }
}
