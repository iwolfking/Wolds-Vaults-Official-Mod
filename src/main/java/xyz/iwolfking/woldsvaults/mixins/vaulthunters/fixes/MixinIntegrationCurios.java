package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.integration.IntegrationSB;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import xyz.iwolfking.woldsvaults.items.TrinketPouchItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(value = IntegrationCurios.class, remap = false)
public class MixinIntegrationCurios {

    /**
     * @author iwolfking
     * @reason Fix issues with not handling non-existent curio slots.
     */
    @Overwrite
    public static List applyMappedSerializedCuriosItemStacks(Player player, CompoundTag tag, boolean replaceExisting) {
        return (List) player.getCapability(CuriosCapability.INVENTORY).map((inv) -> {
            List<ItemStack> filledItems = new ArrayList();

            for (String handlerKey : tag.getAllKeys()) {
                inv.getStacksHandler(handlerKey).ifPresent((handle) -> {
                    IDynamicStackHandler stackHandler = handle.getStacks();
                    CompoundTag handlerKeyMap = tag.getCompound(handlerKey);

                    for (String strSlot : handlerKeyMap.getAllKeys()) {
                        int slot;
                        try {
                            slot = Integer.parseInt(strSlot);
                        } catch (NumberFormatException var11) {
                            continue;
                        }

                        if (slot >= 0 && slot < stackHandler.getSlots()) {
                            ItemStack stack = ItemStack.of(handlerKeyMap.getCompound(strSlot));
                            if (ModList.get().isLoaded("sophisticatedbackpacksvh")) {
                                IntegrationSB.restoreSnapshotIfBackpack(stack);
                            }

                            if (!replaceExisting && !stackHandler.getStackInSlot(slot).isEmpty()) {
                                if(!(stack.getItem() instanceof TrinketPouchItem) && !(stack.getItem() instanceof WalletItem)) {
                                    filledItems.add(stack);
                                }
                                else {
                                    stackHandler.setStackInSlot(slot, stack);
                                }
                            } else {
                                stackHandler.setStackInSlot(slot, stack);
                            }
                        }
                        else {
                            ItemStack stack = ItemStack.of(handlerKeyMap.getCompound(strSlot));
                            filledItems.add(stack);
                        }
                    }

                });
            }

            return filledItems;
        }).orElse(Collections.emptyList());
    }
}
