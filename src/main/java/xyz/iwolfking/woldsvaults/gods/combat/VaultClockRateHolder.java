package xyz.iwolfking.woldsvaults.gods.combat;

/** Implemented on the base mod's TickClock by mixin, so each clock carries its own rate state. */
public interface VaultClockRateHolder {
    VaultClockRate.State woldsvaults$getClockRate();
}
