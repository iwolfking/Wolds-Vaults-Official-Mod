package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.VaultMod;
import iskallia.vault.config.recipe.ForgeRecipeType;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.crafting.recipe.JewelCraftingRecipe;
import iskallia.vault.gear.crafting.recipe.VaultForgeRecipe;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.LootInitialization;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.data.discovery.ClientRecipeDiscoveryData;
import xyz.iwolfking.woldsvaults.data.discovery.DiscoveredRecipesData;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import java.util.List;
import java.util.Set;

@Mixin(value = JewelCraftingRecipe.class, remap = false)
public abstract class MixinJewelCraftingRecipe extends VaultForgeRecipe {

    @Unique
    private static final Set<ResourceLocation> CRAFTABLE_UNIQUE_JEWELS = Set.of(WoldsVaults.id("treasure_jewel"), WoldsVaults.id("shattering_jewel"));

    protected MixinJewelCraftingRecipe(ForgeRecipeType type, ResourceLocation id, ItemStack output) {
        super(type, id, output);
    }

    @Override
    public boolean canCraft(Player player) {
        if(ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.containsKey(this.getId())) {
            if (player instanceof ServerPlayer sPlayer) {
                return player.isCreative() || DiscoveredRecipesData.get(sPlayer.server).hasDiscovered(player, this.getId());
            }

            return player.isCreative() || ClientRecipeDiscoveryData.getDiscoveredRecipes().contains(this.getId());
        }

        return true;
    }

    @Redirect(method = "createAttributeJewel", at = @At(value = "INVOKE", target = "Liskallia/vault/gear/data/VaultGearData;addModifier(Liskallia/vault/gear/attribute/VaultGearModifier$AffixType;Liskallia/vault/gear/attribute/VaultGearModifier;)Z", ordinal = 1))
    private static boolean createAttributeJewel(VaultGearData instance, VaultGearModifier.AffixType type, VaultGearModifier<?> modifier, @Local VaultGearAttribute<?> attribute) {
        if (attribute != null) {
            if (attribute.getRegistryName().equals(VaultMod.id("immortality"))) {
                return instance.addModifier(VaultGearModifier.AffixType.SUFFIX, new VaultGearModifier(attribute, 0.2F));
            }
        }
        return instance.addModifier(VaultGearModifier.AffixType.SUFFIX, new VaultGearModifier(attribute, true));
    }

    @Inject(method = "createJewel", at = @At("HEAD"), cancellable = true)
    private void createUniqueJewel(int vaultLevel, CallbackInfoReturnable<ItemStack> cir) {
        if(CRAFTABLE_UNIQUE_JEWELS.contains(this.getId())) {
            cir.setReturnValue(woldsVaults$createUniqueJewel(vaultLevel));
        }
    }

    @Inject(method = "getDisplayOutput", at = @At("HEAD"), cancellable = true)
    private void displayUniqueJewel(int vaultLevel, CallbackInfoReturnable<ItemStack> cir) {
        if(CRAFTABLE_UNIQUE_JEWELS.contains(this.getId())) {
            cir.setReturnValue(woldsVaults$createUniqueJewel(vaultLevel));
        }
    }

    @Unique
    private ItemStack woldsVaults$createUniqueJewel(int vaultLevel) {
        ItemStack stack = this.getRawOutput();
        VaultGearData data = VaultGearData.read(stack);
        data.setItemLevel(vaultLevel);
        data.setRarity(VaultGearRarity.UNIQUE);
        data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_ROLL_TYPE, "Unique");
        ResourceLocation pool = iskallia.vault.init.ModConfigs.UNIQUE_GEAR.findPoolForUnique(getId()).orElse(null);
        if(pool != null) {
            data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_UNIQUE_POOL, pool);
            data.write(stack);
            stack = LootInitialization.initializeVaultLoot(stack, vaultLevel);
            if(stack.getItem() instanceof IdentifiableItem identifiableItem) {
                identifiableItem.instantIdentify(null, stack);
            }
        }
        return stack;
    }

    @Override
    public List<Component> getDisabledText() {
        if(ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.containsKey(this.getId())) {
            return List.of(new TextComponent(ModConfigs.RECIPE_UNLOCKS.RECIPE_UNLOCKS.get(this.getId()).DESCRIPTION));
        }
        else if(this.getId().equals(VaultMod.id("random"))) {
            return List.of(new TextComponent("Random Jewel Crafting - Unlocked by the Jeweler Expertise"));
        }
        return super.getDisabledText();
    }

}
