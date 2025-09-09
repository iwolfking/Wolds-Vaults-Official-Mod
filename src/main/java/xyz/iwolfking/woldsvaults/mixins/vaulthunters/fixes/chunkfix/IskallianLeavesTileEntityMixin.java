package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes.chunkfix;

import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.blocks.tiles.IskallianLeavesTileEntity;

import java.util.List;

@Mixin(IskallianLeavesTileEntity.class)
public class IskallianLeavesTileEntityMixin {
    @Shadow(remap = false) @Final private List<String> templateTags;

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    public void clearTemplateTags(CompoundTag nbt, CallbackInfo ci) {
        this.templateTags.clear();
    }
}
