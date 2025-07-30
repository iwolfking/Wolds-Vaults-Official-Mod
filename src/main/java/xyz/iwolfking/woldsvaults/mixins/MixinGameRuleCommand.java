package xyz.iwolfking.woldsvaults.mixins;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.helper.GameruleHelper;

@Mixin(GameRuleCommand.class)
public class MixinGameRuleCommand {
    @Inject(method = "setRule", at = @At("RETURN"))
    private static <T extends GameRules.Value<T>> void triggerGameruleSync(CommandContext<CommandSourceStack> pSource, GameRules.Key<T> pGameRule, CallbackInfoReturnable<Integer> cir) {
        GameruleHelper.syncGameRules(pSource.getSource().getServer().getPlayerList().getPlayers());
    }
}
