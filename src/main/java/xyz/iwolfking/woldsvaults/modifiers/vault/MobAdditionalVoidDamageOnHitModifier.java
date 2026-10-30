package xyz.iwolfking.woldsvaults.modifiers.vault;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import xyz.iwolfking.woldsvaults.modifiers.vault.lib.MobAdditionalDamageOnHitModifier;

public class MobAdditionalVoidDamageOnHitModifier extends MobAdditionalDamageOnHitModifier {
    public MobAdditionalVoidDamageOnHitModifier(ResourceLocation id, MobAdditionalDamageOnHitModifier.Properties properties, Display display) {
        super(id, properties, display, DamageSource.OUT_OF_WORLD);
        this.setDescriptionFormatter((t, p, s) -> t.formatted((int)Math.abs(p.getOnHitApplyChance() * (float)s * 100.0F)));
    }
}
