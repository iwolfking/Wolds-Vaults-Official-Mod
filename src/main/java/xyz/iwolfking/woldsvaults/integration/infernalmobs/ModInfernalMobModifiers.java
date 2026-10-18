package xyz.iwolfking.woldsvaults.integration.infernalmobs;

import atomicstryker.infernalmobs.common.MobModifier;
import xyz.iwolfking.woldsvaults.integration.infernalmobs.modifiers.*;

import java.util.ArrayList;
import java.util.List;

public class ModInfernalMobModifiers {
    public static final List<Class<? extends MobModifier>> MOB_MODIFIERS = new ArrayList<>();

    static {
        MOB_MODIFIERS.add(MM_Surge.class);
        MOB_MODIFIERS.add(MM_CorruptedAura.class);
        MOB_MODIFIERS.add(MM_Berserk.class);
        MOB_MODIFIERS.add(MM_Exposing.class);
        MOB_MODIFIERS.add(MM_Bloodthirsty.class);
        MOB_MODIFIERS.add(MM_ManaBurn.class);
        MOB_MODIFIERS.add(MM_Immortal.class);
        MOB_MODIFIERS.add(MM_SizeShifting.class);
    }
}
