package xyz.iwolfking.woldsvaults.init;

import iskallia.vault.VaultMod;
import iskallia.vault.etching.EtchingSet;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.etching.IngeniumSet;
import xyz.iwolfking.woldsvaults.etching.ZodSet;

public class ModEtchings {
    public static final ZodSet ZOD = new ZodSet(VaultMod.id("zod"));
    public static final IngeniumSet INGENIUM = new IngeniumSet(WoldsVaults.id("ingenium"));
    public static final EtchingSet.Simple EMPTY = new EtchingSet.Simple(WoldsVaults.id("unidentified"));
}
