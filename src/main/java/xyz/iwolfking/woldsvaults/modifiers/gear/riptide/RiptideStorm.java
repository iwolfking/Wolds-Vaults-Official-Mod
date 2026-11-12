package xyz.iwolfking.woldsvaults.modifiers.gear.riptide;

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;
import xyz.iwolfking.woldsvaults.init.ModEffects;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.network.message.RiptideFxMessage;

/**
 * The riptide storm: an arcane crackle around the player while dashing, a lightning arc from the player to every
 * mob the dash touches for {@link ModGearAttributes#RIPTIDE_BOLT} times the contact's final damage ({@link ModGearAttributes#RIPTIDE_BOLT_CHILLED}
 * against chilled or frozen mobs), less damage taken while the spin is active, and a hover after the dash.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public class RiptideStorm implements RiptideDash.Listener {
    private static final float MAX_REDUCTION = 0.95F;

    static {
        FinalDamageStage.register(WoldsVaults.id("riptide_storm_reduction"), FinalDamageStage.ORDER_REDUCTION, (event, amount) -> {
            if (!(event.getEntityLiving() instanceof ServerPlayer player) || !player.isAutoSpinAttack()) {
                return amount;
            }
            float reduction = RiptideDash.snapshot(player).getAttributeValue(ModGearAttributes.RIPTIDE_BOOST_DAMAGE_REDUCTION, VaultGearAttributeTypeMerger.floatSum());
            if (reduction <= 0.0F) {
                return amount;
            }
            return amount * (1.0F - Math.min(MAX_REDUCTION, reduction));
        });
    }

    @Override
    public void onStart(ServerPlayer player, RiptideDash.Dash dash) {
        endHover(player);
    }

    @Override
    public void onTick(ServerPlayer player, RiptideDash.Dash dash) {
        if (bolt(RiptideDash.snapshot(player)) <= 0.0F) {
            return;
        }
        RiptideFxMessage.send(player, RiptideFxMessage.aura(player));
        if (dash.getTicks() % 6 == 1) {
            player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.35F, 1.6F);
        }
    }

    @Override
    public void onContact(ServerPlayer player, RiptideDash.Dash dash, LivingEntity target, float finalDamage) {
        strike(player, dash, target, finalDamage);
    }

    /**
     * Strikes {@code target} with the storm's lightning for its bolt multiplier times {@code reference}, at most once
     * per mob per dash: mobs the dash touches (their own contact damage) and mobs the shock cone hits (the dash's
     * contact damage).
     */
    public static void strike(ServerPlayer player, RiptideDash.Dash dash, LivingEntity target, float reference) {
        AttributeSnapshot snapshot = RiptideDash.snapshot(player);
        float multiplier = isChilled(target)
                ? snapshot.getAttributeValue(ModGearAttributes.RIPTIDE_BOLT_CHILLED, VaultGearAttributeTypeMerger.floatSum())
                : bolt(snapshot);
        if (multiplier <= 0.0F || reference <= 0.0F || !dash.markStruck(target)) {
            return;
        }
        RiptideDerivedHits.queue(player, target, multiplier * reference);
        boolean chilled = isChilled(target);
        if (dash.countVisualBolt() <= GearSetHelper.config().getRiptide().stormVisualBoltsPerDash) {
            RiptideFxMessage.send(player, RiptideFxMessage.arc(player, target, chilled));
            player.getLevel().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.7F, 1.35F + player.getRandom().nextFloat() * 0.3F);
        } else {
            RiptideFxMessage.send(player, RiptideFxMessage.burst(player, target, chilled));
        }
    }

    @Override
    public void onEnd(ServerPlayer player, RiptideDash.Dash dash) {
        int hover = RiptideDash.snapshot(player).getAttributeValue(ModGearAttributes.RIPTIDE_HOVER, VaultGearAttributeTypeMerger.intSum());
        if (hover > 0 && !player.isOnGround()) {
            player.addEffect(new MobEffectInstance(ModEffects.TIDAL_HOVER, hover, 0, false, false, true));
            RiptideFxMessage.send(player, RiptideFxMessage.hover(player, hover));
        }
    }

    @SubscribeEvent
    public static void cancelHoverOnSneak(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player
                && player.isShiftKeyDown()) {
            endHover(player);
        }
    }

    private static void endHover(ServerPlayer player) {
        if (player.removeEffect(ModEffects.TIDAL_HOVER)) {
            RiptideFxMessage.send(player, RiptideFxMessage.hover(player, 0));
        }
    }

    private static float bolt(AttributeSnapshot snapshot) {
        return snapshot.getAttributeValue(ModGearAttributes.RIPTIDE_BOLT, VaultGearAttributeTypeMerger.floatSum());
    }

    static boolean isChilled(LivingEntity target) {
        return target.hasEffect(iskallia.vault.init.ModEffects.CHILLED)
                || target.hasEffect(iskallia.vault.init.ModEffects.FREEZE)
                || target.isFullyFrozen();
    }
}
