package xyz.iwolfking.woldsvaults.modifiers.gear.scepter;

import iskallia.vault.event.ActiveFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.lib.ElementalType;
import xyz.iwolfking.woldsvaults.api.util.DerivedHit;
import xyz.iwolfking.woldsvaults.api.util.ElementHelper;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Magibash strikes from a Magibash scepter's melee hits. Each strike is queued from the melee hit's hurt event and
 * dealt at the end of the server tick as its own magic hit, with the melee hit's invulnerability frames cleared around
 * it, so it neither nests inside the melee hit's damage handling nor gets eaten by the frames that hit just set. The
 * strike counts as ability damage, carries the scepter's elements and is kept out of Rampage like the other scepter
 * casts.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class MagibashHits {
    public static final float AP_COEFFICIENT = 0.5F;
    public static final float MYTHIC_AP_COEFFICIENT = 1.0F;

    private static final Deque<Pending> QUEUE = new ArrayDeque<>();

    private MagibashHits() {
    }

    private record Pending(ServerPlayer attacker, LivingEntity target, float amount, List<ElementalType> elements) {
    }

    /** Queues a Magibash strike of {@code amount} on {@code target}, dealt at the end of this server tick. */
    public static void queue(ServerPlayer attacker, LivingEntity target, float amount, List<ElementalType> elements) {
        if (amount > 0.0F) {
            QUEUE.add(new Pending(attacker, target, amount, elements));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        int budget = QUEUE.size();
        while (budget-- > 0) {
            Pending hit = QUEUE.poll();
            if (hit == null) {
                return;
            }
            if (hit.attacker().isRemoved() || !hit.target().isAlive() || hit.target().level != hit.attacker().level) {
                WoldLogger.debug("Magibash strike on {} dropped: the target died or left before the end of the tick.",
                        hit.target().getType().getRegistryName());
                continue;
            }
            deal(hit);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        QUEUE.clear();
    }

    private static void deal(Pending hit) {
        WoldActiveFlags.IS_NO_KNOCKBACK_DAMAGE.runWithFlag(() -> {
            ActiveFlags.IS_AP_ATTACKING.push();
            try {
                WoldActiveFlags.IS_SCEPTER_CAST_ATTACKING.runWithFlag(() ->
                        ElementHelper.runWithElements(hit.elements(), () ->
                                DerivedHit.deal(hit.target(), DamageSource.indirectMagic(hit.attacker(), hit.attacker()), hit.amount())));
            } finally {
                ActiveFlags.IS_AP_ATTACKING.pop();
            }
        });
    }
}
