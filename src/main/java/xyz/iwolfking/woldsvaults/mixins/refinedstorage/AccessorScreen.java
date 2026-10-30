package xyz.iwolfking.woldsvaults.mixins.refinedstorage;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Screen.class)
public interface AccessorScreen {
    @Accessor("children")
    List<AbstractWidget> getChildren();

    @Accessor("renderables")
    List<Object> getRenderables();
}
