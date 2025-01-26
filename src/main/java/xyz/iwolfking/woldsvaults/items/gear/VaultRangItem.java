package xyz.iwolfking.woldsvaults.items.gear;

import cofh.ensorcellation.init.EnsorcEnchantments;
import com.google.common.collect.Multimap;
import iskallia.vault.dynamodel.DynamicModel;
import iskallia.vault.gear.VaultGearClassification;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.crafting.ProficiencyType;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.BasicItem;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.quark.base.handler.QuarkSounds;
import xyz.iwolfking.woldsvaults.data.enchantments.AllowedEnchantmentsData;
import xyz.iwolfking.woldsvaults.items.gear.rang.VaultRangEntity;
import xyz.iwolfking.woldsvaults.models.Rangs;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class VaultRangItem extends BasicItem implements VaultGearItem, DyeableLeatherItem {

    public VaultRangItem(ResourceLocation id,  Item.Properties properties) {
        super(id, properties);
    }


    @Override
    public boolean isCorrectToolForDrops(@Nonnull BlockState blockIn) {
        return false;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return VaultGearHelper.getDisplayName(stack, super.getName(stack));
    }



    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, @Nonnull InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        if(VaultGearHelper.rightClick(worldIn, playerIn, handIn, super.use(worldIn, playerIn, handIn)).getResult().equals(InteractionResult.FAIL)) {
            return InteractionResultHolder.fail(itemstack);
        }
        playerIn.setItemInHand(handIn, ItemStack.EMPTY);
        AttributeSnapshot snapshot = AttributeSnapshotHelper.getInstance().getSnapshot(playerIn);
        float velocity = snapshot.getAttributeValue(ModGearAttributes.VELOCITY, VaultGearAttributeTypeMerger.floatSum()) * 100;
        Double attackSpeed = snapshot.getAttributeValue(ModGearAttributes.ATTACK_SPEED, VaultGearAttributeTypeMerger.doubleSum());
        Vec3 pos = playerIn.position();
        worldIn.playSound(null, pos.x, pos.y, pos.z, QuarkSounds.ENTITY_PICKARANG_THROW, SoundSource.NEUTRAL, 0.5F + velocity * 0.14F, 0.4F / (worldIn.random.nextFloat() * 0.4F + 0.8F));

        if(!worldIn.isClientSide) {
            Inventory inventory = playerIn.getInventory();
            int slot = handIn == InteractionHand.OFF_HAND ? inventory.getContainerSize() - 1 : inventory.selected;
            VaultRangEntity entity = new VaultRangEntity(worldIn, playerIn);
            entity.setThrowData(slot, itemstack);
            entity.shoot(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.5F + velocity * 0.325F, 0F);
            worldIn.addFreshEntity(entity);
        }

        if(!playerIn.getAbilities().instabuild) {
            int cooldown = 10 - attackSpeed.intValue();
            if (cooldown > 0)
                playerIn.getCooldowns().addCooldown(this, cooldown);
        }

        playerIn.awardStat(Stats.ITEM_USED.get(this));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        return 0F;
    }

    @Override
    public Optional<? extends DynamicModel<?>> resolveDynamicModel(ItemStack stack, ResourceLocation key) {
        return Rangs.REGISTRY.get(key);
    }

    @NotNull
    @Override
    public VaultGearClassification getClassification(ItemStack itemStack) {
        return VaultGearClassification.SWORD;
    }

    @NotNull
    @Override
    @SuppressWarnings({"deprecation","removal"})
    public ProficiencyType getCraftingProficiencyType(ItemStack itemStack) {
        return ProficiencyType.SWORD;
    }

    @NotNull
    @Override
    public VaultGearType getGearType(ItemStack itemStack) {
        return VaultGearType.SWORD;
    }

    @Nullable
    @Override
    public ResourceLocation getRandomModel(ItemStack stack, Random random) {
        VaultGearData gearData = VaultGearData.read(stack);
        EquipmentSlot intendedSlot = this.getGearType(stack).getEquipmentSlot();
        return ModConfigs.GEAR_MODEL_ROLL_RARITIES.getRandomRoll(stack, gearData, intendedSlot, random);
    }


    @Override
    public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
        return super.getDefaultTooltipHideFlags(stack) | ItemStack.TooltipPart.MODIFIERS.getMask();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return VaultGearHelper.getModifiers(stack, slot);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, itemSlot, isSelected);
        if (entity instanceof ServerPlayer player) {
            this.vaultGearTick(stack, player);
        }

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.addAll(this.createTooltip(stack, GearTooltip.itemTooltip()));
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            items.add(this.defaultItem());
        }

    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return VaultGearData.read(stack).getState() == VaultGearState.IDENTIFIED;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return VaultGearData.read(stack).get(ModGearAttributes.DURABILITY, VaultGearAttributeTypeMerger.intSum());
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(enchantment.equals(Enchantments.UNBREAKING) || enchantment.equals(Enchantments.MOB_LOOTING) || enchantment.equals(EnsorcEnchantments.SOULBOUND.get())) {
            return true;
        }
        if(AllowedEnchantmentsData.isAllowedUtilityEnchantment(enchantment)) {
            return true;
        }
        else {
            return false;
        }
    }
}
