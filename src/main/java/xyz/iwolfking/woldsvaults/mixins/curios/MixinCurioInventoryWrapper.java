package xyz.iwolfking.woldsvaults.mixins.curios;

import xyz.iwolfking.woldsvaults.items.trinket_pouch.PouchMigration;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability.CurioInventoryWrapper;

@Mixin(value = CurioInventoryWrapper.class, remap = false)
public abstract class MixinCurioInventoryWrapper {
    @ModifyVariable(method = "readTag", at = @At("HEAD"), argsOnly = true)
    private Tag convertBeforeSlotValidation(Tag tag) {
        return PouchMigration.serializedCurios(tag);
    }

    @Inject(method = "readTag", at = @At("HEAD"))
    private void beginRestore(Tag tag, CallbackInfo callback) {
        PouchMigration.setRestoring(true);
    }

    @Inject(method = "readTag", at = @At("RETURN"))
    private void finishRestore(Tag tag, CallbackInfo callback) {
        PouchMigration.setRestoring(false);
    }
}
