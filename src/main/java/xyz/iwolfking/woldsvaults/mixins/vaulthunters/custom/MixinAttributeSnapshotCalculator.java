package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotCalculator;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.integration.bettercombat.DualWieldSnapshotComputer;

import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(value = AttributeSnapshotCalculator.class, remap = false)
public abstract class MixinAttributeSnapshotCalculator {
    @WrapOperation(
            method = "computeSnapshot",
            at = @At(
                    value = "INVOKE",
                    target = "Liskallia/vault/snapshot/AttributeSnapshotCalculator;computeGearSnapshot(Ljava/util/function/Function;Ljava/util/function/Predicate;ILiskallia/vault/snapshot/AttributeSnapshot;)V"
            )
    )
    private static void woldsvaults$overrideWeapon(
            Function<EquipmentSlot, ItemStack> equipmentFn,
            Predicate<Item> isItemOnCooldown,
            int playerLevel,
            AttributeSnapshot snapshot,
            Operation<Void> original
    ) {
        ItemStack weapon = DualWieldSnapshotComputer.getWeaponOverride();

        if (weapon == null) {
            original.call(equipmentFn, isItemOnCooldown, playerLevel, snapshot);
            return;
        }

        Function<EquipmentSlot, ItemStack> overriddenEquipment = slot -> {
            if (slot == EquipmentSlot.MAINHAND) {
                return weapon;
            }
            if (slot == EquipmentSlot.OFFHAND) {
                return ItemStack.EMPTY;
            }
            return equipmentFn.apply(slot);
        };

        original.call(
                overriddenEquipment,
                isItemOnCooldown,
                playerLevel,
                snapshot
        );
    }
}