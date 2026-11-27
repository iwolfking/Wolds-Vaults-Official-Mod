package xyz.iwolfking.woldsvaults.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

@Mixin(Player.class)
public abstract class MixinPlayerMeleeFlag {

    /**
     * Marks every weapon hit (a swing, and the rang, trident and Riptide hits that go through {@code attack}) so on-hit
     * effects that should only follow weapon hits, such as Sparkles, can tell them from ability damage.
     */
    @WrapMethod(method = "attack")
    private void woldsvaults$markMelee(Entity target, Operation<Void> original) {
        WoldActiveFlags.IS_MELEE_ATTACKING.runWithFlag(() -> original.call(target));
    }
}
