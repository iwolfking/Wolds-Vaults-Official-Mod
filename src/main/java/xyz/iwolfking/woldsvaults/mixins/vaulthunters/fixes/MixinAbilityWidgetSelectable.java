package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityWidget;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityWidgetSelectable;
import iskallia.vault.init.ModKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

@Mixin(value = AbilityWidgetSelectable.class, remap = false)
public abstract class MixinAbilityWidgetSelectable {

    /**
     * Ability groups that join the tree after client setup have no quickfire key, and other mods
     * read that map without a null check while building the hover tooltip. Insert an unbound
     * placeholder (not registered with Options) so the lookup never returns null.
     */
    @Inject(method = "renderHover", at = @At("HEAD"))
    private void woldsvaults$ensureQuickfireKey(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        String id = ((AbilityWidget) (Object) this).makeAbilityNode().getId();
        if (id == null || ModKeybinds.abilityQuickfireKey.containsKey(id)) {
            return;
        }
        String name = "key.the_vault.quickselect." + id.toLowerCase(Locale.ROOT).replace(' ', '_');
        ModKeybinds.abilityQuickfireKey.put(id, new KeyMapping(name, KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, ModKeybinds.QUICKFIRE_CATEGORY));
        WoldLogger.error("No quickfire keybind registered for ability group {}; inserted an unbound placeholder", id);
    }
}
