package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.init.ModBlocks;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = ModBlocks.class, remap = false)
public class MixinModBlocks {

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntityType$Builder;of(Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;[Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/entity/BlockEntityType$Builder;", remap = true, ordinal = 9), index = 1)
    private static Block[] injectNewBackpack(Block[] pValidBlocks) {
        ArrayList<Block> pedestalList = new java.util.ArrayList<>(Arrays.stream(pValidBlocks).toList());
        pedestalList.add(xyz.iwolfking.woldsvaults.init.ModBlocks.ETCHING_PEDESTAL);
        pedestalList.add(xyz.iwolfking.woldsvaults.init.ModBlocks.GOD_VENDOR_PEDESTAL);
        pedestalList.add(xyz.iwolfking.woldsvaults.init.ModBlocks.BLACKSMITH_VENDOR_PEDESTAL);
        pedestalList.add(xyz.iwolfking.woldsvaults.init.ModBlocks.RARE_VENDOR_PEDESTAL);
        pedestalList.add(xyz.iwolfking.woldsvaults.init.ModBlocks.OMEGA_VENDOR_PEDESTAL);
        pedestalList.add(xyz.iwolfking.woldsvaults.init.ModBlocks.SPOOKY_VENDOR_PEDESTAL);
        return pedestalList.toArray(new Block[]{});
    }

}
