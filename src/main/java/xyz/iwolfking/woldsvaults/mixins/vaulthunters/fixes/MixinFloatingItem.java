package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.core.world.storage.IZonedWorld;
import iskallia.vault.core.world.storage.WorldZone;
import iskallia.vault.entity.entity.FloatingItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = FloatingItemEntity.class, remap = false)
public abstract class MixinFloatingItem extends ItemEntity {
    public MixinFloatingItem(EntityType<? extends ItemEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    // make floating items able to be picked up by magnet unless they are in unbreakable vault zone
    @Inject(method = "tick", at = @At("HEAD"), remap = true)
    public void tick(CallbackInfo ci) {
        if (this.getLevel() instanceof ServerLevel sLevel && this.getTags().contains("PreventMagnetMovement")) {
            IZonedWorld proxy = IZonedWorld.of(sLevel).orElse(null);
            if (proxy != null) {
                var pos = this.blockPosition();
                List<WorldZone> zones = proxy.getZones().get(pos);
                if (!zones.isEmpty()) {
                    for (WorldZone zone : zones) {
                        if (zone.canModify() == Boolean.FALSE) {
                            return; // unmodifiable zone => prevent magnet movement (uncompleted raids)
                        }
                    }
                }

            }
            this.removeTag("PreventMagnetMovement");
        }
    }

}
