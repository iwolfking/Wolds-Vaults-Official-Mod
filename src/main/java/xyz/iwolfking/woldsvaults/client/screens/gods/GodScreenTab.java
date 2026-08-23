package xyz.iwolfking.woldsvaults.client.screens.gods;

import iskallia.vault.core.vault.influence.VaultGod;

import javax.annotation.Nullable;

/** The pages of the gods tab, in the order their tabs run down the chart's left edge. */
public enum GodScreenTab {
    OVERVIEW(null),
    IDONA(VaultGod.IDONA),
    VELARA(VaultGod.VELARA),
    WENDARR(VaultGod.WENDARR),
    TENOS(VaultGod.TENOS);

    @Nullable
    private final VaultGod god;

    GodScreenTab(@Nullable VaultGod god) {
        this.god = god;
    }

    @Nullable
    public VaultGod god() {
        return this.god;
    }

    public boolean isOverview() {
        return this.god == null;
    }

    public static GodScreenTab of(VaultGod god) {
        for (GodScreenTab tab : values()) {
            if (tab.god == god) {
                return tab;
            }
        }
        throw new IllegalArgumentException("No gods tab page for " + god);
    }
}
