package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.block.render.MonolithRenderer;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MonolithRenderer.class, remap = false)
public class MixinMonolithRenderer {

    @WrapOperation(method = "lambda$render$0", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/modifier/spi/VaultModifier;getChatDisplayNameComponent(I)Lnet/minecraft/network/chat/Component;"))
    private static Component removeEmoji(VaultModifier<?> modifier, int modifierStackSize, Operation<Component> original){
        String modifierName = original.call(modifier, modifierStackSize).getString();
        modifierName = modifierName.replaceAll(":.*: *", "");
        Style style = Style.EMPTY.withColor(modifier.getDisplayTextColor());
        return new TextComponent(modifierName).withStyle(style);
    }
}
