package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.block.entity.WardrobeTileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import xyz.iwolfking.woldsvaults.init.ModItems;

import java.util.*;

@Mixin(value = WardrobeTileEntity.class, remap = false)
public class MixinWardrobeTileEntity {
    @Redirect(method = "swapCuriosSlots", at = @At(value = "INVOKE", target = "Liskallia/vault/integration/IntegrationCurios;getCuriosItemStacks(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Map;"))
    private Map<String, List<Tuple<ItemStack, Integer>>> removePouchFromCurioSlots(LivingEntity entity) {
        return woldsVaults$getFilteredCurioStacks(entity);
    }

    @Unique
    private static Map<String, List<Tuple<ItemStack, Integer>>> woldsVaults$getFilteredCurioStacks(LivingEntity entity) {
        return entity.getCapability(CuriosCapability.INVENTORY).map((inv) -> {
            Map<String, List<Tuple<ItemStack, Integer>>> contents = new HashMap<>();
            inv.getCurios().forEach((key, handle) -> {
                IDynamicStackHandler stackHandler = handle.getStacks();

                for(int index = 0; index < stackHandler.getSlots(); ++index) {
                    if(stackHandler.getStackInSlot(index).is(ModItems.TRINKET_POUCH)) {
                        continue;
                    }
                    (contents.computeIfAbsent(key, (str) -> new ArrayList<>())).add(new Tuple<>(stackHandler.getStackInSlot(index), index));
                }

            });
            return contents;
        }).orElse(Collections.emptyMap());
    }
}
