package xyz.iwolfking.woldsvaults.mixins.dungeons_library;

import com.infamous.dungeons_libraries.integration.curios.client.CuriosKeyBindings;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = CuriosKeyBindings.class, remap = false)
public abstract class MixinCuriosKeyBindings {

    /**
     * @author iwolfking
     * @reason Disable artifact keybinds
     */
    @Overwrite
    public static void setupCuriosKeybindings() {
    }

    /**
     * @author iwolfking
     * @reason Disable artifact keybinds
     */
    @Overwrite
    @SubscribeEvent
    public static void onClientTick(InputEvent.KeyInputEvent event) {

    }
}
