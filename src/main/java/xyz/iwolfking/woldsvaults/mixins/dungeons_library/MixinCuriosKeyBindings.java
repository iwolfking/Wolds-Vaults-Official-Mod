package xyz.iwolfking.woldsvaults.mixins.dungeons_library;

import com.infamous.dungeons_libraries.integration.curios.client.CuriosKeyBindings;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "dungeons_libraries")
        }
)
@Mixin(value = CuriosKeyBindings.class, remap = false)
public abstract class MixinCuriosKeyBindings {
    /**
     * @author iwolfking
     * @reason Disable artifact keybinds
     */
    @Overwrite
    @SubscribeEvent
    public static void onClientTick(InputEvent.KeyInputEvent event) {

    }
}
