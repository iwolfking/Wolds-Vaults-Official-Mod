package xyz.iwolfking.woldsvaults.mixins.ae2.accessors;

import appeng.client.gui.AEBaseScreen;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AEBaseScreen.class, remap = false)
public interface AccessorAEBaseScreen {
    @Invoker("addToLeftToolbar")
    Button invokeAddToLeftToolbar(Button button);
}