package xyz.iwolfking.woldsvaults.items;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.TalentDialog;
import iskallia.vault.client.gui.screen.player.legacy.widget.TalentWidget;
import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import javax.annotation.Nullable;
import java.util.*;

public class TrinketPouchItem extends BasicItem implements ICurioItem {
    private final Map<String, Integer> curioSlotsToAdd;

    public TrinketPouchItem(ResourceLocation id, Map<String, Integer> curioSlotsToAdd) {
        super(id, new Properties().stacksTo(1));
        this.curioSlotsToAdd = curioSlotsToAdd;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level.isClientSide || !stack.hasTag()) return;

        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains("StoredCurios")) return;

        LazyOptional<ICuriosItemHandler> optHandler = CuriosApi.getCuriosHelper().getCuriosHandler(entity);
        if (!optHandler.isPresent()) return;

        ICuriosItemHandler handler = optHandler.resolve().get();

        List<String> requiredSlots = new ArrayList<>(curioSlotsToAdd.keySet());

        // Check if all required slots exist and have capacity
        for (String slotId : requiredSlots) {
            if (handler.getStacksHandler(slotId).isEmpty()) {
                return; // defer to future tick
            }
            if (CuriosApi.getSlotHelper().getSlotsForType(entity, slotId) <= 0) {
                return; // slot not added yet, defer
            }
        }

        // If we reached here, all slots are valid, safe to restore
        ListTag storedList = tag.getList("StoredCurios", Tag.TAG_COMPOUND);
        for (int i = 0; i < storedList.size(); i++) {
            CompoundTag itemTag = storedList.getCompound(i);
            String slotId = itemTag.getString("Slot");
            int slotIndex = itemTag.getInt("Index");

            ItemStack restored = ItemStack.of(itemTag);

            if(handler.getStacksHandler(slotId).isEmpty()) {
                return;
            }

            handler.getStacksHandler(slotId).ifPresent(slotHandler -> {
                IItemHandlerModifiable slotStacks = slotHandler.getStacks();
                if (slotIndex < slotStacks.getSlots() && slotStacks.getStackInSlot(slotIndex).isEmpty()) {
                    slotStacks.setStackInSlot(slotIndex, restored);
                } else {
                    // fallback: try to insert into any available slot
                    for (int j = 0; j < slotStacks.getSlots(); j++) {
                        if (slotStacks.getStackInSlot(j).isEmpty()) {
                            slotStacks.setStackInSlot(j, restored);
                            break;
                        }
                    }
                }
            });
        }

        // Clean up NBT after successful restore
        tag.remove("StoredCurios");
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        Optional<SlotResult> slot = CuriosApi.getCuriosHelper().findCurio(slotContext.entity(), "trinket_pouch", 0);
        return slot.map(slotResult -> slotResult.stack().is(Items.AIR)).orElse(true);
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        if(slotContext.entity() instanceof Player player) {
            return !player.level.dimension().location().getNamespace().equals("the_vault");
        }

        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = LinkedHashMultimap.create();
        for (String slotType : curioSlotsToAdd.keySet()) {
            CuriosApi.getCuriosHelper().addSlotModifier(map, slotType, uuid, curioSlotsToAdd.get(slotType), AttributeModifier.Operation.ADDITION);
        }
        return map;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);

        if (!stack.hasTag()) return;
        CompoundTag tag = stack.getOrCreateTag();

        if (tag.contains("StoredCurios", Tag.TAG_LIST)) {
            ListTag storedList = tag.getList("StoredCurios", Tag.TAG_COMPOUND);
            if (!storedList.isEmpty()) {
                tooltip.add(new TextComponent("Stored Trinkets:").withStyle(ChatFormatting.GRAY));

                for (int i = 0; i < storedList.size(); i++) {
                    CompoundTag itemTag = storedList.getCompound(i);
                    ItemStack trinket = ItemStack.of(itemTag);
                    Component name = trinket.getHoverName();

                    tooltip.add(new TextComponent("• ").append(name).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }


}
