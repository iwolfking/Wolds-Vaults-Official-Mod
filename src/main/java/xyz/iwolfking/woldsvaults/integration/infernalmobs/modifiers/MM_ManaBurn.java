package xyz.iwolfking.woldsvaults.integration.infernalmobs.modifiers;

import atomicstryker.infernalmobs.common.MobModifier;
import iskallia.vault.mana.ManaAction;
import iskallia.vault.mana.ManaPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class MM_ManaBurn extends MobModifier {
    private static String[] suffix = new String[]{"theManaEater", "ofManaSucking", "theManaBurner"};
    private static String[] prefix = new String[]{"mana-vampire", "mana-seeking", "mana-hungry"};

    public MM_ManaBurn() {
    }

    public MM_ManaBurn(MobModifier next) {
        super(next);
    }

    public String getModName() {
        return "ManaBurn";
    }

    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity != null) {
            if(entity instanceof ManaPlayer manaPlayer) {
                manaPlayer.decreaseMana(ManaAction.NEGATIVE_GAME_MECHANIC, manaPlayer.getManaMax() * 0.15F);
                entity.hurt(DamageSource.MAGIC, manaPlayer.getManaMax() * 0.05F);
            }
        }

        return super.onAttack(entity, source, damage);
    }

    protected String[] getModNameSuffix() {
        return suffix;
    }

    protected String[] getModNamePrefix() {
        return prefix;
    }
}