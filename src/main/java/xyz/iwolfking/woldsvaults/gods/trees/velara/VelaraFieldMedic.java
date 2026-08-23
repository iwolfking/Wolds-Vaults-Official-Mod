package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Field Medic: a multiplier on healing the player gives to allies, never to themselves. The healer
 * is carried on a thread local pushed from mixins.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VelaraFieldMedic {
    private static final ThreadLocal<LivingEntity> CURRENT_HEALER = new ThreadLocal<>();

    private VelaraFieldMedic() {
    }

    /** Attributes heals on this thread to {@code healer}. Pair with {@link #popHealer} in a finally. */
    public static LivingEntity pushHealer(LivingEntity healer) {
        LivingEntity previous = CURRENT_HEALER.get();
        CURRENT_HEALER.set(healer);
        return previous;
    }

    public static void popHealer(LivingEntity previous) {
        if (previous == null) {
            CURRENT_HEALER.remove();
        } else {
            CURRENT_HEALER.set(previous);
        }
    }

    /** Runs after {@code PlayerRecoveryHelper}, so the multiplier lands on top of healing efficiency. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHeal(LivingHealEvent event) {
        LivingEntity healer = CURRENT_HEALER.get();
        if (!(healer instanceof ServerPlayer medic) || healer == event.getEntityLiving()) {
            return;
        }
        if (!VelaraNodes.isActive(medic, VelaraNodes.FIELD_MEDIC)) {
            return;
        }
        if (!VelaraParty.isAlly(medic, event.getEntityLiving().getUUID())) {
            return;
        }
        event.setAmount(event.getAmount() * VelaraValues.fieldMedicMultiplier());
    }
}
