package xyz.iwolfking.woldsvaults.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.config.GearSetConfig;
import xyz.iwolfking.woldsvaults.items.gear.VaultTridentItem;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDash;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideShockCone;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityRiptide extends Entity {
    @Shadow protected int autoSpinAttackTicks;

    private MixinLivingEntityRiptide(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow protected abstract void doAutoAttackOnTouch(LivingEntity target);

    @Shadow protected abstract void setLivingEntityFlag(int flag, boolean set);

    /**
     * Vault trident riptide. Contacts are found in the swept box grown by the set's contactInflate, so a mob next to
     * the dash's line is hit. With piercing every mob the spin sweeps through is hit once per dash and the dash
     * carries on; without it the first one stops and bounces the dash, as in vanilla. A wall ends the dash only once
     * it has stalled (horizontal speed below stallSpeed) and never in its first launchGraceTicks ticks, so a dash
     * slides along walls and corners in tight rooms. Runs on both sides, since the player's movement is client-driven.
     */
    @Inject(method = "checkAutoSpinAttack", at = @At("HEAD"), cancellable = true)
    private void vaultRiptide(AABB previous, AABB current, CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || !(player.getMainHandItem().getItem() instanceof VaultTridentItem)) {
            return;
        }
        GearSetConfig.Riptide tuning = GearSetHelper.config().getRiptide();
        boolean pierce = RiptideDash.hasPierce(player);
        for (Entity entity : this.level.getEntities(this, previous.minmax(current).inflate(tuning.contactInflate()))) {
            if (!(entity instanceof LivingEntity target) || !RiptideShockCone.isTarget(player, target)) {
                continue;
            }
            if (pierce) {
                if (RiptideDash.markContact(player, target)) {
                    this.doAutoAttackOnTouch(target);
                }
                continue;
            }
            RiptideDash.markContact(player, target);
            this.doAutoAttackOnTouch(target);
            this.autoSpinAttackTicks = 0;
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.2));
            break;
        }
        if (this.horizontalCollision && this.autoSpinAttackTicks > 0) {
            int elapsed = VaultTridentItem.RIPTIDE_SPIN_TICKS - this.autoSpinAttackTicks;
            if (elapsed > tuning.launchGraceTicks() && this.getDeltaMovement().horizontalDistance() < tuning.stallSpeed()) {
                this.autoSpinAttackTicks = 0;
            }
        }
        if (!this.level.isClientSide && this.autoSpinAttackTicks <= 0) {
            this.setLivingEntityFlag(4, false);
        }
        ci.cancel();
    }

    /**
     * Brackets the equipment scan so the base mod's cooldown check there ignores a riptide cooldown: the trident
     * keeps its vanilla attribute modifiers (attack damage, attack speed) through its own riptide cooldown, as it
     * already does in the attribute snapshot.
     */
    @Inject(method = "collectEquipmentChanges", at = @At("HEAD"))
    private void beginEquipmentScan(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        RiptideDash.beginEquipmentScan((LivingEntity) (Object) this);
    }

    @Inject(method = "collectEquipmentChanges", at = @At("RETURN"))
    private void endEquipmentScan(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        RiptideDash.endEquipmentScan();
    }
}
