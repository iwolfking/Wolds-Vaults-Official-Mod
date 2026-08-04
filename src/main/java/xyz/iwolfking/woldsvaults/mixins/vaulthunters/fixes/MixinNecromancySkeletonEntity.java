package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.VaultMod;
import iskallia.vault.core.random.ChunkRandom;
import iskallia.vault.entity.entity.necromancy.NecromancySkeletonEntity;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.LootInitialization;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.Random;

@Mixin(value = NecromancySkeletonEntity.class, remap = false)
public class MixinNecromancySkeletonEntity {
    /**
     * @author iwolfking
     * @reason Test!
     */
    @Overwrite
    private static ItemStack createCosmeticVaultGear(Item item, int vaultLevel) {
        ItemStack stack = new ItemStack(item);
        stack = LootInitialization.initializeVaultLoot(stack, vaultLevel);

        VaultGearData data = VaultGearData.read(stack);
        data.setState(VaultGearState.IDENTIFIED);
        data.setRarity(VaultGearRarity.COMMON);
        if(stack.getItem() instanceof VaultGearItem vaultGearItem) {
            ResourceLocation modelId = vaultGearItem.getRandomModel(stack, new Random(), null, null);
            if (modelId != null) {
                data.createOrReplaceAttributeValue(ModGearAttributes.GEAR_MODEL, modelId);
            }
        }


        data.write(stack);

        return stack;
    }
}
