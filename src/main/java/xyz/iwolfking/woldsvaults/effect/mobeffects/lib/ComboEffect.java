package xyz.iwolfking.woldsvaults.effect.mobeffects.lib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xyz.iwolfking.woldsvaults.modifiers.gear.lib.ComboAttribute;

public class ComboEffect extends MobEffect {

    public ComboEffect(MobEffectCategory category, int color, ResourceLocation id) {
        super(category, color);
        this.setRegistryName(id);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public void increaseCombo(ComboAttribute comboAttributeInstance, LivingEntity target, Player player) {
        int requiredHits = comboAttributeInstance.getRequiredHits() - 1;
        int duration = comboAttributeInstance.getEffectDuration();

        int currentAmplifier = 0;
        if (target.hasEffect(this)) {
            MobEffectInstance existingEffect = target.getEffect(this);
            if (existingEffect != null) {
                currentAmplifier = existingEffect.getAmplifier() + 1;
            }
        }

        if (currentAmplifier >= requiredHits) {
            comboAttributeInstance.triggerComboEffect(player, target, comboAttributeInstance);

            target.removeEffect(this);
        } else {
            target.addEffect(new MobEffectInstance(this, duration, currentAmplifier, false, false, true));
        }
    }
}