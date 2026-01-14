package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.authlib.GameProfile;
import iskallia.vault.block.entity.UnboxingStationTileEntity;
import iskallia.vault.config.card.BoosterPackConfig;
import iskallia.vault.core.card.Card;
import iskallia.vault.core.random.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.api.lib.IPlayerOutcomeHandler;

import java.util.List;
import java.util.UUID;

@Mixin(value = UnboxingStationTileEntity.class, remap = false)
public abstract class MixinUnboxingStationTileEntity extends BlockEntity {
    @Shadow
    private UUID ownerId;

    public MixinUnboxingStationTileEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Redirect(method = "generateOutcomesForBoosterPack", at = @At(value = "INVOKE", target = "Liskallia/vault/config/card/BoosterPackConfig;getOutcomes(Ljava/lang/String;Liskallia/vault/core/random/RandomSource;)Ljava/util/List;"))
    private List<Card> passPlayerToOutcomes(BoosterPackConfig instance, String id, RandomSource random) {
        if(ownerId != null) {
            if(this.getLevel() instanceof ServerLevel serverLevel) {
                return ((IPlayerOutcomeHandler)instance).woldsVaults$getOutcomes(new FakePlayer(serverLevel, new GameProfile(ownerId, UsernameCache.getLastKnownUsername(ownerId))), id, random);
            }
        }

        return instance.getOutcomes(id, random);
    }
}
