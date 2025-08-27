package xyz.iwolfking.woldsvaults.mixins.dungeons_mobs;

import com.infamous.dungeons_libraries.utils.GoalUtils;
import com.infamous.dungeons_mobs.capabilities.animatedprops.AnimatedProps;
import com.infamous.dungeons_mobs.capabilities.animatedprops.AnimatedPropsHelper;
import com.infamous.dungeons_mobs.entities.illagers.ReplacedIllagerEvents;
import com.infamous.dungeons_mobs.goals.ApproachTargetGoal;
import com.infamous.dungeons_mobs.goals.ReplacedModdedAttackGoal;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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
