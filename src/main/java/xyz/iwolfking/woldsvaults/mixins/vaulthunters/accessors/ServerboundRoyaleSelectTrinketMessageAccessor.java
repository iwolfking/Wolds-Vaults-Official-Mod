package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.network.message.ServerboundRoyaleSelectTrinketMessage;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ServerboundRoyaleSelectTrinketMessage.class, remap = false)
public interface ServerboundRoyaleSelectTrinketMessageAccessor {
    @Accessor("trinket")
    ResourceLocation getTrinket();
}
