package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;


import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.client.gui.screen.player.StatisticsElementContainerScreenData;
import iskallia.vault.client.gui.screen.player.element.GearAttributeStatLabel;
import iskallia.vault.client.gui.screen.player.element.StatLabelElementBuilder;
import iskallia.vault.init.ModGearAttributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = StatisticsElementContainerScreenData.class, remap = false)
public class MixinStatisticsElementContainerScreenData {

    @Shadow @Final protected Player player;

    @Inject(method = "getStatListPlayer", at = @At("TAIL"))
    private void addNewDamageStats(CallbackInfoReturnable<List<StatLabelElementBuilder<?>>> cir, @Local List<StatLabelElementBuilder<?>> result) {
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.IDONA_AFFINITY));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.TENOS_AFFINITY));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.VELARA_AFFINITY));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.WENDARR_AFFINITY));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.HIT_HEARTS));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.FRUIT_EFFECTIVENESS));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.DODGE_PERCENT));
        result.add( GearAttributeStatLabel.ofInteger(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.SOUL_LEECH_FLAT));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.AP_SCALING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.CHAINING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.THORNS_SCALING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.CHANNELING_CHANCE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.DISMANTLE_CHANCE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_CHANCE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.ECHOING_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.HEXING_CHANCE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.EXECUTION_DAMAGE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, xyz.iwolfking.woldsvaults.init.ModGearAttributes.REAVING_DAMAGE));
    }
}
