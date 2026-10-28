package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.client.gui.screen.player.StatisticsElementContainerScreenData;
import iskallia.vault.client.gui.screen.player.element.GearAttributeStatLabel;
import iskallia.vault.client.gui.screen.player.element.StatLabelElementBuilder;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.util.calc.CopiousHelper;
import iskallia.vault.util.calc.ItemQuantityHelper;
import iskallia.vault.util.calc.ItemRarityHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.util.WoldAttributeHelper;

import java.util.List;

@Mixin(value = StatisticsElementContainerScreenData.class, remap = false)
public class MixinStatisticsElementContainerScreenData {

    @Shadow @Final protected Player player;

    @WrapOperation(method = "getStatListPlayer", at = @At(value = "INVOKE", target = "Liskallia/vault/client/gui/screen/player/element/GearAttributeStatLabel;ofFloat(Lnet/minecraft/world/entity/player/Player;Liskallia/vault/gear/attribute/VaultGearAttribute;)Liskallia/vault/client/gui/screen/player/element/StatLabelElementBuilder;"))
    private StatLabelElementBuilder<Float> properlyHandleStatValues(Player player, VaultGearAttribute<Float> attribute, Operation<StatLabelElementBuilder<Float>> original) {
        if(attribute.getRegistryName().equals(ModGearAttributes.ITEM_QUANTITY.getRegistryName())) {
            return GearAttributeStatLabel.ofFloat(player, attribute, ItemQuantityHelper::getItemQuantity, (p) -> Float.MAX_VALUE);
        }
        else if(attribute.getRegistryName().equals(ModGearAttributes.ITEM_RARITY.getRegistryName())) {
            return GearAttributeStatLabel.ofFloat(player, attribute, ItemRarityHelper::getItemRarity, (p) -> Float.MAX_VALUE);
        }
        else if(attribute.getRegistryName().equals(ModGearAttributes.COPIOUSLY.getRegistryName())) {
            return GearAttributeStatLabel.ofFloat(player, attribute, CopiousHelper::getCopiousChance, (p) -> Float.MAX_VALUE);
        }

        return original.call(player, attribute);
    }

    @Inject(method = "getStatListPlayer", at = @At("TAIL"))
    private void addNewDamageStats(CallbackInfoReturnable<List<StatLabelElementBuilder<?>>> cir, @Local List<StatLabelElementBuilder<?>> result) {
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.IDONA_AFFINITY));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.TENOS_AFFINITY));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.VELARA_AFFINITY));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.WENDARR_AFFINITY));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.HIT_HEARTS));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.FRUIT_EFFECTIVENESS));
        result.add(GearAttributeStatLabel.ofFloat(player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.DODGE_PERCENT, WoldAttributeHelper::getDodgeChance, (p) -> WoldConstants.DODGE_PERCENT_CAP));

        result.add( GearAttributeStatLabel.ofInteger(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.SOUL_LEECH_FLAT));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.AP_SCALING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.CHAINING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.THORNS_SCALING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.CHANNELING_CHANCE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.DISMANTLE_CHANCE));
        result.add( GearAttributeStatLabel.ofFloat(player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_CHANCE, WoldAttributeHelper::getEchoingChance, (p) -> 1.0F));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.HEXING_CHANCE, WoldAttributeHelper::getHexingChance, (p) -> 1.0F));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.EXECUTION_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.REAVING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE));
    }
}
