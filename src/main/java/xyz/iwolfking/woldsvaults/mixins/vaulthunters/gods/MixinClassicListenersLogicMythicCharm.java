package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.core.vault.player.ClassicListenersLogic;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.WoldsVaults;

@Mixin(value = ClassicListenersLogic.class, remap = false)
public abstract class MixinClassicListenersLogicMythicCharm {
    private static final ResourceLocation MYTHIC_CHARM_ID = WoldsVaults.id("mythic_vault_charm");
    private static final ResourceLocation BASE_CHARM_ID = new ResourceLocation("the_vault", "vault_god_charm");

    /**
     * @author PoorMansPhysicist
     * @reason the Immortal Charms prestige power is configured against the base god charm's item
     * id and the free-use gate compares ids exactly, so mythic charms would burn uses through it.
     * Substituting the base charm id for mythic stacks makes every charm-targeting no-uses power
     * cover mythics too.
     */
    @ModifyVariable(method = "shouldAddFree", at = @At("STORE"), ordinal = 0)
    private ResourceLocation woldsVaults$mythicSharesCharmImmortality(ResourceLocation stackId) {
        return MYTHIC_CHARM_ID.equals(stackId) ? BASE_CHARM_ID : stackId;
    }
}
