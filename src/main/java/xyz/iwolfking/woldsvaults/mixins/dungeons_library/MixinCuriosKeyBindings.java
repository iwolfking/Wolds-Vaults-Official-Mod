package xyz.iwolfking.woldsvaults.mixins.dungeons_library;

import com.infamous.dungeons_libraries.integration.curios.client.CuriosKeyBindings;
import com.mojang.blaze3d.platform.InputConstants;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.*;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "dungeons_libraries")
        }
)
@Mixin(value = CuriosKeyBindings.class, remap = false)
public abstract class MixinCuriosKeyBindings {

    @Shadow @Final public static KeyMapping activateArtifact1;
    @Shadow @Final public static KeyMapping activateArtifact2;
    @Shadow @Final public static KeyMapping activateArtifact3;

    /**
     * @author iwolfking
     * @reason Disable artifact keybinds
     */
    @Overwrite
    public static void setupCuriosKeybindings() {
        activateArtifact1.setKeyConflictContext(KeyConflictContext.IN_GAME);
        ClientRegistry.registerKeyBinding(activateArtifact1);
        activateArtifact2.setKeyConflictContext(KeyConflictContext.IN_GAME);
        ClientRegistry.registerKeyBinding(activateArtifact2);
        activateArtifact3.setKeyConflictContext(KeyConflictContext.IN_GAME);
        ClientRegistry.registerKeyBinding(activateArtifact3);
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
