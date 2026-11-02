package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.mojang.authlib.GameProfile;
import iskallia.vault.world.data.ServerVaults;
import iskallia.vault.entity.entity.SpiritEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = SpiritEntity.class, remap = false)
public abstract class MixinSpiritEntity {
    @Shadow
    private UUID heroId;

    @Shadow
    public Optional<GameProfile> getGameProfile() {
        return Optional.empty();
    }

    @Inject(method = "teleportOut", at = @At("HEAD"))
    private void woldsVaults$creditRescuingPlayerOnSpiritTransport(CallbackInfo ci) {
        if (this.heroId == null) {
            Entity vehicle = (Entity) (Object) this;
            vehicle = vehicle.getVehicle();
            if (vehicle instanceof ServerPlayer player && ServerVaults.get(vehicle.level).isPresent()) {
                Optional<GameProfile> gameProfile = this.getGameProfile();
                if (gameProfile.isPresent()
                        && !gameProfile.get().getId().equals(player.getUUID())) {
                    this.heroId = player.getUUID();
                }
            }
        }
    }
}