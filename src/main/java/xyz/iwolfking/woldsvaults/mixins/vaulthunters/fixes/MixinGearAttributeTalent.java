package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import iskallia.vault.gear.VaultGearHelper;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.skill.base.SkillContext;
import iskallia.vault.skill.talent.type.GearAttributeTalent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(value = GearAttributeTalent.class, remap = false)
public abstract class MixinGearAttributeTalent {

    /**
     * Strips this talent's vanilla attribute modifiers when it is unlearned.
     * Once a talent is no longer present, getGearAttributes returns an empty stream, so the
     * onRemoveModifiers call in onTick can never resolve the modifiers it is supposed to remove.
     * Any vanilla-bound talent (Speed, Strength, Lunge, Medic) therefore keeps its transient
     * modifier until relog, letting tier walks stack every traversed tier's buff at once.
     */
    @Inject(method = "onRemove", at = @At("HEAD"))
    private void woldsVaults$removeAttributeModifiers(SkillContext context, CallbackInfo ci) {
        GearAttributeTalent self = (GearAttributeTalent) (Object) this;
        context.getSource().as(ServerPlayer.class).ifPresent(player ->
            VaultGearHelper.getModifiers(self.getUuid(), Stream.of(VaultGearAttributeInstance.cast(self.getAttribute(), self.getValue())))
                .forEach((attribute, modifier) -> {
                    AttributeInstance instance = player.getAttribute(attribute);
                    if (instance != null) {
                        instance.removeModifier(modifier.getId());
                    }
                }));
    }
}
