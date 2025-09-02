package xyz.iwolfking.woldsvaults.api.helper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.item.gear.TrinketItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.CuriosHelper;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TrinketHelper {
    public static void unequipTrinketStack(LivingEntity entity, ItemStack stack, String slotKey, int slotIndex) {
        entity.getCapability(CuriosCapability.INVENTORY).map((handler) -> (Boolean) handler.getStacksHandler(slotKey).map((stackHandler) -> {
            int slotCount = stackHandler.getSlots();
            if (slotIndex >= slotCount) {
                return false;
            } else {
                NonNullList<Boolean> renderStates = stackHandler.getRenders();
                SlotContext slotContext = new SlotContext(slotKey, entity, slotIndex, false, renderStates.size() > slotIndex && (Boolean)renderStates.get(slotIndex));
                CuriosApi.getCuriosHelper().getCurio(stack).ifPresent(iCurio -> {
                    iCurio.onUnequip(slotContext, stack);
                });

                stackHandler.getStacks().setStackInSlot(slotIndex, stack);
                return true;
            }
        }).orElse(false));
    }

    public static void clearCurios(Player entity) {
        CuriosApi.getCuriosHelper()
                .getCuriosHandler(entity)
                .ifPresent(
                        handler -> handler.getCurios()
                                .values()
                                .forEach(
                                        stacksHandler -> {
                                            IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                                            IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();
                                            String id = stacksHandler.getIdentifier();

                                            if (!id.equals("trinket_pouch")) {
                                                for (int i = 0; i < stackHandler.getSlots(); i++) {
                                                    UUID uuid = UUID.nameUUIDFromBytes((id + i).getBytes());
                                                    NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                                                    SlotContext slotContext = new SlotContext(id, entity, i, false, renderStates.size() > i && (Boolean) renderStates.get(i));
                                                    ItemStack stack = stackHandler.getStackInSlot(i);
                                                    Multimap<Attribute, AttributeModifier> map = CuriosApi.getCuriosHelper().getAttributeModifiers(slotContext, uuid, stack);
                                                    Multimap<String, AttributeModifier> slots = HashMultimap.create();
                                                    Set<CuriosHelper.SlotAttributeWrapper> toRemove = new HashSet<>();

                                                    for (Attribute attribute : map.keySet()) {
                                                        if (attribute instanceof CuriosHelper.SlotAttributeWrapper wrapper) {
                                                            slots.putAll(wrapper.identifier, map.get(attribute));
                                                            toRemove.add(wrapper);
                                                        }
                                                    }

                                                    for (Attribute attributex : toRemove) {
                                                        map.removeAll(attributex);
                                                    }

                                                    entity.getAttributes().removeAttributeModifiers(map);
                                                    handler.removeSlotModifiers(slots);
                                                    CuriosApi.getCuriosHelper().getCurio(stack).ifPresent(curio -> curio.onUnequip(slotContext, stack));
                                                    stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                                                    NetworkHandler.INSTANCE
                                                            .send(
                                                                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                                                                    new SPacketSyncStack(entity.getId(), id, i, ItemStack.EMPTY, SPacketSyncStack.HandlerType.EQUIPMENT, new CompoundTag())
                                                            );
                                                    cosmeticStackHandler.setStackInSlot(i, ItemStack.EMPTY);
                                                    NetworkHandler.INSTANCE
                                                            .send(
                                                                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                                                                    new SPacketSyncStack(entity.getId(), id, i, ItemStack.EMPTY, SPacketSyncStack.HandlerType.COSMETIC, new CompoundTag())
                                                            );
                                                }
                                            }
                                        }
                                )
                );
    }

}
