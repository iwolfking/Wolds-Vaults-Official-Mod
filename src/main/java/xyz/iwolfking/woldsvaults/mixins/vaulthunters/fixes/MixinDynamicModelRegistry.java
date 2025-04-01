package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.dynamodel.DynamicModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DynamicModel.class, remap = false)
public class MixinDynamicModelRegistry {

    @Shadow @Final protected String displayName;

    /**
     * @author iwolfking
     * @reason Change certain gear piece names
     */
    @Overwrite
    public String getDisplayName() {
        if(this.displayName.equals("Skallified Sword")) {
            return "Woldified Sword";
        }
        else if(this.displayName.equals("Iskallium Tech Wand")) {
            return "Woldium Tech Wand";
        } else if (this.displayName.equals("Iskallibur")) {
            return "Woldibur";
        }
        return this.displayName;
    }
}
