package xyz.iwolfking.woldsvaults.effect.mobeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import xyz.iwolfking.woldsvaults.WoldsVaults;

public class HealBlockEffect extends MobEffect {
    public HealBlockEffect() {
        super(MobEffectCategory.HARMFUL, 0x55FFFF);
        this.setRegistryName(WoldsVaults.id("heal_block"));
    }
}