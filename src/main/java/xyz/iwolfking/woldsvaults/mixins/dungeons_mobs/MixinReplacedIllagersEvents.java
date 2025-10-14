package xyz.iwolfking.woldsvaults.mixins.dungeons_mobs;

import com.infamous.dungeons_mobs.entities.illagers.ReplacedIllagerEvents;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Restriction(
    require = {
        @Condition(type = Condition.Type.MOD, value = "dungeons_mobs")
    }
)
@Mixin(value = ReplacedIllagerEvents.class, remap = false)
public abstract class MixinReplacedIllagersEvents
{

    /**
     * @author iwolfking
     * @reason test again
     */
    @Overwrite
    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {

    }

    /**
     * @author test
     * @reason again
     */
    @Overwrite
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {

    }
}
