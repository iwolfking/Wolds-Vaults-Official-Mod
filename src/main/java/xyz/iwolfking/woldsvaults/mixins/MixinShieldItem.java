package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ShieldItem.class, remap = false)
public class MixinShieldItem {
}
