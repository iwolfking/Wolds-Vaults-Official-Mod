package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.entity.entity.VaultThrownJavelin;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.lib.SplittingJavelin;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

@Mixin(VaultThrownJavelin.class)
public abstract class MixinVaultThrownJavelin implements SplittingJavelin {
    @Unique
    private boolean woldsvaults$preSplit = false;

    @Override
    public boolean woldsvaults$hasPreSplit() {
        return this.woldsvaults$preSplit;
    }

    @Override
    public void woldsvaults$setPreSplit(boolean preSplit) {
        this.woldsvaults$preSplit = preSplit;
    }

    /**
     * With {@code splitting_javelins}, the first wall hit produces {@code getNumberOfJavelins()} scatter
     * javelins at bounce count zero instead of ricochets. Copies are flagged, so a throw splits once.
     */
    @Redirect(method = "onHitBlock", at = @At(value = "INVOKE", target = "Liskallia/vault/entity/entity/VaultThrownJavelin;ricochet(Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/world/level/Level;)V", remap = false))
    private void woldsvaults$preSplitScatter(VaultThrownJavelin javelin, Vec3 normal, int numRicochets, Level world) {
        int splitCount = woldsvaults$getSplitCount(javelin);
        if (splitCount > 1 && javelin instanceof SplittingJavelin marker && !marker.woldsvaults$hasPreSplit()) {
            woldsvaults$spawnSplit(javelin, normal, splitCount, world);
            return;
        }
        javelin.ricochet(normal, numRicochets, world);
    }

    @Unique
    private static int woldsvaults$getSplitCount(VaultThrownJavelin javelin) {
        Player thrower = javelin.getThrower();
        if (thrower == null || !AttributeSnapshotHelper.canHaveSnapshot(thrower)) {
            return 0;
        }
        return AttributeSnapshotHelper.getInstance().getSnapshot(thrower).getAttributeValue(ModGearAttributes.SPLITTING_JAVELINS, VaultGearAttributeTypeMerger.intSum());
    }

    /** Mirrors {@code VaultThrownJavelin#ricochet}'s spread, leaving the spawned javelins at bounce zero. */
    @Unique
    private static void woldsvaults$spawnSplit(VaultThrownJavelin source, Vec3 normal, int splitCount, Level world) {
        Player thrower = source.getThrower();
        if (thrower == null) {
            return;
        }
        Vec3 motion = source.prevDeltaMovement;
        for (int i = 0; i < splitCount; ++i) {
            double dot = motion.dot(normal) * 1.5;
            Vec3 reflect = motion.subtract(normal.multiply(dot, dot, dot)).add(0.0, 0.15F, 0.0);
            float angle = (float) i / (float) splitCount * 360.0F;
            Vec3 direction = new Vec3(Math.cos(Math.toRadians(angle)) / 5.0, 0.15F, Math.sin(Math.toRadians(angle)) / 5.0).normalize();
            float pitch = (float) (0.15F * (Math.random() - 0.5)) * 2.0F;
            float yaw = (float) (0.15F * (Math.random() - 0.5)) * 2.0F;
            float roll = (float) (0.15F * (Math.random() - 0.5)) * 2.0F;
            Vec3 result = direction.scale(0.5).add(reflect).normalize();
            result = result.xRot(pitch).yRot(yaw).zRot(Math.abs(roll));

            VaultThrownJavelin split = source.createBouncingJavelin(world, (LivingEntity) thrower, 0);
            if (split == null) {
                return;
            }
            split.setPos(source.getX() + result.x / 5.0, source.getY() + result.y / 5.0, source.getZ() + result.z / 5.0);
            split.setDeltaMovement(result);
            double horizontal = result.horizontalDistance();
            float xRot = (float) (Mth.atan2(result.y, horizontal) * 57.2957763671875);
            float yRot = (float) (Mth.atan2(result.x, result.z) * 57.2957763671875);
            split.xRotO = xRot;
            split.yRotO = yRot;
            split.setXRot(xRot);
            split.setYRot(yRot);
            split.setType(source.getJavelinType().ordinal());
            split.pickup = AbstractArrow.Pickup.DISALLOWED;
            split.tickCount = source.tickCount;
            ((SplittingJavelin) split).woldsvaults$setPreSplit(true);
            world.addFreshEntity(split);
        }
    }
}
