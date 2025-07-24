package xyz.iwolfking.woldsvaults.items;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.TrinketPouchConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.*;
import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

public class TrinketPouchItem extends BasicItem implements ICurioItem {
    public TrinketPouchItem(ResourceLocation id) {
        super(id, new Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
    }


    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level.isClientSide || !stack.hasTag()) return;

        CompoundTag tag = stack.getOrCreateTag();



        if (!tag.contains("StoredCurios") || !tag.contains("id")) return;

        LazyOptional<ICuriosItemHandler> optHandler = CuriosApi.getCuriosHelper().getCuriosHandler(entity);
        if (!optHandler.isPresent()) return;

        ICuriosItemHandler handler = optHandler.resolve().get();

        TrinketPouchConfig.TrinketPouchConfigEntry pouchConfig = getPouchConfigFor(stack);

        List<String> requiredSlots = new ArrayList<>(pouchConfig.SLOT_ENTRIES.keySet());

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
        return slot.map(slotResult -> slotResult.stack().isEmpty()).orElse(true);
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
        TrinketPouchConfig.TrinketPouchConfigEntry pouchConfigEntry = getPouchConfigFor(stack);
        for (String slotType : pouchConfigEntry.SLOT_ENTRIES.keySet()) {
            CuriosApi.getCuriosHelper().addSlotModifier(map, slotType, uuid, pouchConfigEntry.SLOT_ENTRIES.get(slotType), AttributeModifier.Operation.ADDITION);
        }
        return map;
    }

    public static ItemStack create(ResourceLocation id, boolean isTemporary) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        if(isTemporary) {
            tag.putBoolean("temporary", true);
        }
        ItemStack stack = new ItemStack(ModItems.TRINKET_POUCH);
        stack.setTag(tag);
        return stack;
    }

    public static ItemStack create(ResourceLocation id) {
        return create(id, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        if(isTemporary(stack)) {
            tooltip.add(new TextComponent("This is a temporary pouch to hold your temporary stuffs!").withStyle(ChatFormatting.AQUA));
            tooltip.add(new TextComponent(""));
        }
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

    @Override
    public Component getName(ItemStack stack) {
        TrinketPouchConfig.TrinketPouchConfigEntry pouchConfigEntry = getPouchConfigFor(stack);
        return new TextComponent(pouchConfigEntry.NAME).withStyle(Style.EMPTY.withColor(pouchConfigEntry.COLOR));
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, @NotNull NonNullList<ItemStack> items) {
        if (category.equals(iskallia.vault.init.ModItems.VAULT_MOD_GROUP)) {
            items.add(create(WoldsVaults.id("basic_vanilla")));
            items.add(create(WoldsVaults.id("basic_alt_r")));
            items.add(create(WoldsVaults.id("basic_alt_g")));
            items.add(create(WoldsVaults.id("explorer")));
            items.add(create(WoldsVaults.id("light")));
            items.add(create(WoldsVaults.id("heavy")));
            items.add(create(WoldsVaults.id("standard")));
        }
    }

    public static TrinketPouchConfig.TrinketPouchConfigEntry getPouchConfigFor(ItemStack pouchStack) {
        CompoundTag tag = pouchStack.getOrCreateTag();
        if(!tag.contains("id")) {
            return new TrinketPouchConfig.TrinketPouchConfigEntry("Trinket Pouch", Map.of(), TextColor.fromLegacyFormat(ChatFormatting.WHITE));
        }
        ResourceLocation pouchId = ResourceLocation.tryParse(tag.getString("id"));

        return ModConfigs.TRINKET_POUCH.TRINKET_POUCH_CONFIGS.getOrDefault(pouchId, new TrinketPouchConfig.TrinketPouchConfigEntry("Trinket Pouch", Map.of(), TextColor.fromLegacyFormat(ChatFormatting.WHITE)));
    }

    public static Set<String> getSlotTypes(ItemStack pouch) {
        TrinketPouchConfig.TrinketPouchConfigEntry entry = getPouchConfigFor(pouch);
        return entry.SLOT_ENTRIES.keySet();
    }

    public static boolean isTemporary(ItemStack pouch) {
        return pouch.hasTag() && pouch.getOrCreateTag().contains("temporary");
    }

}
