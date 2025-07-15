package xyz.iwolfking.woldsvaults.mixins.dungeons_library;

import com.infamous.dungeons_libraries.capabilities.builtinenchants.AttacherBuiltInEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = AttacherBuiltInEnchantments.class, remap = false)
public class MixinAttacherBuiltInEnchantments {
    /**
     * @author iwolfking
      * @reason Disable built in enchantment capability
     */
    @Overwrite
    public static void attach(AttachCapabilitiesEvent<ItemStack> event) {
    }
}
