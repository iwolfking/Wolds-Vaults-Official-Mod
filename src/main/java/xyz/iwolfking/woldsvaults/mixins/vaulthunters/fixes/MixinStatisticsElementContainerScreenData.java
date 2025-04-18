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
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.DAMAGE_HORDE));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.DAMAGE_TANK));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.DAMAGE_ASSASSIN));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.DAMAGE_DWELLER));
        result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.DAMAGE_CHAMPION));
        //result.add( GearAttributeStatLabel.ofFloat(this.player, ModGearAttributes.DAMAGE_DUNGEON));
    }
}
