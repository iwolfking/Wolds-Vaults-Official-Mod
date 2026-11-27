package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.client.gui.screen.player.legacy.SplitTabContent;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.spi.AbstractDialog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The dialog and pan halves of a skill menu tab. */
@Mixin(value = SplitTabContent.class, remap = false)
public interface SplitTabContentAccessor {
    @Accessor("dialog")
    AbstractDialog<?> woldsvaults$getDialog();
}
