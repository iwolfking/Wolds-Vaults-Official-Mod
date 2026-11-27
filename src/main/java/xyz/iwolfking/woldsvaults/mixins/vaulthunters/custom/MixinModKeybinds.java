package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.platform.InputConstants;
import iskallia.vault.init.ModKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.abilities.WoldsAbilityGroups;

import java.util.Locale;

@Mixin(value = ModKeybinds.class, remap = false)
public class MixinModKeybinds {
    /**
     * Gives each of this mod's datagen ability groups an unbound entry in Vault Hunters' quick-fire key map, since the
     * code that builds ability tooltips expects one for every group.
     */
    @Inject(method = "register", at = @At("TAIL"))
    private static void woldsvaults$registerDatagenQuickfireKeys(FMLClientSetupEvent event, CallbackInfo ci) {
        for (String id : WoldsAbilityGroups.DATAGEN_GROUPS) {
            if (ModKeybinds.abilityQuickfireKey.containsKey(id)) {
                continue;
            }
            String name = "key.the_vault.quickselect." + id.toLowerCase(Locale.ROOT).replace(' ', '_');
            KeyMapping keyMapping = new KeyMapping(name, KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, ModKeybinds.QUICKFIRE_CATEGORY);
            ModKeybinds.abilityQuickfireKey.put(id, keyMapping);
        }
    }
}
