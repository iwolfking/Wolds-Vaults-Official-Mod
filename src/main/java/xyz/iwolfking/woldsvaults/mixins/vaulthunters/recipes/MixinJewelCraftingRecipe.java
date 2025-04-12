package xyz.iwolfking.woldsvaults.mixins.vaulthunters.recipes;

import iskallia.vault.VaultMod;
import iskallia.vault.client.ClientExpertiseData;
import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.crafting.recipe.JewelCraftingRecipe;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.tool.JewelItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.expertise.type.JewelExpertise;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = JewelCraftingRecipe.class, remap = false)
public abstract class MixinJewelCraftingRecipe extends VaultForgeRecipe {
    @Shadow private ResourceLocation jewelAttribute;
    @Shadow private int size;

    protected MixinJewelCraftingRecipe(ForgeRecipeType type, ResourceLocation id, ItemStack output) {
        super(type, id, output);
    }

    @Override
    public boolean canCraft(Player player) {
        if(this.getId().equals(VaultMod.id("true_random"))) {
            TieredSkill expertise = ClientExpertiseData.getLearnedTalentNode("Jeweler");
            return expertise != null && expertise.isUnlocked();
        }

        return true;
    }

    @Inject(method = "getDisplayOutput", at = @At("HEAD"), cancellable = true)
    public void getDisplayOutput(int vaultLevel, CallbackInfoReturnable<ItemStack> cir) {
        if (this.getId().equals(VaultMod.id("random"))) {
            ItemStack jewel = JewelItem.create((vaultGearData) -> {
                vaultGearData.setRarity(VaultGearRarity.RARE);
                vaultGearData.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier(ModGearAttributes.JEWEL_SIZE, this.size));
                vaultGearData.setState(VaultGearState.UNIDENTIFIED);
            });
            jewel.getOrCreateTag().putBoolean("ignoreJewelSize", true);
            cir.setReturnValue(jewel);
        }
    }

    /**
     * @author iwolfking
     * @reason Add special text to random jewel crafting
     */
    @Inject(method = "addCraftingDisplayTooltip", at = @At("HEAD"), cancellable = true)
    public void addCraftingDisplayTooltip(ItemStack result, List<Component> out, CallbackInfo ci) {
        if(this.getId().equals(VaultMod.id("true_random"))) {
            String name = "Random Modifier";
            out.add((new TextComponent(name)).withStyle(ChatFormatting.GOLD));
            ci.cancel();
        }
    }
}
