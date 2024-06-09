package xyz.iwolfking.woldsvaults.mixins.vaulthunters;

import iskallia.vault.core.vault.player.ClassicListenersLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ClassicListenersLogic.class, remap = false)
public class MixinClassicListenersLogic  {
    /**
     * @author iwolfking
     * @reason Add Unhinged Scavenger as special case
     */
    @Overwrite
    public String getVaultObjective(String key) {
        String var10000;
        switch (key == null ? "" : key.toLowerCase()) {
            case "boss":
                var10000 = "Hunt the Guardians";
                break;
            case "unhinged_scavenger":
                var10000 = "Unhinged Scavenger Hunt";
                break;
            case "scavenger":
                var10000 = "Scavenger Hunt";
                break;
            case "haunted_braziers":
                var10000 = "Haunted Brazier";
                break;
            case "enchanted_elixir":
                var10000 = "Enchanted Elixir";
                break;
            case "brutal_bosses":
                var10000 = "Brutal Bosses";
                break;
            case "monolith":
                var10000 = "Brazier";
                break;
            case "empty":
                var10000 = "Architect";
                break;
            case "":
                var10000 = "";
                break;
            default:
                var10000 = key.substring(0, 1).toUpperCase() + key.substring(1);
        }

        return var10000;
    }
}