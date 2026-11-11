package xyz.iwolfking.woldsvaults.spellcraft.chain;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.EntityChainAttackedEvent;
import iskallia.vault.event.ActiveFlags;
import iskallia.vault.util.calc.ChainHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.cast.CastContext;
import xyz.iwolfking.woldsvaults.api.cast.CastTag;
import xyz.iwolfking.woldsvaults.augments.AugmentType;
import xyz.iwolfking.woldsvaults.augments.cast.CastScope;
import xyz.iwolfking.woldsvaults.augments.effect.DeferredReach;
import xyz.iwolfking.woldsvaults.augments.effect.SpellHits;
import xyz.iwolfking.woldsvaults.spellcraft.gear.GlyphGrants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The Chaining glyph (loadout spec sec. 6.2, 9.5). Every DIRECT own hit of a spell step with an active Chaining glyph
 * (cast-time hits, hits of what the step threw, its Echo and Shotgun copies) starts one chain: at HIGH priority the hurt
 * is marked, so VH's own chain on the same hit stands down, and at LOWEST priority the chain runs with the hit's final
 * amount. Links follow {@link ChainLinks} (VH's range, targets and step, with the etching and Chainlash's prefix) for
 * the player's chain count. Each link re-hurts with the same damage source inside {@code IS_CHAINING_ATTACKING} and the
 * WV chain scope, so it keeps kill credit and on-kill effects but procs nothing, never chains again and respects hurt
 * immunity. There is no 2-tick chain lockout.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class GlyphChains {
    private static final Set<LivingHurtEvent> ORIGINS = Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));

    private GlyphChains() {
    }

    /** Whether this hurt starts a Chaining glyph chain, so VH's own chain must skip it. */
    public static boolean startsChain(LivingHurtEvent event) {
        return ORIGINS.contains(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void markOrigin(LivingHurtEvent event) {
        LivingEntity target = event.getEntityLiving();
        if (target.level.isClientSide() || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (SpellChainDamage.active() || ActiveFlags.IS_CHAINING_ATTACKING.isSet()) {
            return;
        }
        if (chainsFrom(player, event.getSource())) {
            ORIGINS.add(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void runChain(LivingHurtEvent event) {
        if (!ORIGINS.remove(event) || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        float amount = event.getAmount();
        int count = ChainHelper.getChainCount(player);
        if (amount <= 0.0F || count <= 0) {
            return;
        }
        LivingEntity attacked = event.getEntityLiving();
        DamageSource source = event.getSource();
        List<Mob> targets = ChainLinks.targets(attacked.level, attacked, player, source, ChainLinks.range(player), count);
        if (targets.isEmpty()) {
            return;
        }
        float step = ChainLinks.step(player);
        List<Vec3> points = new ArrayList<>();
        points.add(ChainLinks.linkPoint(attacked));
        SpellChainDamage.run(() -> ActiveFlags.IS_CHAINING_ATTACKING.runIfNotSet(() -> {
            float multiplier = step;
            for (Mob mob : targets) {
                Vec3 movement = mob.getDeltaMovement();
                points.add(ChainLinks.linkPoint(mob));
                mob.hurt(source, amount * multiplier);
                mob.setDeltaMovement(movement);
                multiplier *= step;
            }
        }));
        CommonEvents.ENTITY_CHAIN_ATTACKED.invoke(new EntityChainAttackedEvent.Data(player, targets));
        ChainLinks.particles(player, points);
    }

    /**
     * Whether the hit is a DIRECT own hit of a spell step whose glyphs hold an active Chaining: the open cast scope's
     * step, the step that threw the hit's direct entity, or the deferred step whose carrier delivers it.
     */
    private static boolean chainsFrom(ServerPlayer player, DamageSource source) {
        CastScope.Active scope = CastScope.current().filter(active -> active.player() == player).orElse(null);
        if (scope != null) {
            return chaining(scope.modifiers().grants()) && SpellHits.isOwnDirectHit(scope.spec(), source, null);
        }
        CastTag tag = CastContext.tagOf(source.getDirectEntity());
        if (tag != null && tag.glyphs() != null && tag.glyphs().isOwnedBy(player)) {
            return chaining(tag.glyphs().modifiers().grants()) && SpellHits.isOwnDirectHit(tag.glyphs().spec(), source, null);
        }
        DeferredReach.Reach reach = DeferredReach.forHit(source);
        return reach != null && chaining(reach.glyphs().modifiers().grants()) && SpellHits.isOwnDirectHit(reach.glyphs().spec(), source, null);
    }

    private static boolean chaining(@Nullable GlyphGrants grants) {
        return grants != null && grants.has(AugmentType.CHAINING);
    }
}
