package xyz.iwolfking.woldsvaults.config;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.init.ModConfigs;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tuning for the Bullet Bill set mechanic, generated as {@code vault_configs/gear/bullet_bill/bullet_bill.json}.
 * Bullet Bill tuning (bullet_bill grant = ticks the form lasts). Double tapping jump turns the wearer into a Bullet
 * Bill, then cooldownTicks from when the form ends before the next. The flight is PneumaticCraft's jet boots with jump
 * as the thrust key. Holding jump flies where the wearer looks at 0.3 blocks a tick per jetUpgrades (the boots'
 * upgrade count, 1 to 5), times jetPower (0 to 1), times a speed multiplier that starts at 1, rises in a dive and
 * falls four times more slowly in a climb (0.8 to 4.2), spooling up over the first second. With jump let go the jets
 * carry on the same way at cruiseSpeed blocks a tick, speed multiplier included, spooling back down over a second;
 * with cruiseSpeed 0 it hovers instead: sinking at 0.1 less 0.02 per jetUpgrades blocks a tick (5 holds its height),
 * or 0.45 while sneaking, with sideways speed dying off on air drag. flightStabilizers (needs jetUpgrades 4 or more)
 * stops the wearer dead on letting go, and whenever no movement key is held; smartHover only hovers after the first
 * thrust, until the wearer lands. Hitting a wall above 2 blocks a tick with the jets running (speed x1.5 on Normal, x2
 * on Hard) hurts like flying into a wall. No fall damage while a Bullet Bill, nor on the landing after.
 * bullet_bill_dash grant = more Bullet Dash damage while a Bullet Bill: the dash also fires the jets for boostTicks at
 * boostSpeed times the speed, whether or not jump is held, with a spin over spinTicks and a red pulse.
 * bullet_bill_detonation grant = detonation damage: right click as a Bullet Bill arms it, and the next time it hits a
 * block going at least detonationMinSpeed blocks a tick it explodes and the form ends, hitting every enemy within
 * detonationRadius plus detonationRadiusPerSpeed per block a tick (at most detonationMaxRadius) for one Bullet Dash
 * hit (attack damage times the learned tier's percent, or detonationNoDashPercent if Bullet Dash isn't learned, with
 * damage multipliers and the bullet_bill_dash bonus) times its speed in blocks a tick times the grant. An armed Bullet
 * Bill flies on past the end of its time until it crashes, or explodes where it is once detonationOvertimeTicks more
 * have gone by. Worn in place of the helmet, the Banzai Bill Helmet makes the Bill banzaiScale times larger and the
 * King Bill Helmet kingScale times, tinted light red; the larger size is the drawn model, Bullet Dash's reach and the
 * detonation's radius (and its cap), not the collision box; giantReachShare is how much of the extra size the reach
 * and radius get (1 = all of it, so 5 times the size is 5 times the reach; 0 = none). A King Bill also burns every
 * enemy within kingHeatRadius every kingHeatIntervalTicks, for kingHeatDamage of a Bullet Dash hit per second.
 */
public class BulletBillConfig extends Config implements WoldsTable {
    private static final AtomicBoolean WARNED = new AtomicBoolean();

    @Expose public int cooldownTicks;
    @Expose public int jetUpgrades;
    @Expose public float jetPower;
    @Expose public boolean flightStabilizers;
    @Expose public boolean smartHover;
    @Expose public int boostTicks;
    @Expose public float boostSpeed;
    @Expose public int spinTicks;
    @Expose public float detonationNoDashPercent;
    @Expose public float detonationMinSpeed;
    @Expose public float detonationRadius;
    @Expose public float detonationRadiusPerSpeed;
    @Expose public float detonationMaxRadius;
    @Expose public int detonationOvertimeTicks;
    @Expose public float banzaiScale;
    @Expose public float kingScale;
    @Expose public float giantReachShare;
    @Expose public float cruiseSpeed;
    @Expose public float kingHeatRadius;
    @Expose public float kingHeatDamage;
    @Expose public int kingHeatIntervalTicks;

    /** The installed table, or null (logged once) until it is installed; callers treat null as the mechanic being off. */
    @Nullable
    public static BulletBillConfig installed() {
        BulletBillConfig config = ModConfigs.BULLET_BILL;
        if (config == null && WARNED.compareAndSet(false, true)) {
            WoldLogger.error("Bullet Bill was used before the gear/bullet_bill/bullet_bill table was installed; it does nothing until it is");
        }
        return config;
    }

    @Override
    public String getName() {
        return "bullet_bill";
    }

    @Override
    protected void reset() {
    }

    @Nullable
    @Override
    public String validate() {
        if (this.jetUpgrades < 1 || this.jetUpgrades > 5) {
            return "jetUpgrades is not between 1 and 5";
        }
        return null;
    }
}
