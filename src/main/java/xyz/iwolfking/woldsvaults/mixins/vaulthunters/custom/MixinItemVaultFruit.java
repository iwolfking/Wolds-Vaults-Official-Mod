package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.item.ItemVaultFruit;
import iskallia.vault.snapshot.AttributeSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.lib.IRottenFruit;
import xyz.iwolfking.woldsvaults.api.util.HealthReductionHelper;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import static iskallia.vault.item.ItemVaultFruit.MAX_HEALTH_REDUCTION_ATTRIBUTE_MODIFIER_UUID;

@Mixin(value = ItemVaultFruit.class, remap = false)
public abstract class MixinItemVaultFruit extends Item implements IRottenFruit {

    public MixinItemVaultFruit(Properties pProperties) {
        super(pProperties);
    }

    /**
     * @author aida
     * @reason overfruit - change fruit maxHP penalty scaling to discourage armor swapping
     */
    @Overwrite
    protected void reducePlayerMaxHealth(ServerPlayer player) {
        HealthReductionHelper.reducePlayerMaxHealth(player);
    }

    /**
     * @author aida
     * @reason overfruit - change fruiting minimum health threshold to be in line with everything else
     */
    @Overwrite
    protected boolean isPlayerMaxHealthGreaterThan(Player player, int threshold) {
        AttributeInstance maxHP = player.getAttribute(Attributes.MAX_HEALTH);
        return maxHP != null && maxHP.getValue() >= 5.0;
    }

    @Redirect(method = "onEaten", at = @At(value = "INVOKE", target = "Liskallia/vault/snapshot/AttributeSnapshot;getAttributeValue(Liskallia/vault/gear/attribute/VaultGearAttribute;Liskallia/vault/gear/attribute/type/VaultGearAttributeTypeMerger;)Ljava/lang/Object;"))
    private Object cancelFruitEffectivenessTimeGain(AttributeSnapshot instance, VaultGearAttribute<Float> attribute, VaultGearAttributeTypeMerger<Float, Float> merger) {
        return (instance.getAttributeValue(attribute, merger) / (1.0F + instance.getAttributeValue(attribute, merger)));
    }

    @Override
    public float getRotChance() {
        if(this.getRegistryName().equals(VaultMod.id("sweet_kiwi"))) {
            return 0.05F;
        }
        else if(this.getRegistryName().equals(VaultMod.id("grapes"))) {
            return 0.15F;
        }
        else if(this.getRegistryName().equals(VaultMod.id("bitter_lemon"))) {
            return 0.25F;
        }
        else if(this.getRegistryName().equals(VaultMod.id("mango"))) {
            return 0.40F;
        }
        else if(this.getRegistryName().equals(VaultMod.id("sour_orange"))) {
            return 0.55F;
        }
        else if(this.getRegistryName().equals(VaultMod.id("star_fruit"))) {
            return 0.85F;
        }
        else if(this.getRegistryName().equals(VaultMod.id("mystic_pear"))) {
            return 1.5F;
        }

        return ModConfigs.VAULT_FRUIT_CONFIG.baseRotChance;
    }
}
