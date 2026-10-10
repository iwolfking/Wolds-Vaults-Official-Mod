package xyz.iwolfking.woldsvaults.items.gear;

import com.google.common.collect.Multimap;
import iskallia.vault.dynamodel.DynamicModel;
import iskallia.vault.entity.entity.PetEntity;
import iskallia.vault.gear.VaultGearClassification;
import iskallia.vault.gear.VaultGearHelper;
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
import iskallia.vault.util.calc.AbilityPowerHelper;
import iskallia.vault.util.calc.AreaOfEffectHelper;
import iskallia.vault.world.data.DiscoveredModelsData;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.NotNull;
import xyz.iwolfking.woldsvaults.models.Scepters;
import xyz.iwolfking.woldsvaults.modifiers.gear.scepter.ScepterInvokeAttribute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class VaultScepterItem extends SwordItem implements VaultGearItem, DyeableLeatherItem {

    public VaultScepterItem(ResourceLocation id, Properties builder) {
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
        return Scepters.REGISTRY.get(key);
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
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        VaultGearData gearData = VaultGearData.read(stack);
        if (gearData.getState() == VaultGearState.IDENTIFIED) {
            if (gearData.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SCEPTER_BEAM) ||
                    gearData.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SCEPTER_INVOKE)) {
                return UseAnim.BOW;
            }
        }
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        VaultGearData gearData = VaultGearData.read(stack);

        if (gearData.getState() == VaultGearState.IDENTIFIED) {
            boolean hasBeam = gearData.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SCEPTER_BEAM);
            boolean hasInvoke = gearData.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SCEPTER_INVOKE);

            if (hasBeam || hasInvoke) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(stack);
            }
        }

        return VaultGearHelper.rightClick(world, player, hand, super.use(world, player, hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;

        int chargeTicks = getUseDuration(stack) - timeLeft;
        VaultGearData gearData = VaultGearData.read(stack);

        if (gearData.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SCEPTER_INVOKE)) {
            xyz.iwolfking.woldsvaults.modifiers.gear.scepter.ScepterInvokeAttribute invokeAttr = gearData.get(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SCEPTER_INVOKE, VaultGearAttributeTypeMerger.firstNonNull());
            if (invokeAttr != null && chargeTicks >= invokeAttr.getChargeTicks()) {
                invokeAttr.trigger(player);
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.2F);
                return;
            }
        }

        if (gearData.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SCEPTER_BEAM)) {
            if (chargeTicks < 10) return;
            executeBeamAttack(serverLevel, player);
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity entity, int count) {
        if (!(entity instanceof ServerPlayer player)) return;

        int chargeTicks = getUseDuration(stack) - count;
        VaultGearData gearData = VaultGearData.read(stack);
        if (gearData.getState() != VaultGearState.IDENTIFIED) return;

        float targetTicks = 20.0F;
        boolean hasValidCharge = false;

        if (gearData.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SCEPTER_INVOKE)) {
            ScepterInvokeAttribute invokeAttr = gearData.get(
                    xyz.iwolfking.woldsvaults.init.ModGearAttributes.SCEPTER_INVOKE,
                    VaultGearAttributeTypeMerger.firstNonNull()
            );
            if (invokeAttr != null) {
                targetTicks = invokeAttr.getChargeTicks();
                hasValidCharge = true;
            }
        } else if (gearData.hasAttribute(xyz.iwolfking.woldsvaults.init.ModGearAttributes.SCEPTER_BEAM)) {
            hasValidCharge = true;
        }

        if (!hasValidCharge) return;

        Level level = player.level;
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 look = player.getLookAngle();
        Vec3 tipPos = player.getEyePosition().add(look.x * 0.7, -0.2, look.z * 0.7);

        if (chargeTicks < targetTicks) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    tipPos.x, tipPos.y, tipPos.z,
                    2, 0.1, 0.1, 0.1, 0.05);
        }
        else if (chargeTicks == (int) targetTicks) {
            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING, SoundSource.PLAYERS, 0.8F, 2.0F);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    tipPos.x, tipPos.y, tipPos.z,
                    8, 0.1, 0.1, 0.1, 0.08);
        }
        else if (chargeTicks % 10 == 0) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    tipPos.x, tipPos.y, tipPos.z,
                    1, 0.05, 0.05, 0.05, 0.01);
        }
    }

    private void executeBeamAttack(ServerLevel level, ServerPlayer player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        float aoeBonus = AreaOfEffectHelper.getAreaOfEffectUnlimited(player);
        double range = 16.0 * (1.0F + aoeBonus);
        double beamRadius = 1.0 * (1.0F + (0.5F * aoeBonus));

        Vec3 end = start.add(look.scale(range));

        AABB beamBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(beamRadius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, beamBox,
                e -> !(e instanceof Player) && e.isAlive() && !e.isAlliedTo(player) && !(e instanceof PetEntity)
        );
        float playerAP = AbilityPowerHelper.getAbilityPower(player);

        for (LivingEntity target : targets) {
            AABB targetBox = target.getBoundingBox().inflate(beamRadius);
            if (targetBox.clip(start, end).isPresent()) {
                target.hurt(DamageSource.indirectMagic(player, player), playerAP * 0.65F);
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.7F, 1.8F);

        double step = 0.4;
        for (double d = 0; d < range; d += step) {
            Vec3 point = start.add(look.scale(d));
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.01);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 2, 0.05, 0.05, 0.05, 0.02);
        }
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
        return false;
    }
}