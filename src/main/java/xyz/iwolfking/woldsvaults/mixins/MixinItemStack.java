package xyz.iwolfking.woldsvaults.mixins;

import iskallia.vault.config.DurabilityConfig;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.IConditionalDamageable;
import iskallia.vault.item.MagnetItem;
import iskallia.vault.item.gear.*;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.prestige.GearDurabilityPrestigePower;
import iskallia.vault.skill.prestige.helper.PrestigeHelper;
import iskallia.vault.util.calc.DurabilityWearReductionHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.iwolfking.woldsvaults.items.gear.VaultBattleStaffItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultLootSackItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultPlushieItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultTridentItem;

import javax.annotation.Nullable;
import java.util.Random;
//TODO: Replace with MixinSquared?
@Mixin(value = ItemStack.class)
public abstract class MixinItemStack extends net.minecraftforge.common.capabilities.CapabilityProvider<ItemStack> implements net.minecraftforge.common.extensions.IForgeItemStack  {
    protected MixinItemStack(Class<ItemStack> baseClass) {
        super(baseClass);
    }

    @Shadow public abstract boolean isDamageableItem();

    @Shadow @Final @Deprecated private Item item;

    @Shadow public abstract Item getItem();

    @Shadow public abstract int getDamageValue();

    @Shadow public abstract void setDamageValue(int pDamage);

    @Shadow public abstract int getMaxDamage();

    /**
     * @author iwolfking
     * @reason Dirty terrible things
     */
    @Overwrite
    public boolean hurt(int damage, Random rand, @Nullable ServerPlayer damager) {
        if (!this.isDamageableItem()) {
            return false;
        } else if (this.item == Items.ELYTRA && (new Random()).nextInt(5) != 0) {
            return false;
        } else {
            Item var5 = this.getItem();
            if (var5 instanceof VaultGearItem gearItem) {
                if (gearItem.isBroken((ItemStack)(Object)this)) {
                    return false;
                }
            }

            if (damage > 0) {
                var5 = this.getItem();
                if (var5 instanceof IConditionalDamageable) {
                    IConditionalDamageable cd = (IConditionalDamageable)var5;
                    if (cd.isImmuneToDamage((ItemStack)(Object)this, damager)) {
                        return false;
                    }
                }

                int unbreaking = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, (ItemStack)(Object)this);
                int durabilityNegation = 0;
                boolean isArmor = ((ItemStack)(Object)this).getItem() instanceof ArmorItem;
                DurabilityConfig cfg = ModConfigs.DURABILITY;
                float chance = isArmor ? cfg.getArmorDurabilityIgnoreChance(unbreaking) : cfg.getDurabilityIgnoreChance(unbreaking);

                for(int k = 0; unbreaking > 0 && k < damage; ++k) {
                    if (rand.nextFloat() < chance) {
                        ++durabilityNegation;
                    }
                }

                int wearReduction = 0;
                boolean isDmgReducible = true;
                Item var12 = this.getItem();
                if (var12 instanceof IConditionalDamageable) {
                    IConditionalDamageable cd = (IConditionalDamageable)var12;
                    isDmgReducible = cd.doesDurabilityReductionApply((ItemStack)(Object)this, damager);
                }

                if (damager != null && isDmgReducible) {
                    float wearReductionChance = DurabilityWearReductionHelper.getDurabilityWearReduction(damager);

                    for(int k = 0; k < damage; ++k) {
                        if (rand.nextFloat() < wearReductionChance) {
                            ++wearReduction;
                        }
                    }
                }

                damage -= durabilityNegation;
                damage -= wearReduction;
                if (damage <= 0) {
                    return false;
                }

                Item item = ((ItemStack)(Object)this).getItem();
                if (item instanceof VaultArmorItem || item instanceof VaultBattleStaffItem || item instanceof VaultTridentItem || item instanceof VaultPlushieItem || item instanceof VaultLootSackItem || item instanceof FocusItem || item instanceof WandItem || item instanceof VaultShieldItem || item instanceof VaultSwordItem || item instanceof VaultAxeItem || item instanceof MagnetItem) {
                    for(GearDurabilityPrestigePower power : PrestigeHelper.getPrestige(damager).getAll(GearDurabilityPrestigePower.class, Skill::isUnlocked)) {
                        damage = power.getReducedDamage(damage);
                    }
                }
            }

            if (damager != null && damage != 0) {
                CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(damager, (ItemStack)(Object)this, this.getDamageValue() + damage);
            }

            int absDamage = this.getDamageValue() + damage;
            this.setDamageValue(absDamage);
            int newDamage = this.getDamageValue();
            if (damager != null && newDamage == -1) {
                damager.level.playSound((Player)null, damager.getOnPos(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            return newDamage >= this.getMaxDamage();
        }
    }
}
