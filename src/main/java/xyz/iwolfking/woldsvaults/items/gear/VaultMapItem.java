package xyz.iwolfking.woldsvaults.items.gear;

import com.google.common.collect.Multimap;
import iskallia.vault.gear.VaultGearClassification;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.crafting.ProficiencyType;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.BasicItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

import java.util.List;
import java.util.Random;

public class VaultMapItem extends BasicItem implements VaultGearItem {

    Random rand = new Random();

    public VaultMapItem(ResourceLocation id, Properties properties) {
        super(id, properties);
    }

    @NotNull
    @Override
    public VaultGearClassification getClassification(ItemStack itemStack) {
        return VaultGearClassification.valueOf("MAP");
    }

    @NotNull
    @Override
    @SuppressWarnings({"removal", "deprecation"})
    public ProficiencyType getCraftingProficiencyType(ItemStack itemStack) {
        return ProficiencyType.UNKNOWN;
    }

    @NotNull
    @Override
    public VaultGearType getGearType(ItemStack itemStack) {
        return VaultGearType.JEWEL;
    }

    @Nullable
    @Override
    public ResourceLocation getRandomModel(ItemStack stack, Random random) {
        VaultGearData gearData = VaultGearData.read(stack);
        EquipmentSlot intendedSlot = this.getEquipmentSlot(stack);
        return ModConfigs.GEAR_MODEL_ROLL_RARITIES.getRandomRoll(stack, gearData, intendedSlot, random);
    }

    @Override
    public void tickRoll(ItemStack stack, @Nullable Player player) {
        VaultGearData data = VaultGearData.read(stack);
        int tier;
        if (data.getState() != VaultGearState.IDENTIFIED) {
            tier = data.getFirstValue(ModGearAttributes.MAP_TIER).orElse(-1);
            if(tier == -1) {
                data.createOrReplaceAttributeValue(ModGearAttributes.MAP_TIER, rand.nextInt(0, 10));
            }
            data.write(stack);
        }
        VaultGearItem.super.tickRoll(stack, player);
    }


    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            items.add(this.defaultItem());
        }

    }

    public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
        return super.getDefaultTooltipHideFlags(stack) | ItemStack.TooltipPart.MODIFIERS.getMask();
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return VaultGearHelper.getModifiers(stack, slot);
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return VaultGearHelper.shouldPlayGearReequipAnimation(oldStack, newStack, slotChanged);
    }

    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    public int getMaxDamage(ItemStack stack) {
        return 1;
    }

    public Component getName(ItemStack stack) {
        return VaultGearHelper.getDisplayName(stack, super.getName(stack));
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return VaultGearHelper.rightClick(world, player, hand, super.use(world, player, hand));
    }

    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, itemSlot, isSelected);
        if (entity instanceof ServerPlayer player) {
            this.vaultGearTick(stack, player);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.addAll(this.createTooltip(stack, GearTooltip.itemTooltip()));
    }

    @Override
    public void addRepairTooltip(List<Component> tooltip, int usedRepairs, int totalRepairs) {

    }

    @Override
    public void addTooltipItemLevel(VaultGearData data, ItemStack stack, List<Component> tooltip, VaultGearState state) {
        if(data.hasAttribute(ModGearAttributes.MAP_TIER)) {
            int tier = data.getFirstValue(ModGearAttributes.MAP_TIER).orElse(0);
            tooltip.add(new TextComponent("Tier: ").append(new TextComponent(String.valueOf(tier)).withStyle(Style.EMPTY.withColor(getTierColor(tier)))));
        }
        else if(stack.getOrCreateTag().contains("the_vault:map_tier")) {
            int tier = stack.getOrCreateTag().getInt("the_vault:map_tier");
            if(tier == -1) {
                tooltip.add(new TextComponent("Tier: ").append(new TextComponent("???").withStyle(Style.EMPTY.withColor(7247291))));
            }
            else {
                tooltip.add(new TextComponent("Tier: ").append(new TextComponent(String.valueOf(tier)).withStyle(Style.EMPTY.withColor(getTierColor(tier)))));
            }
        }
        else {
            tooltip.add(new TextComponent("Tier: ").append(new TextComponent("???").withStyle(Style.EMPTY.withColor(7247291))));
        }
    }


    public int getTierColor(int tier) {
        return switch (tier) {
            case 9 -> 7877375;
            case 8 -> 9974527;
            case 7 -> 9974403;
            case 6 -> 9974337;
            case 5 -> 3307654;
            case 4 -> 3295110;
            case 3 -> 3295017;
            case 2 -> 3374592;
            case 1 -> 9240575;
            default -> 14352383;
        };
    }
}
