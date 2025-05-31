package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.client.gui.screen.summary.element.CombatStatsContainerElement;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CombatStatsContainerElement.class, remap = false)
public class MixinCombatStatsElementContainer {
}
