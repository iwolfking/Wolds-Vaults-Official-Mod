package xyz.iwolfking.woldsvaults.modifiers.gear.riptide;

import iskallia.vault.event.ActiveFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.mixins.LivingEntityAccessor;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Hits derived from a set mechanic rather than swung, queued and dealt at the end of the server tick outside the
 * hurt event that caused them. Every derived hit is a thorns-flagged player hit, so Vault Hunters' on-hit procs,
 * effect clouds and thorns reflection skip it, while lucky hits, damage increase and damage talents still see it.
 * Invulnerability frames are cleared around the hit and restored, so it lands in full and does not clip the next one.
 * <p>
 * Authored hits ({@link #queue}) carry an amount already taken from a fully scaled hit (the riptide shock cone and
 * storm lightning); the amount is restored at the end of the hurt chain so no multiplier applies twice, and only a
 * lucky hit's own multiplier, captured by {@code MixinLuckyHitTalentDerivedHits}, is kept on top of it. Scaled hits
 * ({@link #queueScaled}) start from unscaled attack damage and take the full damage pipeline.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class RiptideDerivedHits {
    private static final Deque<Pending> QUEUE = new ArrayDeque<>();

    private RiptideDerivedHits() {
    }

    private record Pending(ServerPlayer attacker, LivingEntity target, float amount, boolean authored, WoldActiveFlags flag) {
    }

    /** The damage source of an authored derived hit; carries the amount it must land with. */
    public static class Source extends EntityDamageSource {
        private final float authoredAmount;
        private float luckyBefore;
        private float luckyFactor = 1.0F;

        public Source(ServerPlayer attacker, float authoredAmount) {
            super("player", attacker);
            this.authoredAmount = authoredAmount;
        }

        public float getAuthoredAmount() {
            return this.authoredAmount;
        }

        /** The amount the hit lands with: the authored amount times whatever a lucky hit multiplied it by. */
        public float getLandedAmount() {
            return this.authoredAmount * this.luckyFactor;
        }

        public void beginLuckyHit(float amount) {
            this.luckyBefore = amount;
        }

        public void endLuckyHit(float amount) {
            if (this.luckyBefore > 0.0F && amount > 0.0F) {
                this.luckyFactor *= amount / this.luckyBefore;
            }
            this.luckyBefore = 0.0F;
        }
    }

    /** The damage source of a scaled derived hit, which takes the full damage pipeline. */
    public static class ScaledSource extends EntityDamageSource {
        public ScaledSource(ServerPlayer attacker) {
            super("player", attacker);
        }
    }

    public static void queue(ServerPlayer attacker, LivingEntity target, float amount) {
        if (amount > 0.0F) {
            QUEUE.add(new Pending(attacker, target, amount, true, null));
        }
    }

    /** Queues a hit for unscaled damage that the damage pipeline scales, dealt with {@code flag} set when not null. */
    public static void queueScaled(ServerPlayer attacker, LivingEntity target, float amount, WoldActiveFlags flag) {
        if (amount > 0.0F) {
            QUEUE.add(new Pending(attacker, target, amount, false, flag));
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
                continue;
            }
            deal(hit);
        }
    }

    private static void deal(Pending hit) {
        LivingEntity target = hit.target();
        int savedInvulnerable = target.invulnerableTime;
        int savedHurtTime = target.hurtTime;
        float savedLastHurt = ((LivingEntityAccessor) target).getLastHurt();
        ActiveFlags.IS_THORNS_REFLECTING.push();
        WoldActiveFlags.IS_DERIVED_ATTACKING.push();
        if (hit.flag() != null) {
            hit.flag().push();
        }
        try {
            target.invulnerableTime = 0;
            EntityDamageSource source = hit.authored() ? new Source(hit.attacker(), hit.amount()) : new ScaledSource(hit.attacker());
            target.hurt(source, hit.amount());
        } finally {
            target.invulnerableTime = savedInvulnerable;
            target.hurtTime = savedHurtTime;
            ((LivingEntityAccessor) target).setLastHurt(savedLastHurt);
            if (hit.flag() != null) {
                hit.flag().pop();
            }
            WoldActiveFlags.IS_DERIVED_ATTACKING.pop();
            ActiveFlags.IS_THORNS_REFLECTING.pop();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void restoreAuthoredAmount(LivingHurtEvent event) {
        if (event.getSource() instanceof Source source && event.getAmount() != source.getLandedAmount()) {
            event.setAmount(source.getLandedAmount());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        QUEUE.clear();
    }
}
