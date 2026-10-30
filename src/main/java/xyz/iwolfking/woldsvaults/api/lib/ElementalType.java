package xyz.iwolfking.woldsvaults.api.lib;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.init.ModEffects;
import mcjty.lib.varia.TriConsumer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import xyz.iwolfking.woldsvaults.api.util.ElementHelper;
import xyz.iwolfking.woldsvaults.effect.mobeffects.PercentBurnEffect;
import xyz.iwolfking.woldsvaults.events.WoldActiveFlags;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

public enum ElementalType {
    FIRE(ModGearAttributes.FIRE_ELEMENT, WoldActiveFlags.FIRE_ELEMENT_ATTACK, (target, attacker, damage) -> PercentBurnEffect.applyPercentBurn(target, attacker, 200, damage.doubleValue())),
    POISON(ModGearAttributes.POISON_ELEMENT, WoldActiveFlags.POISON_ELEMENT_ATTACK, (target, attacker, level) -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 140, level.intValue()))),
    LIGHTNING(ModGearAttributes.LIGHTNING_ELEMENT, WoldActiveFlags.LIGHTNING_ELEMENT_ATTACK, ElementHelper::triggerLightningChain),
    ICE(ModGearAttributes.ICE_ELEMENT, WoldActiveFlags.ICE_ELEMENT_ATTACK, (target, attacker, level) -> target.addEffect(new MobEffectInstance(ModEffects.CHILLED, 100, level.intValue())));

    private final VaultGearAttribute<Boolean> attribute;
    private final WoldActiveFlags flag;
    private final TriConsumer<LivingEntity, LivingEntity, Number> method;
    private final int color;

    ElementalType(VaultGearAttribute<Boolean> associatedGearAttribute, WoldActiveFlags associatedFlag, TriConsumer<LivingEntity, LivingEntity, Number> method) {
        this.attribute = associatedGearAttribute;
        this.flag = associatedFlag;
        this.method = method;
        this.color = attribute.getReader().getRgbColor();
    }

    public VaultGearAttribute<Boolean> getAttribute() {
        return attribute;
    }

    public WoldActiveFlags getFlag() {
        return flag;
    }

    public void applyEffect(LivingEntity target, LivingEntity attacker, Number effectValue) {
        this.method.accept(target, attacker, effectValue);
    }

    public int getColor() {
        return this.color;
    }
}
