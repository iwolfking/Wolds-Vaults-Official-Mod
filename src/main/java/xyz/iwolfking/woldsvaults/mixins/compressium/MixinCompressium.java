package xyz.iwolfking.woldsvaults.mixins.compressium;

import me.dinnerbeef.compressium.CompressibleBlock;
import me.dinnerbeef.compressium.CompressibleType;
import me.dinnerbeef.compressium.Compressium;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.init.ModCompressibleBlocks;

import java.util.HashMap;

@Mixin(value = Compressium.class, remap = false)
public class MixinCompressium {
    @Shadow
    @Final
    private static HashMap<String, CompressibleBlock> COMPRESSIUM_BLOCKS;

    @Inject(method = "registerBlocks", at = @At("HEAD"))
    private void addVHCompressiumBlocks(RegistryEvent.Register<Block> event, CallbackInfo ci) {
        COMPRESSIUM_BLOCKS.putAll(ModCompressibleBlocks.ADDITIONAL_COMPRESSIBLE_BLOCKS);
    }
}
