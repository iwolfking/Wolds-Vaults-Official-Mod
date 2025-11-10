package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.block.entity.ModifierWorkbenchTileEntity;
import iskallia.vault.config.gear.VaultGearWorkbenchConfig;
import iskallia.vault.gear.VaultGearModifierHelper;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.crafting.ModifierWorkbenchHelper;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.util.InventoryUtil;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.woldsvaults.expertises.CraftsmanExpertise;
import xyz.iwolfking.woldsvaults.api.util.ModifierWorkbenchMixinHelper;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.MixinModifierWorkbenchCraftMessageAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(value = iskallia.vault.network.message.ModifierWorkbenchCraftMessage.class, remap = false)
public class ModifierWorkbenchCraftMessage {
    /**
     * @author iwolfking
     * @reason Add Craftsman Expertise to Modifier Workbench
     */
    @Overwrite
    public static void handle(iskallia.vault.network.message.ModifierWorkbenchCraftMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ExpertiseTree expertises = PlayerExpertisesData.get(player.getLevel()).getExpertises(player);
            int craftsmanLevel = 0;

            for (CraftsmanExpertise craftsmanExpertise : expertises.getAll(CraftsmanExpertise.class, Skill::isUnlocked)) {
                craftsmanLevel = craftsmanExpertise.getCraftsmanLevel();
            }
            int numberOfAllowedModifiers = craftsmanLevel == 0 ? 1 : craftsmanLevel;
            BlockPos pos = ((MixinModifierWorkbenchCraftMessageAccessor)message).getPos();
            BlockEntity tile = player.getLevel().getBlockEntity(pos);
            if (tile instanceof ModifierWorkbenchTileEntity workbenchTile) {
                ItemStack input = workbenchTile.getInventory().getItem(0);
                if (!input.isEmpty() && input.getItem() instanceof VaultGearItem && AttributeGearData.hasData(input)) {
                    if (VaultGearData.read(input).isModifiable()) {
                        int craftsmanLevelCopy = craftsmanLevel;
                        VaultGearWorkbenchConfig.getConfig(input.getItem()).ifPresent((cfg) -> {
                            ItemStack inputCopy = input.copy();
                            VaultGearModifier.AffixType targetAffix = null;
                            VaultGearModifier<?> createdModifier = null;
                            List<ItemStack> cost = new ArrayList<>();
                            if (((MixinModifierWorkbenchCraftMessageAccessor)message).getCraftModifierIdentifier() == null) {
                                if (!ModifierWorkbenchHelper.hasCraftedModifier(inputCopy)) {
                                    return;
                                }

                                cost.addAll(cfg.getCostRemoveCraftedModifiers());


                            } else {
                                VaultGearWorkbenchConfig.CraftableModifierConfig modifierConfig = cfg.getConfig(((MixinModifierWorkbenchCraftMessageAccessor)message).getCraftModifierIdentifier());
                                if (modifierConfig == null) {
                                    return;
                                }

                                if (!modifierConfig.hasPrerequisites(player)) {
                                    return;
                                }

                                VaultGearData data = VaultGearData.read(inputCopy);
                                boolean hadCraftedModifiers = ModifierWorkbenchHelper.hasCraftedModifier(inputCopy);
                                boolean isCrafted = data.getFirstValue(ModGearAttributes.CRAFTED_BY).isPresent();

                                if(ModifierWorkbenchMixinHelper.getCraftedModifierCount(inputCopy) > numberOfAllowedModifiers && isCrafted) {
                                    ModifierWorkbenchHelper.removeCraftedModifiers(inputCopy);
                                }
                                else if(!isCrafted) {
                                    ModifierWorkbenchHelper.removeCraftedModifiers(inputCopy);
                                }

                                if (data.getItemLevel() < modifierConfig.getMinLevel()) {
                                    return;
                                }

                                targetAffix = modifierConfig.getAffixGroup().getTargetAffixType();
                                if (targetAffix == VaultGearModifier.AffixType.PREFIX) {
                                    if (!VaultGearModifierHelper.hasOpenPrefix(inputCopy)) {
                                        return;
                                    }
                                } else if (!VaultGearModifierHelper.hasOpenSuffix(inputCopy)) {
                                    return;
                                }

                                createdModifier = modifierConfig.createModifier().orElse(null);
                                if (createdModifier == null) {
                                    return;
                                }

                                Set<String> existingModGroups = data.getExistingModifierGroups(VaultGearData.Type.EXPLICIT_MODIFIERS);
                                if (existingModGroups.contains(createdModifier.getModifierGroup())) {
                                    return;
                                }

                                cost.addAll(modifierConfig.createCraftingCost(inputCopy));
                                if (hadCraftedModifiers && (ModifierWorkbenchMixinHelper.getCraftedModifierCount(input) > numberOfAllowedModifiers && isCrafted)) {
                                    cost.addAll(cfg.getCostRemoveCraftedModifiers());
                                }
                                else if(hadCraftedModifiers && !isCrafted) {
                                    cost.addAll(cfg.getCostRemoveCraftedModifiers());
                                }
                            }

                            List<ItemStack> missing = InventoryUtil.getMissingInputs(cost, player.getInventory());
                            if (missing.isEmpty()) {
                                if (InventoryUtil.consumeInputs(cost, player.getInventory(), true)) {
                                    if (InventoryUtil.consumeInputs(cost, player.getInventory(), false)) {
                                        boolean isCrafted = VaultGearData.read(input).getFirstValue(ModGearAttributes.CRAFTED_BY).isPresent();
                                        if (createdModifier == null) {
                                                ModifierWorkbenchHelper.removeCraftedModifiers(input);
                                        } else {
                                            createdModifier.addCategory(VaultGearModifier.AffixCategory.CRAFTED);
                                            createdModifier.setGameTimeAdded(player.getLevel().getGameTime());
                                            if(ModifierWorkbenchMixinHelper.getCraftedModifierCount(input) > numberOfAllowedModifiers && isCrafted) {
                                                ModifierWorkbenchHelper.removeCraftedModifiers(input);
                                            }
                                            else if(!isCrafted) {
                                                ModifierWorkbenchHelper.removeCraftedModifiers(input);
                                            }
                                            VaultGearData datax = VaultGearData.read(input);
                                            datax.addModifier(targetAffix, createdModifier);
                                            datax.write(input);
                                        }

                                        player.getLevel().levelEvent(1030, tile.getBlockPos(), 0);
                                    }

                                }
                            }
                        });
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
