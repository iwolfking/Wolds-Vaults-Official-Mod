package xyz.iwolfking.woldsvaults.api.data;

import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import iskallia.vault.init.ModEffects;
import iskallia.vault.util.data.WeightedList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class HexEffects {
    public static WeightedList<MobEffectInstance> HEX_EFFECTS = new WeightedList<>();

    static {
        HEX_EFFECTS.add(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), 4);
        HEX_EFFECTS.add(new MobEffectInstance(MobEffects.POISON, 100, 1), 4);
        HEX_EFFECTS.add(new MobEffectInstance(MobEffects.WITHER, 80, 1), 4);
        HEX_EFFECTS.add(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1), 4);
        HEX_EFFECTS.add(new MobEffectInstance(MobEffects.UNLUCK, 100, 1), 4);
        HEX_EFFECTS.add(new MobEffectInstance(ModEffects.BLEED, 100, 0), 4);
        HEX_EFFECTS.add(new MobEffectInstance(ModEffects.CHILLED, 80, 0), 4);
        HEX_EFFECTS.add(new MobEffectInstance(ModEffects.VULNERABLE, 80, 0), 4);
        HEX_EFFECTS.add(new MobEffectInstance(AMEffectRegistry.FEAR, 60, 0), 4);
    }
}
