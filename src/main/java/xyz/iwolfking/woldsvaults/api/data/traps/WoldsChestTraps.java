package xyz.iwolfking.woldsvaults.api.data.traps;

import net.minecraft.world.effect.MobEffects;
import xyz.iwolfking.woldsvaults.effect.traps.MobBuffTrapEffect;
import xyz.iwolfking.woldsvaults.effect.traps.ShockwaveTrapEffect;

import java.util.ArrayList;
import java.util.List;

public class WoldsChestTraps {
    public static final List<ShockwaveTrapEffect> SHOCKWAVE_TRAP_EFFECTS = new ArrayList<>();
    public static final List<MobBuffTrapEffect> MOB_BUFF_TRAP_EFFECTS = new ArrayList<>();

    static {
        SHOCKWAVE_TRAP_EFFECTS.add(new ShockwaveTrapEffect("Shockwave", 3.0F, 3.0F));
        MOB_BUFF_TRAP_EFFECTS.add(new MobBuffTrapEffect("Zoomies", 48.0D, 600, 2, List.of(MobEffects.MOVEMENT_SPEED)));
        MOB_BUFF_TRAP_EFFECTS.add(new MobBuffTrapEffect("Plating", 48.0D, 600, 2, List.of(MobEffects.DAMAGE_RESISTANCE, MobEffects.DAMAGE_BOOST)));
    }
}
