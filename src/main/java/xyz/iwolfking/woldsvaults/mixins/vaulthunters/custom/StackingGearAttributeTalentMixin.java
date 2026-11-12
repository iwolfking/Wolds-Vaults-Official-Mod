package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.skill.talent.type.StackingGearAttributeTalent;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.server.level.ServerPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaStackTuning;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;

@Mixin(value = StackingGearAttributeTalent.class, remap = false)
public abstract class StackingGearAttributeTalentMixin {

    @Shadow
    private int maxStacks;

    @Redirect(
        method = "onStack",
        at = @At(
                value = "FIELD",
                target = "Liskallia/vault/skill/talent/type/StackingGearAttributeTalent;maxStacks:I",
                opcode = Opcodes.GETFIELD)
    )
    private int getDynamicMaxStacks(StackingGearAttributeTalent instance, @Local(argsOnly = true) ServerPlayer player) {
        int baseMaxStacks = maxStacks;

        int maxStacksBonus = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.ADDITIONAL_STACKING_STACKS, VaultGearAttributeTypeMerger.intSum());

        return IdonaStackTuning.maxStacks(player, Math.max(0, baseMaxStacks + maxStacksBonus));
    }
}