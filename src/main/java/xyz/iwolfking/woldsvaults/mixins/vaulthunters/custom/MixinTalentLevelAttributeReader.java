package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.talent.TalentLevelAttribute;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "iskallia.vault.gear.attribute.talent.TalentLevelAttribute$2", remap = false)
public class MixinTalentLevelAttributeReader {

    @Inject(
        method = "getDisplay",
        at = @At("HEAD"),
        cancellable = true
    )
    private void handleMissingTalent(VaultGearAttributeInstance<TalentLevelAttribute> instance, VaultGearModifier.AffixType type, CallbackInfoReturnable<MutableComponent> cir) {
        TalentLevelAttribute attribute = instance.getValue();
        
        if (attribute.getTalent() != null && attribute.getTalent().equals("all_talents")) {
            return;
        }

        boolean exists = ModConfigs.TALENTS.getTalentById(attribute.getTalent()).isPresent();
        
        if (!exists) {
            MutableComponent fallback = new TextComponent("+")
                    .append(String.valueOf(attribute.getLevelChange()))
                    .append(" to level of Unknown Talent (")
                    .append(attribute.getTalent())
                    .append(")")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.STRIKETHROUGH);
            
            cir.setReturnValue(fallback);
        }
    }
}