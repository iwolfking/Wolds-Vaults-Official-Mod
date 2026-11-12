package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotCalculator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDash;

import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(value = AttributeSnapshotCalculator.class, remap = false)
public abstract class MixinAttributeSnapshotCalculatorSets {
    /**
     * @author PoorMansPhysicist
     * @reason keep a vault trident counted while its own riptide cooldown runs; swap cooldowns still drop it
     */
    @WrapOperation(method = "computeSnapshot", at = @At(value = "INVOKE",
            target = "Liskallia/vault/snapshot/AttributeSnapshotCalculator;computeGearSnapshot(Ljava/util/function/Function;Ljava/util/function/Predicate;ILiskallia/vault/snapshot/AttributeSnapshot;)V"))
    private static void keepRiptideTrident(Function<EquipmentSlot, ItemStack> equipment, Predicate<Item> onCooldown, int playerLevel, AttributeSnapshot snapshot,
                                           Operation<Void> original, @Local(argsOnly = true) ServerPlayer player) {
        Predicate<Item> cooldownTest = item -> onCooldown.test(item) && !RiptideDash.isRiptideCooldown(player, item);
        original.call(equipment, cooldownTest, playerLevel, snapshot);
    }

    /**
     * @author PoorMansPhysicist
     * @reason add active set-tier grants once the gear and curios walk has counted the pieces
     */
    @Inject(method = "computeSnapshot", at = @At(value = "INVOKE",
            target = "Liskallia/vault/core/vault/modifier/modifier/VaultGearAttributeModifier;applySnapshotModifiers(Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/snapshot/AttributeSnapshot;)V"))
    private static void addSetBonuses(ServerPlayer player, AttributeSnapshot snapshot, CallbackInfo ci) {
        GearSetHelper.addSetBonuses(snapshot);
    }
}
