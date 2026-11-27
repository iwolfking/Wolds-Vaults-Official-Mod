package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.AbilityDialog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The specialization the Abilities dialog shows, i.e. the node selected in the tree. */
@Mixin(value = AbilityDialog.class, remap = false)
public interface AbilityDialogAccessor {
    @Accessor("selectedAbility")
    String woldsvaults$getSelectedAbility();
}
