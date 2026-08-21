package xyz.iwolfking.woldsvaults.gods.trees.tenos;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Drillmaster (r111): raises the Fortune cap from 5 to 7 for this player.
 *
 * <p>The cap lives on the enchantment read path, which is one of the hottest methods in the game,
 * so this used to sample node membership into a private set once a second rather than ask the god
 * data on every read. The shared {@code GodNodeCache} answers exactly that question at exactly that
 * cost - two map lookups, no curios walk and no saved-data read - so the private set, its tick
 * sampler and its logout listener are gone and the answer is live rather than up to a second stale.
 *
 * <p>The {@link ServerPlayer} test is what keeps client-side enchantment reads on base-mod
 * behaviour, which the sampled set gave for free by only ever being populated server side.
 */
public final class TenosDrillmaster {
    private TenosDrillmaster() {
    }

    /** Called from the enchantment cap map on every enchantment read. */
    public static boolean hasRaisedFortuneCap(LivingEntity entity) {
        return entity instanceof ServerPlayer player && TenosNodes.isActive(player, TenosNodes.DRILLMASTER);
    }

    public static int raisedFortuneCap() {
        return TenosNodeHandlers.params(TenosNodes.DRILLMASTER,
                TenosNodeHandlers.DrillmasterParams.class).raised_fortune_cap();
    }
}
