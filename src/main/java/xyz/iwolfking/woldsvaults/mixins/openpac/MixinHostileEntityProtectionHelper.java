package xyz.iwolfking.woldsvaults.mixins.openpac;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xaero.pac.common.server.claims.protection.ChunkProtectionEntityHelper;

@Restriction(
        require = {
                @Condition(type = Condition.Type.MOD, value = "openpartiesandclaims")
        }
)
@Mixin(value = ChunkProtectionEntityHelper.class, remap = false)
public abstract class MixinHostileEntityProtectionHelper {
    @Shadow abstract boolean hostileException(Entity e);

    /**
     * @author iwolfking
     * @reason Add monster to be considered as hostile.
     */
    @Overwrite
    boolean isHostile(Entity e) {
        return e.getLevel().getDifficulty() != Difficulty.PEACEFUL && !this.hostileException(e) && (e instanceof Enemy || e instanceof Monster || e.getSoundSource() == SoundSource.HOSTILE);
    }
}
