package xyz.iwolfking.woldsvaults.mixins.vaulthunters.skills.modifications;

import iskallia.vault.skill.ability.effect.spi.AbstractVeinMinerAbility;
import iskallia.vault.skill.ability.effect.spi.core.HoldAbility;
import iskallia.vault.util.BlockBreakHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.util.ChainBreakHandler;
import xyz.iwolfking.woldsvaults.util.ducks.DuckGetRange;

@Mixin(value = AbstractVeinMinerAbility.class, remap = false)
public abstract class MixinAbstractVeinMinerAbility extends HoldAbility implements DuckGetRange {


    @Shadow
    public abstract int getBlockLimit(Player player);

    @Shadow
    protected abstract ItemStack getVeinMiningItemProxy(Player player);

    @Shadow
    public abstract boolean shouldVoid(ServerLevel level, ServerPlayer player, BlockState blockState);


    @Shadow @Mutable @Final
    private BlockBreakHandler blockBreakHandler;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void replaceHandlerInstance(CallbackInfo ci) {
        blockBreakHandler = new ChainBreakHandler() {
            @Override
            protected int getBlockLimit(Player player) {
                return MixinAbstractVeinMinerAbility.this.getBlockLimit(player);
            }

            @Override
            protected ItemStack getMiningItemProxy(Player player) {
                return MixinAbstractVeinMinerAbility.this.getVeinMiningItemProxy(player);
            }

            @Override
            protected boolean shouldVoid(ServerLevel level, ServerPlayer player, BlockState blockState) {
                return MixinAbstractVeinMinerAbility.this.shouldVoid(level, player, blockState);
            }

            @Override
            public int getRange() {
                return MixinAbstractVeinMinerAbility.this.getRange();
            }
        };
    }
}
