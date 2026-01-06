package xyz.iwolfking.woldsvaults.items.gear;

import com.google.common.collect.Multimap;
import iskallia.vault.VaultMod;
import iskallia.vault.config.VaultCrystalConfig;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.modifier.GroupedModifier;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.gear.VaultGearClassification;
import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.crafting.ProficiencyType;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.BasicItem;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.objective.CrystalObjective;
import iskallia.vault.item.crystal.theme.CrystalTheme;
import iskallia.vault.item.crystal.theme.PoolCrystalTheme;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import iskallia.vault.item.data.InscriptionData;
import iskallia.vault.item.tool.JewelItem;
import iskallia.vault.recipe.anvil.AnvilContext;
import iskallia.vault.world.data.DiscoveredModelsData;
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
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.SettableValueVaultModifier;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.GreedyVaultModifier;
import xyz.iwolfking.woldsvaults.modifiers.vault.map.modifiers.InscriptionCrystalModifierSettable;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class VaultMapItem extends BasicItem implements VaultGearItem, IVaultCrystalModifier {

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
    @SuppressWarnings({"removal", "deprecation"})
    public ProficiencyType getCraftingProficiencyType(ItemStack itemStack) {
        return ProficiencyType.UNKNOWN;
    }

    @NotNull
    @Override
    public VaultGearType getGearType(ItemStack itemStack) {
        return VaultGearType.IDOL;
    }

    @javax.annotation.Nullable
    public ResourceLocation getRandomModel(ItemStack stack, Random random, @javax.annotation.Nullable Player player, @javax.annotation.Nullable DiscoveredModelsData discoveredModelsData) {
        VaultGearData gearData = VaultGearData.read(stack);
        EquipmentSlot intendedSlot = this.getGearType(stack).getEquipmentSlot();
        return ModConfigs.GEAR_MODEL_ROLL_RARITIES.getRandomRoll(stack, gearData, intendedSlot, random, player, discoveredModelsData);
    }

    @Override
    public void tickRoll(ItemStack stack, @Nullable Player player) {
        VaultGearData data = VaultGearData.read(stack);
        int tier;
        if (data.getState() != VaultGearState.IDENTIFIED) {
            tier = data.getFirstValue(ModGearAttributes.MAP_TIER).orElse(-1);
            if (tier == -1) {
                float randChance = rand.nextFloat();
                if (randChance <= 0.05F) {
                    data.createOrReplaceAttributeValue(ModGearAttributes.MAP_TIER, 5);
                } else if (randChance <= 0.15F) {
                    data.createOrReplaceAttributeValue(ModGearAttributes.MAP_TIER, 4);
                } else if (randChance <= 0.35F) {
                    data.createOrReplaceAttributeValue(ModGearAttributes.MAP_TIER, 3);
                } else if (randChance <= 0.6F) {
                    data.createOrReplaceAttributeValue(ModGearAttributes.MAP_TIER, 2);
                } else if (randChance <= 0.75F) {
                    data.createOrReplaceAttributeValue(ModGearAttributes.MAP_TIER, 1);
                } else {
                    data.createOrReplaceAttributeValue(ModGearAttributes.MAP_TIER, 0);
                }
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
    public void addSlotTooltip(List<Component> tooltip, EquipmentSlot slot) {
    }

    @Override
    public void addTooltipItemLevel(VaultGearData data, ItemStack stack, List<Component> tooltip, VaultGearState state) {
        if (data.hasAttribute(ModGearAttributes.MAP_TIER)) {
            int tier = data.getFirstValue(ModGearAttributes.MAP_TIER).orElse(0);
            tooltip.add(new TextComponent("Tier: ").append(new TextComponent(String.valueOf(tier)).withStyle(Style.EMPTY.withColor(getTierColor(tier)))));
        } else if (stack.getOrCreateTag().contains("the_vault:map_tier")) {
            int tier = stack.getOrCreateTag().getInt("the_vault:map_tier");
            if (tier == -1) {
                tooltip.add(new TextComponent("Tier: ").append(new TextComponent("???").withStyle(Style.EMPTY.withColor(7247291))));
            } else {
                tooltip.add(new TextComponent("Tier: ").append(new TextComponent(String.valueOf(tier)).withStyle(Style.EMPTY.withColor(getTierColor(tier)))));
            }
        } else {
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

    public boolean applyCrystalRecipe(AnvilContext context, CrystalData data, ItemStack ingredientStack, ItemStack output) {
        boolean hasGreedy = false;

        for (VaultModifierStack modifierStack : data.getModifiers()) {
            if (modifierStack.getModifier() instanceof GroupedModifier groupedModifier) {
                for (VaultModifier<?> childMod : groupedModifier.properties().getChildren()) {
                    if (childMod instanceof GreedyVaultModifier) {
                        hasGreedy = true;
                        break;
                    }
                }
            } else if (modifierStack.getModifier() instanceof GreedyVaultModifier) {
                hasGreedy = true;
                break;
            }
        }

        if (data.getProperties().getLevel().isPresent() && data.getProperties().getLevel().get() < 100) {
            return false;
        }

        if (!hasGreedy) {
            return false;
        }

        VaultGearData mapData = VaultGearData.read(ingredientStack);

        Optional<Integer> prefixSlots = mapData.getFirstValue(iskallia.vault.init.ModGearAttributes.PREFIXES);
        Optional<Integer> suffixSlots = mapData.getFirstValue(iskallia.vault.init.ModGearAttributes.SUFFIXES);

        if (prefixSlots.isEmpty() || suffixSlots.isEmpty()) {
            return false;
        }

        int numberOfPrefixes = mapData.getModifiers(VaultGearModifier.AffixType.PREFIX).size();
        int numberOfSuffixes = mapData.getModifiers(VaultGearModifier.AffixType.SUFFIX).size();

        boolean unfinishedMap = false;

        if (prefixSlots.get() != numberOfPrefixes || suffixSlots.get() != numberOfSuffixes) {
            unfinishedMap = true;
        }

        String themeId = mapData.getFirstValue(ModGearAttributes.THEME).orElse(null);
        String themePoolId = mapData.getFirstValue(ModGearAttributes.THEME_POOL).orElse(null);
        String objectiveId = mapData.getFirstValue(ModGearAttributes.OBJECTIVE).orElse(null);

        if (themeId != null) {
            CrystalTheme theme = new ValueCrystalTheme(ResourceLocation.parse(themeId));
            data.setTheme(theme);
        } else if (themePoolId != null) {
            CrystalTheme theme = new PoolCrystalTheme(ResourceLocation.parse(themePoolId));
            data.setTheme(theme);
        } else {
            return false;
        }

        if (objectiveId != null) {
            if (CrystalData.OBJECTIVE.getValue(objectiveId) != null) {
                CrystalObjective objective = CrystalData.OBJECTIVE.getValue(objectiveId);
                if (ModConfigs.VAULT_CRYSTAL.OBJECTIVES.containsKey(VaultMod.id(objectiveId))) {
                    VaultCrystalConfig.ObjectiveEntry entry = ModConfigs.VAULT_CRYSTAL.OBJECTIVES.get(VaultMod.id(objectiveId)).getForLevel(data.getProperties().getLevel().orElse(0)).orElse(null);
                    if (entry != null) {
                        CrystalObjective obj = entry.pool.getRandom().orElse(null);
                        if (obj != null) {
                            objective = obj;
                        }
                    }
                }
                data.setObjective(objective);

            }
        } else {
            return false;
        }

        applySpecialModifiers(data, mapData, VaultGearModifier.AffixType.PREFIX, context, output, unfinishedMap);
        applySpecialModifiers(data, mapData, VaultGearModifier.AffixType.SUFFIX, context, output, unfinishedMap);
        applySpecialModifiers(data, mapData, VaultGearModifier.AffixType.IMPLICIT, context, output, unfinishedMap);


        data.getProperties().setUnmodifiable(true);
        data.write(output);
        context.setOutput(output);

        context.onTake(context.getTake().append(() -> {
            context.getInput()[0].shrink(1);
            context.getInput()[1].shrink(1);
        }));
        return true;
    }

    public static boolean applySpecialModifiers(CrystalData data, VaultGearData mapData, VaultGearModifier.AffixType affixType, AnvilContext context, ItemStack output, boolean shouldReduceValues) {
        for (VaultGearModifier<?> mod : mapData.getModifiers(affixType)) {
            VaultModifier<?> vaultMod = VaultModifierRegistry.get(mod.getModifierIdentifier());
            if (vaultMod instanceof SettableValueVaultModifier<?> settableValueVaultModifier) {
                float value;
                if (mod.getValue() instanceof Integer integerValue) {
                    value = Float.valueOf(integerValue);
                } else {
                    value = (float) mod.getValue();
                }

                if (shouldReduceValues) {
                    value *= 0.25F;
                }
                settableValueVaultModifier.properties().setValue(value);

                if (vaultMod instanceof InscriptionCrystalModifierSettable inscriptionCrystalModifierSettable) {
                    InscriptionData inscriptionData = inscriptionCrystalModifierSettable.properties().getData();
                    inscriptionData.apply(context.getPlayer().orElse(null), output, data);
                } else {
                    VaultModifierStack stack = new VaultModifierStack(settableValueVaultModifier, 1);
                    data.getModifiers().add(stack);
                }

            } else if (vaultMod != null) {
                VaultModifierStack stack = new VaultModifierStack(vaultMod, 1);
                data.getModifiers().add(stack);
            }
        }
        return true;
    }
}