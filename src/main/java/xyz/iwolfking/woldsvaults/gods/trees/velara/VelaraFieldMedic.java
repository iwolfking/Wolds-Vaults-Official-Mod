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

    public static void pushHealer(LivingEntity healer) {
        CURRENT_HEALER.set(healer);
    }

    public static void popHealer() {
        CURRENT_HEALER.remove();
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
