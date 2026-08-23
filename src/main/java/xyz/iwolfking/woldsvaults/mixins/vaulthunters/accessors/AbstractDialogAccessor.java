package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.client.gui.screen.player.legacy.tab.split.spi.AbstractDialog;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reaches the learn and unlearn buttons every skill dialog inherits. */
@Mixin(value = AbstractDialog.class, remap = false)
public interface AbstractDialogAccessor {
    @Accessor("learnButton")
    Button woldsvaults$getLearnButton();

    @Accessor("regretButton")
    void woldsvaults$setRegretButton(Button button);
}
