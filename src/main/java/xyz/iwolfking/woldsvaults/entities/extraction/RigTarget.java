package xyz.iwolfking.woldsvaults.entities.extraction;

import net.minecraft.world.entity.LivingEntity;

/** A rig wave mobs attack: the main extractor or one of its thumpers. */
public interface RigTarget {
    int getSiteIndex();

    /** The pad the rig stands on: 0 is the main pad, 1 and 2 are thumper pads. */
    int getPadIndex();

    /** True while mobs should attack this rig. */
    boolean isRigTargetable();

    /** Damage one battering ram hit deals: shield regen per second plus (max shield + max health) / killSeconds. */
    float ramHitDamage(float killSeconds);

    /** Applies a battering ram hit as true damage: shield first, then hull, ignoring armor and shield toughness. */
    void applyRamHit(LivingEntity ram, float damage);

    default LivingEntity asLiving() {
        return (LivingEntity) this;
    }
}
