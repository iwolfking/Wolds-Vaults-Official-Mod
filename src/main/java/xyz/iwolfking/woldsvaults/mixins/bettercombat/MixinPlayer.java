package xyz.iwolfking.woldsvaults.mixins.bettercombat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.integration.bettercombat.DualWieldSnapshotManager;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "bettercombat")
        }
)
@Mixin(value = Player.class)
public abstract class MixinPlayer {

    @WrapMethod(method = "attack")
    private void woldsVaults$useOffhandSnapshot(
            Entity target,
            Operation<Void> original
    ) {
        Player player = (Player) (Object) this;

        if (!(player instanceof ServerPlayer serverPlayer)
                || !(player instanceof EntityPlayer_BetterCombat betterCombatPlayer)) {
            original.call(target);
            return;
        }

        AttackHand hand = betterCombatPlayer.getCurrentAttack();

        if (hand != null && hand.isOffHand()) {
            DualWieldSnapshotManager.withOffhandSnapshot(
                    serverPlayer,
                    () -> original.call(target)
            );
        } else {
            original.call(target);
        }
    }
}