package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.GearDataCache;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.gear.VaultSwordItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = VaultSwordItem.class, remap = false)
public abstract class MixinVaultSwordItem extends SwordItem implements VaultGearItem, DyeableLeatherItem {
    public MixinVaultSwordItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    /**
     * @author iwolfking
     * @reason Vault Swords act like normal swords for mining.
     */
    @Overwrite
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if(GearDataCache.of(stack).getState().equals(VaultGearState.IDENTIFIED)) {
            return super.isCorrectToolForDrops(stack, state);
        }

        return false;
    }

    /**
     * @author iwolfking
     * @reason Vault Swords act like normal swords for mining.
     */
    @Overwrite
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if(GearDataCache.of(stack).getState().equals(VaultGearState.IDENTIFIED)) {
            return super.getDestroySpeed(stack, state);
        }

        return 1.0F;
    }
}
