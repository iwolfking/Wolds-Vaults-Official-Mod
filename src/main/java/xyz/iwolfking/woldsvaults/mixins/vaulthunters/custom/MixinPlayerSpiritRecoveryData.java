package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.authlib.GameProfile;
import iskallia.vault.block.entity.SpiritExtractorTileEntity;
import iskallia.vault.entity.IPlayerSkinHolder;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.world.data.PlayerExpertisesData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.iwolfking.woldsvaults.expertises.GraveInsurance;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = SpiritExtractorTileEntity.class, remap = false)
public abstract class MixinPlayerSpiritRecoveryData extends BlockEntity implements IPlayerSkinHolder {
    @Shadow
    @Nullable
    private GameProfile gameProfile;

    public MixinPlayerSpiritRecoveryData(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @ModifyArg(method = "recalculateCost", at = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/SpiritExtractorTileEntity$RecoveryCost;calculate(FILjava/util/List;Liskallia/vault/world/data/InventorySnapshot;FF)V", ordinal = 0), index = 5)
    private float modifyCost(float multiplier) {
        if(this.level instanceof ServerLevel serverLevel) {
            if(this.gameProfile != null) {
                List<GraveInsurance> graveInsurances = PlayerExpertisesData.get(serverLevel).getExpertises(this.gameProfile.getId()).getAll(GraveInsurance.class, Skill::isUnlocked);
                for(GraveInsurance insurance : graveInsurances) {
                    multiplier += insurance.getCostReduction();
                }
            }
        }
        return multiplier;
    }
}
