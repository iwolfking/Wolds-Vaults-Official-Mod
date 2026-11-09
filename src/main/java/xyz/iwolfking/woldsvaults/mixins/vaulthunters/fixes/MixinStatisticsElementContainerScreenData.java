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
import iskallia.vault.util.calc.AttributeLimitHelper;
import iskallia.vault.util.calc.ChainHelper;
import iskallia.vault.util.calc.ResistanceHelper;
import iskallia.vault.util.calc.StunChanceHelper;
import iskallia.vault.util.calc.ThornsHelper;
import iskallia.vault.util.calc.TrapDisarmChanceHelper;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.data.WoldConstants;
import xyz.iwolfking.woldsvaults.api.util.WoldAttributeHelper;
import xyz.iwolfking.woldsvaults.api.util.VaultGodAffinityHelper;
import xyz.iwolfking.woldsvaults.stats.StatDisplayValues;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatRowStyle;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatScreenLayout;
import xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors.StatLabelElementBuilderAccessor;

import java.util.List;

@Mixin(value = StatisticsElementContainerScreenData.class, remap = false)
public class MixinStatisticsElementContainerScreenData {

    @Unique private static final String DAMAGE_INCREASE_LABEL = "Damage Increase";

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
        result.add( GearAttributeStatLabel.of(this.player, ModGearAttributes.IDONA_AFFINITY, VaultGodAffinityHelper::getClientIdonaAffinityPercent));
        result.add( GearAttributeStatLabel.of(this.player, ModGearAttributes.TENOS_AFFINITY, VaultGodAffinityHelper::getClientTenosAffinityPercent));
        result.add( GearAttributeStatLabel.of(this.player, ModGearAttributes.VELARA_AFFINITY, VaultGodAffinityHelper::getClientVelaraAffinityPercent));
        result.add( GearAttributeStatLabel.of(this.player, ModGearAttributes.WENDARR_AFFINITY, VaultGodAffinityHelper::getClientWendarrAffinityPercent));
        result.add(GearAttributeStatLabel.ofFloat(player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.DODGE_PERCENT, WoldAttributeHelper::getDodgeChance, (p) -> WoldConstants.DODGE_PERCENT_CAP));

        result.add( GearAttributeStatLabel.ofInteger(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.SOUL_LEECH_FLAT));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.AP_SCALING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.CHAINING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.THORNS_SCALING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.THORNS_FLAT_PERCENT));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.CHANNELING_CHANCE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.DISMANTLE_CHANCE));
        result.add( GearAttributeStatLabel.ofFloat(player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_CHANCE, WoldAttributeHelper::getEchoingChance, (p) -> 1.0F));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.HEXING_CHANCE, WoldAttributeHelper::getHexingChance, (p) -> 1.0F));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.EXECUTION_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.REAVING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.INCREASED_EFFECT_CLOUD_CHANCE));
        woldsvaults$replace(result, ModGearAttributes.RESISTANCE, GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.RESISTANCE,
                p -> ResistanceHelper.getResistanceUnlimited(p, null),
                p -> WoldAttributeHelper.getResistanceLimit(p, AttributeLimitHelper.getResistanceLimit(p))));
        woldsvaults$replace(result, ModGearAttributes.THORNS_DAMAGE_FLAT, GearAttributeStatLabel.of(this.player, ModGearAttributes.THORNS_DAMAGE_FLAT, ThornsHelper::getAdditionalThornsFlatDamage));
        woldsvaults$replace(result, ModGearAttributes.ON_HIT_CHAIN, GearAttributeStatLabel.of(this.player, ModGearAttributes.ON_HIT_CHAIN, ChainHelper::getChainCount));
        woldsvaults$replace(result, ModGearAttributes.ON_HIT_STUN, GearAttributeStatLabel.of(this.player, ModGearAttributes.ON_HIT_STUN, p -> StunChanceHelper.getStunChance(p, null)));
        woldsvaults$replace(result, ModGearAttributes.TRAP_DISARMING, GearAttributeStatLabel.of(this.player, ModGearAttributes.TRAP_DISARMING, TrapDisarmChanceHelper::getTrapDisarmChance));
        woldsvaults$replace(result, ModGearAttributes.MOVEMENT_SPEED, GearAttributeStatLabel.of(this.player, ModGearAttributes.MOVEMENT_SPEED, StatDisplayValues::movementSpeed));
        woldsvaults$replaceDamageIncrease(result);
        StatScreenLayout.stylePlayer(result);
    }

    @Unique
    private static void woldsvaults$replace(List<StatLabelElementBuilder<?>> rows, VaultGearAttribute<?> attribute, StatLabelElementBuilder<?> replacement) {
        String key = attribute.getRegistryName().toString();
        for (int i = 0; i < rows.size(); i++) {
            if (key.equals(((StatRowStyle) rows.get(i)).woldsvaults$getStatKey())) {
                rows.set(i, replacement);
                return;
            }
        }
        WoldsVaults.LOGGER.error("Statistics tab: no {} row to replace with the combat-accurate value; added instead", key);
        rows.add(replacement);
    }

    @Unique
    private void woldsvaults$replaceDamageIncrease(List<StatLabelElementBuilder<?>> rows) {
        Player owner = this.player;
        StatLabelElementBuilder<Float> replacement = new StatLabelElementBuilder<>(() -> DAMAGE_INCREASE_LABEL,
                () -> List.of(iskallia.vault.init.ModConfigs.MENU_PLAYER_STAT_DESCRIPTIONS.getModGearAttributeDescriptionFor(ModGearAttributes.DAMAGE_INCREASE.getRegistryName())),
                () -> StatDisplayValues.damageIncrease(owner),
                value -> ModGearAttributes.DAMAGE_INCREASE.getReader().getValueDisplay(value),
                TextColor.fromRgb(0));
        for (int i = 0; i < rows.size(); i++) {
            if (DAMAGE_INCREASE_LABEL.equals(((StatLabelElementBuilderAccessor) rows.get(i)).woldsvaults$getLabelSupplier().get())) {
                rows.set(i, replacement);
                return;
            }
        }
        WoldsVaults.LOGGER.error("Statistics tab: no {} row to replace with the Purist-inclusive value; added instead", DAMAGE_INCREASE_LABEL);
        rows.add(replacement);
    }

    @Inject(method = "getStatListProminent", at = @At("RETURN"))
    private void styleProminentStats(CallbackInfoReturnable<List<StatLabelElementBuilder<?>>> cir) {
        StatScreenLayout.styleProminent(cir.getReturnValue());
    }
}
