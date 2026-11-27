package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.screen.player.element.StatLabelElementBuilder;
import iskallia.vault.client.gui.screen.player.element.StatLabelListElement;
import iskallia.vault.client.gui.screen.player.element.StatListPlayerContainerElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.client.screens.stats.CategorizedStatListElement;
import xyz.iwolfking.woldsvaults.client.screens.stats.StatScreenLayout;

import java.util.List;
import java.util.Map;

/** The Statistics tab's player stat list is shown in categories when the stat screen table is loaded. */
@Mixin(value = StatListPlayerContainerElement.class, remap = false)
public class MixinStatListPlayerContainerElement {
    @SuppressWarnings("rawtypes")
    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Liskallia/vault/client/gui/framework/spatial/spi/ISpatial;Ljava/util/List;)Liskallia/vault/client/gui/screen/player/element/StatLabelListElement;"))
    private StatLabelListElement woldsvaults$categorize(ISpatial spatial, List<StatLabelElementBuilder<?>> statList, Operation<StatLabelListElement> original) {
        Map<String, List<StatLabelElementBuilder<?>>> groups = StatScreenLayout.group(statList);
        if (groups == null) {
            return original.call(spatial, statList);
        }
        return new CategorizedStatListElement(spatial, groups);
    }
}
