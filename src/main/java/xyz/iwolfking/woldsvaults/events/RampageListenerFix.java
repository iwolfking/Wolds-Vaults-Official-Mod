package xyz.iwolfking.woldsvaults.events;

import iskallia.vault.skill.ability.effect.RampageBloodlustAbility;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;

public final class RampageListenerFix {

    private RampageListenerFix() {
    }

    /**
     * Undoes a duplicate Forge listener registration in the_vault. {@code RampageBloodlustAbility}
     * extends {@code RampageAbility} while carrying its own {@code @Mod.EventBusSubscriber}, and
     * {@code EventBus#registerClass} collects handlers via {@code Class#getMethods()}, which includes
     * inherited public statics. The parent's {@code onDirectMeleeDamage} therefore ends up registered
     * twice — once per class — so every Rampage specialization multiplied direct melee hits by
     * {@code (1 + damageIncrease)} twice, turning a max-tier Berserker's intended 9x into 81x.
     *
     * <p>Dropping the subclass registration removes the duplicate and Bloodlust's own kill listener
     * together, so the latter is re-added explicitly at the priority its annotation declared. Only
     * safe once Forge has finished automatic subscribing, i.e. no earlier than common setup.
     */
    public static void init() {
        MinecraftForge.EVENT_BUS.unregister(RampageBloodlustAbility.class);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, LivingDeathEvent.class,
                RampageBloodlustAbility::onDirectMeleeKill);
    }
}
