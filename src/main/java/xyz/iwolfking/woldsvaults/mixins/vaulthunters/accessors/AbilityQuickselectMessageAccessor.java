package xyz.iwolfking.woldsvaults.mixins.vaulthunters.accessors;

import iskallia.vault.network.message.AbilityQuickselectMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The ability group and the press or release a Vault Hunters quick-fire packet carries. */
@Mixin(value = AbilityQuickselectMessage.class, remap = false)
public interface AbilityQuickselectMessageAccessor {
    @Accessor("abilityName")
    String woldsvaults$getAbilityName();

    @Accessor("action")
    int woldsvaults$getAction();
}
