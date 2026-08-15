package xyz.iwolfking.woldsvaults.gods.combat;

/**
 * Implemented on the base mod's TickClock by mixin so each vault clock carries its own rate
 * factors and accumulator without a lookup table keyed by vault id.
 */
public interface VaultClockRateHolder {
    VaultClockRate.State woldsvaults$getClockRate();
}
