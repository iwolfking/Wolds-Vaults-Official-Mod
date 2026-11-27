package xyz.iwolfking.woldsvaults.extraction.jobs;

import iskallia.vault.item.crystal.CrystalData;

/** Marks a vault crystal made by the Ancient Crystals extraction job; such crystals refuse maps and sigils. */
public interface AncientCrystalFlag {
    boolean woldsvaults$isAncient();

    void woldsvaults$setAncient(boolean ancient);

    static boolean isAncient(CrystalData data) {
        return data instanceof AncientCrystalFlag flag && flag.woldsvaults$isAncient();
    }
}
