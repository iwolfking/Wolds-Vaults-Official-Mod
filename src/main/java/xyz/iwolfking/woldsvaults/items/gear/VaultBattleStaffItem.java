package xyz.iwolfking.woldsvaults.items.gear;

import com.google.common.collect.Multimap;
import iskallia.vault.dynamodel.DynamicModel;
import iskallia.vault.gear.VaultGearClassification;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.crafting.ProficiencyType;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.item.VaultGearToolTier;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.gear.VaultSwordItem;
import iskallia.vault.util.MiscUtils;
import iskallia.vault.world.data.DiscoveredModelsData;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.models.Battlestaffs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class VaultBattleStaffItem extends SwordItem implements VaultGearItem, DyeableLeatherItem {

    public VaultBattleStaffItem(ResourceLocation id, Item.Properties builder) {
        super(VaultGearToolTier.INSTANCE, 0, -2.4F, builder);
        setRegistryName(id);
    }



    @Nullable
    public ResourceLocation getRandomModel(ItemStack stack, Random random, @Nullable Player player, @Nullable DiscoveredModelsData discoveredModelsData) {
        VaultGearData gearData = VaultGearData.read(stack);
        EquipmentSlot intendedSlot = this.getGearType(stack).getEquipmentSlot();
        return ModConfigs.GEAR_MODEL_ROLL_RARITIES.getRandomRoll(stack, gearData, intendedSlot, random, player, discoveredModelsData);
    }


    @Override
    public Optional<? extends DynamicModel<?>> resolveDynamicModel(ItemStack stack, ResourceLocation key) {
        return Battlestaffs.REGISTRY.get(key);
    }


    @Nullable
    public EquipmentSlot getIntendedSlot(ItemStack stack) {
        return EquipmentSlot.MAINHAND;
    }


    @NotNull
    public VaultGearClassification getClassification(ItemStack stack) {
        return VaultGearClassification.SWORD;
    }


    @Nonnull @SuppressWarnings({"deprecation","removal"})
    public ProficiencyType getCraftingProficiencyType(ItemStack stack) {
        return ProficiencyType.SWORD;
    }

    @NotNull
    @Override
    public VaultGearType getGearType(ItemStack itemStack) {
        return VaultGearType.SWORD;
    }




    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 1.0F;
    }


    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return false;
    }


    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return VaultGearHelper.getModifiers(stack, slot);
    }


    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return VaultGearHelper.shouldPlayGearReequipAnimation(oldStack, newStack, slotChanged);
    }


    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (allowdedIn(group)) {
            items.add(defaultItem());
        }
    }


    @Override
    public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
        return super.getDefaultTooltipHideFlags(stack) | ItemStack.TooltipPart.MODIFIERS.getMask();
    }


    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }


    @Override
    public boolean isDamageable(ItemStack stack) {
        return (VaultGearData.read(stack).getState() == VaultGearState.IDENTIFIED);
    }


    @Override
    public int getMaxDamage(ItemStack stack) {
        return (VaultGearData.read(stack)
            .get(ModGearAttributes.DURABILITY, VaultGearAttributeTypeMerger.intSum())).intValue();
    }


    @Override
    public Component getName(ItemStack stack) {
        return VaultGearHelper.getDisplayName(stack, super.getName(stack));
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return VaultGearHelper.rightClick(world, player, hand, super.use(world, player, hand));
    }


    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, itemSlot, isSelected);
        if (entity instanceof ServerPlayer player) {
            vaultGearTick(stack, player);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.addAll(createTooltip(stack, GearTooltip.itemTooltip()));
    }


    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return (ToolActions.SWORD_SWEEP == toolAction);
    }
}


