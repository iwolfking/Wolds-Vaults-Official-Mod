package xyz.iwolfking.woldsvaults.gods.trees.velara;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Field Medic: 1.5x on healing the player gives to allies, never on healing themselves.
 *
 * <p>{@code LivingHealEvent} carries no healer and nothing in either codebase attributes a heal to
 * its source, so the healer is carried on a thread local pushed around the handful of call sites
 * that heal someone else. Those pushes live in the mixins under
 * {@code mixins/vaulthunters/gods/velara}; with none of them loaded the flag is simply never set
 * and the node is inert rather than wrong.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class VelaraFieldMedic {
    private static final ThreadLocal<LivingEntity> CURRENT_HEALER = new ThreadLocal<>();

    private VelaraFieldMedic() {
    }

    /**
     * Attributes the heals raised on this thread to {@code healer}, returning whatever attribution
     * it replaced so a nested group heal restores the outer one instead of clearing it. Callers
     * must pair this with {@link #popHealer(LivingEntity)} in a {@code finally}.
     */
    public static LivingEntity pushHealer(LivingEntity healer) {
        LivingEntity previous = CURRENT_HEALER.get();
        CURRENT_HEALER.set(healer);
        return previous;
    }

    /** Restores the attribution {@link #pushHealer(LivingEntity)} replaced. */
    public static void popHealer(LivingEntity previous) {
        if (previous == null) {
            CURRENT_HEALER.remove();
        } else {
            CURRENT_HEALER.set(previous);
        }
    }

    /**
     * Applied after {@code PlayerRecoveryHelper}, so the multiplier lands on top of the
     * recipient's own healing efficiency rather than beside it.
     */
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
