package xyz.iwolfking.woldsvaults.mixins.vaulthunters.compat.lightmanscurrency;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lightman314.lightmanscurrency.common.commands.CommandPlayerTrading;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "lightmanscurrency")
        }
)
@Mixin(value = CommandPlayerTrading.class, remap = false)
public class MixinTradeCommand {
    /**
     * @author iwolfking
     * @reason Disable trading in vault (?)
     */
    @Inject(method = "acceptPlayerTrade", at = @At("HEAD"), cancellable = true)
    private static void acceptPlayerTrade(CommandContext<CommandSourceStack> context, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if(context.getSource().getPlayerOrException().getLevel().dimension().getRegistryName().getNamespace().equals("the_vault") || EntityArgument.getPlayer(context, "player").getLevel().dimension().getRegistryName().getNamespace().equals("the_vault")) {
            EasyText.sendCommandFail(context.getSource(), new TextComponent("You cannot trade in the vault..."));
            cir.setReturnValue(0);
        }
    }
}
