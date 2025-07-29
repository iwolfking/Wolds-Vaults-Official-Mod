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

}
