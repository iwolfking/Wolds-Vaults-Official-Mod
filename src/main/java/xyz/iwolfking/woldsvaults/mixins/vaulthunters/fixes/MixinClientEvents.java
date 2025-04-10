package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.client.gui.component.OpenSupportersButton;
import iskallia.vault.event.ClientEvents;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ClientEvents.class, remap = false)
public class MixinClientEvents {

    /**
     * @author iwolfking
     * @reason Remove supporters button.
     */
    @Overwrite
    public static void onGuiInit(ScreenEvent.InitScreenEvent event) {
    }
}
