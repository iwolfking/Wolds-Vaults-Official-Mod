package xyz.iwolfking.woldsvaults.mixins.vaulthunters.objectives;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.VaultMod;
import iskallia.vault.config.BingoConfig;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.crystal.objective.BingoCrystalObjective;
import iskallia.vault.task.BingoTask;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.ObjectiveDifficulty;

import java.util.Optional;

@Mixin(value = BingoCrystalObjective.class, remap = false)
public class MixinBingoCrystalObjective {
    @Inject(method = "configure", at = @At("HEAD"))
    private void woldsvaults$beginDifficulty(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ObjectiveDifficulty.begin(ObjectiveDifficulty.multiplier(sigil, vault));
    }

    @Inject(method = "configure", at = @At("RETURN"))
    private void woldsvaults$endDifficulty(Vault vault, RandomSource random, String sigil, CallbackInfo ci) {
        ObjectiveDifficulty.end();
    }

    /** Every bingo rolls the default card; the vault's difficulty then raises each tile by its class exponent. */
    @WrapOperation(method = "lambda$configure$2", at = @At(value = "INVOKE", target = "Liskallia/vault/config/BingoConfig;generate(Lnet/minecraft/resources/ResourceLocation;I)Ljava/util/Optional;"))
    private Optional<BingoTask> woldsvaults$scaleTiles(BingoConfig config, ResourceLocation pool, int level, Operation<Optional<BingoTask>> original) {
        Optional<BingoTask> task = original.call(config, VaultMod.id("default"), level);
        task.ifPresent(bingo -> ObjectiveDifficulty.scaleBingoTask(bingo, ObjectiveDifficulty.current()));
        return task;
    }
}
