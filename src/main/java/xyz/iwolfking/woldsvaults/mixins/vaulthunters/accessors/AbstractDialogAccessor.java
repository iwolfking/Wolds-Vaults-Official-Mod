package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.client.gui.screen.player.legacy.tab.split.spi.AbstractDialog;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reaches the learn and unlearn buttons every skill dialog inherits. They are declared on
 * {@code AbstractDialog} rather than on the concrete dialogs, so a {@code @Shadow} from a mixin on
 * one of those subclasses cannot be resolved by the annotation processor; this accessor can.
 */
@Mixin(value = AbstractDialog.class, remap = false)
public interface AbstractDialogAccessor {
    @Accessor("learnButton")
    Button woldsvaults$getLearnButton();

    @Accessor("regretButton")
    void woldsvaults$setRegretButton(Button button);
}
