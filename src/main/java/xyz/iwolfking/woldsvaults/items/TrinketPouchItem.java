package xyz.iwolfking.woldsvaults.items;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import iskallia.vault.item.BasicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchCapability;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchContents;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchMigration;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.menu.PouchMenu;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.TrinketPouchConfig;
import xyz.iwolfking.woldsvaults.init.ModConfigs;
import xyz.iwolfking.woldsvaults.init.ModCreativeTabs;
import xyz.iwolfking.woldsvaults.init.ModItems;

import javax.annotation.Nullable;
import java.util.*;

public class TrinketPouchItem extends BasicItem implements ICurioItem {
    public TrinketPouchItem(ResourceLocation id) {
        super(id, new Properties().stacksTo(1).tab(ModCreativeTabs.WOLDS_VAULTS));
    }


    @Override
    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (!context.identifier().equals("trinket_pouch") || context.index() != 0) {
            return false;
        }
        if (PouchMigration.restoring()) {
            return true;
        }
        ItemStack equippedStack = CuriosApi.getCuriosHelper().findCurio(context.entity(), "trinket_pouch", 0)
                .map(SlotResult::stack).orElse(ItemStack.EMPTY);
        if (equippedStack == stack) {
            return true;
        }
        if (context.entity() instanceof Player player && PouchHelper.locked(player)) {
            return false;
        }
        return equippedStack.isEmpty();
    }

    @Override
    public boolean canUnequip(SlotContext context, ItemStack stack) {
        return !(context.entity() instanceof Player player) || !PouchHelper.locked(player);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext context, UUID uuid, ItemStack stack) {
        if (context.entity() instanceof Player player && PouchHelper.equipped(player) == stack) {
            PouchMigration.equipped(player);
        }
        return LinkedHashMultimap.create();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            PouchMenu.open(serverPlayer, hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            PouchMigration.stored(stack);
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag share = stack.getTag() == null ? new CompoundTag() : stack.getTag().copy();
        share.put("PouchContents", PouchCapability.get(stack).serializeNBT());
        return share;
    }

    @Override
    public void readShareTag(ItemStack stack, CompoundTag share) {
        if (share == null) {
            stack.setTag(null);
            return;
        }
        CompoundTag ordinary = share.copy();
        ordinary.remove("PouchContents");
        stack.setTag(ordinary);
        if (share.contains("PouchContents", Tag.TAG_COMPOUND)) {
            PouchCapability.get(stack).deserializeNBT(share.getCompound("PouchContents"));
        }
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
            tooltip.add(new TranslatableComponent("item.woldsvaults.trinket_pouch_temporary").withStyle(ChatFormatting.AQUA));
        }
        PouchContents contents = PouchCapability.get(stack);
        if (!contents.isReadable()) {
            tooltip.add(new TranslatableComponent("item.woldsvaults.trinket_pouch.unreadable").withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.add(new TranslatableComponent("item.woldsvaults.trinket_pouch.contents",
                PouchContents.SIZE - contents.emptySlots(), PouchContents.SIZE)
                .withStyle(ChatFormatting.GRAY));
        if (contents.unreadableEntryCount() > 0) {
            tooltip.add(new TranslatableComponent("item.woldsvaults.trinket_pouch.unreadable_entries", contents.unreadableEntryCount())
                    .withStyle(ChatFormatting.GOLD));
        }
        appendActiveTrinketsTooltip(stack, contents, tooltip);
    }

    private static void appendActiveTrinketsTooltip(ItemStack pouch, PouchContents contents, List<Component> tooltip) {
        List<ItemStack> activeTrinkets = PouchHelper.validSelection(pouch, contents, contents.activeIndices()).stream()
                .map(contents::getStackInSlot)
                .sorted(Comparator.comparingInt(trinket -> PouchHelper.COLORS.indexOf(PouchHelper.color(trinket))))
                .toList();
        if (activeTrinkets.isEmpty()) {
            tooltip.add(new TranslatableComponent("item.woldsvaults.trinket_pouch.active.none")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(new TranslatableComponent("item.woldsvaults.trinket_pouch.active", activeTrinkets.size())
                .withStyle(ChatFormatting.GRAY));
        for (ItemStack trinket : activeTrinkets) {
            ChatFormatting color = switch (PouchHelper.color(trinket)) {
                case "red_trinket" -> ChatFormatting.RED;
                case "blue_trinket" -> ChatFormatting.AQUA;
                case "green_trinket" -> ChatFormatting.GREEN;
                default -> ChatFormatting.GRAY;
            };
            tooltip.add(new TranslatableComponent("item.woldsvaults.trinket_pouch.active.entry",
                    trinket.getHoverName().copy().withStyle(color), PouchHelper.remainingUses(trinket))
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(new TranslatableComponent("item.woldsvaults.trinket_pouch.active.equipped_only")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public Component getName(ItemStack stack) {
        TrinketPouchConfig.TrinketPouchConfigEntry pouchConfigEntry = getPouchConfigFor(stack);
        return new TranslatableComponent(pouchConfigEntry.NAME).withStyle(Style.EMPTY.withColor(pouchConfigEntry.COLOR));
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
