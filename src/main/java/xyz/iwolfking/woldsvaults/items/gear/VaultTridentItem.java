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
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.MiscUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.LoadingModList;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.compat.bettertridents.BetterThrownTrident;
import xyz.iwolfking.woldsvaults.data.enchantments.AllowedEnchantmentsData;
import xyz.iwolfking.woldsvaults.models.Tridents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class VaultTridentItem extends TridentItem implements VaultGearItem, DyeableLeatherItem {
    public VaultTridentItem(ResourceLocation id, Properties builder) {
        super(builder);
        setRegistryName(id);
    }


    @Nullable
    public ResourceLocation getRandomModel(ItemStack stack, Random random) {
        VaultGearData gearData = VaultGearData.read(stack);
        VaultGearRarity rarity = gearData.getRarity();
        EquipmentSlot intendedSlot = getIntendedSlot(stack);
        ResourceLocation possibleIds =
            ModConfigs.GEAR_MODEL_ROLL_RARITIES.getRandomRoll(this.defaultItem(), gearData, intendedSlot, random);

        return MiscUtils.getRandomEntry(possibleIds);
    }

    @Override
    public Optional<? extends DynamicModel<?>> resolveDynamicModel(ItemStack stack, ResourceLocation key) {
        return Tridents.REGISTRY.get(key);
    }


    @Nullable
    public EquipmentSlot getIntendedSlot(ItemStack stack) {
        return EquipmentSlot.MAINHAND;
    }


    @NotNull
    public VaultGearClassification getClassification(ItemStack stack) {
        return VaultGearClassification.AXE;
    }


    @Nonnull @SuppressWarnings({"deprecation","removal"})
    public ProficiencyType getCraftingProficiencyType(ItemStack stack) {
        return ProficiencyType.AXE;
    }

    @NotNull
    @Override
    public VaultGearType getGearType(ItemStack itemStack) {
        return VaultGearType.AXE;
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
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment.equals(Enchantments.LOYALTY) || enchantment.equals(Enchantments.RIPTIDE) ||
            enchantment.equals(Enchantments.CHANNELING)) {
            return false;
        } else if (enchantment.equals(Enchantments.MOB_LOOTING) || AllowedEnchantmentsData.isAllowedUtilityEnchantment(enchantment)) {
            return true;
        } else {
            return super.canApplyAtEnchantingTable(stack, enchantment);
        }
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
    public boolean isFireResistant() {
        return true;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return (VaultGearData.read(stack).getState() == VaultGearState.IDENTIFIED);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return VaultGearData.read(stack)
            .get(ModGearAttributes.DURABILITY, VaultGearAttributeTypeMerger.intSum());
    }

    @Override
    public Component getName(ItemStack stack) {
        return VaultGearHelper.getDisplayName(stack, super.getName(stack));
    }


    @Override
    public int getUseDuration(ItemStack stack) {
        //VaultGearData data = VaultGearData.read(stack);
        //float percentDecrease = data.get(xyz.iwolfking.woldsvaults.init.ModGearAttributes.TRIDENT_WINDUP, VaultGearAttributeTypeMerger.floatSum());
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int duration) {
        if (entity instanceof Player player) {
            VaultGearData data = VaultGearData.read(stack);
            float percentDecrease = data.get(xyz.iwolfking.woldsvaults.init.ModGearAttributes.TRIDENT_WINDUP, VaultGearAttributeTypeMerger.floatSum());
            int i = this.getUseDuration(stack) - duration;
            if (i >= (10 * (1 -percentDecrease))) {
                int j = data.get(xyz.iwolfking.woldsvaults.init.ModGearAttributes.TRIDENT_RIPTIDE, VaultGearAttributeTypeMerger.intSum());
                if (j <= 0 || player.isInWaterOrRain()) {
                    if (!level.isClientSide) {
                        stack.hurtAndBreak(1, player, (p_43388_) -> {
                            p_43388_.broadcastBreakEvent(entity.getUsedItemHand());
                        });
                        if (j == 0) {
                            ThrownTrident throwntrident;
                            if(LoadingModList.get().getModFileById("bettertridents") != null)
                                throwntrident = new BetterThrownTrident(level, player, stack);
                            else
                                throwntrident = new ThrownTrident(level, player, stack);
                            throwntrident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F + j * 0.5F, 1.0F);
                            if (player.getAbilities().instabuild) {
                                throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }

                            level.addFreshEntity(throwntrident);
                            level.playSound(null, throwntrident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
                            if (!player.getAbilities().instabuild) {
                                player.getInventory().removeItem(stack);
                            }
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    if (j > 0) {
                        float f7 = player.getYRot();
                        float f = player.getXRot();
                        float f1 = -Mth.sin(f7 * ((float)Math.PI / 180F)) * Mth.cos(f * ((float)Math.PI / 180F));
                        float f2 = -Mth.sin(f * ((float)Math.PI / 180F));
                        float f3 = Mth.cos(f7 * ((float)Math.PI / 180F)) * Mth.cos(f * ((float)Math.PI / 180F));
                        float f4 = Mth.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
                        float f5 = 3.0F * ((1.0F + j) / 4.0F);
                        f1 *= f5 / f4;
                        f2 *= f5 / f4;
                        f3 *= f5 / f4;
                        player.push(f1, f2, f3);
                        player.startAutoSpinAttack(20);
                        if (player.isOnGround()) {
                            float f6 = 1.1999999F;
                            player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999F, 0.0D));
                        }

                        SoundEvent soundevent;
                        if (j >= 3) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
                        } else if (j == 2) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
                        } else {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
                        }

                        level.playSound(null, player, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }

                }
            }
        }
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


    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.addAll(createTooltip(stack, GearTooltip.itemTooltip()));
    }

}


