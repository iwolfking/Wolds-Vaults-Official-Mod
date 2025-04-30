package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.core.event.Event;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.vault.player.Runner;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.ars_nouveau.shadowed.eliotlash.mclib.math.functions.classic.Exp;
import xyz.iwolfking.woldsvaults.expertises.SurpriseModifiersExpertise;
import xyz.iwolfking.woldsvaults.modifiers.vault.RemoveBlacklistModifier;
import xyz.iwolfking.woldsvaults.util.VaultModifierUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = Runner.class, remap = false)
public abstract class MixinRunner extends Listener {
    @Inject(method = "lambda$initServer$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isCreative()Z", shift = At.Shift.AFTER, remap = true), cancellable = true)
    private void preventCancelingInteraction(VirtualWorld world, PlayerInteractEvent event, CallbackInfo ci) {
        if(ServerVaults.get(world).isPresent()) {
            Vault vault = ServerVaults.get(world).get();
            List<VaultModifier<?>> modifiers = vault.get(Vault.MODIFIERS).getModifiers();
            for(VaultModifier<?> modifier : modifiers) {
                if(modifier instanceof RemoveBlacklistModifier removeBlacklistModifier) {
                    if(removeBlacklistModifier.properties().shouldUseAsBlacklist() && removeBlacklistModifier.properties().getWhitelist().isEmpty() && removeBlacklistModifier.properties().shouldApplyToItems()) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "lambda$initServer$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isCreative()Z", shift = At.Shift.AFTER), cancellable = true)
    private void preventCancelingInteraction(VirtualWorld world, BlockEvent.EntityPlaceEvent event, CallbackInfo ci) {
        if(ServerVaults.get(world).isPresent()) {
            Vault vault = ServerVaults.get(world).get();
            List<VaultModifier<?>> modifiers = vault.get(Vault.MODIFIERS).getModifiers();
            for(VaultModifier<?> modifier : modifiers) {
                if(modifier instanceof RemoveBlacklistModifier removeBlacklistModifier) {
                    if(removeBlacklistModifier.properties().shouldUseAsBlacklist() && removeBlacklistModifier.properties().getWhitelist().isEmpty() && removeBlacklistModifier.properties().shouldApplyToBlocks()) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "onJoin", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/player/Listener;onJoin(Liskallia/vault/core/world/storage/VirtualWorld;Liskallia/vault/core/vault/Vault;)V"))
    private void addRandomPositiveModifiers(VirtualWorld world, Vault vault, CallbackInfo ci) {
        if(this.getPlayer().isPresent()) {
            ServerPlayer player = this.getPlayer().get();
            ExpertiseTree expertiseTree = PlayerExpertisesData.get(player.server).getExpertises(player);
            int surpriseModifiersExpertiseLevel = 0;
            for(Skill expertise : expertiseTree.skills) {
                if(expertise.getId().equals("Surprise_Favors")) {
                    surpriseModifiersExpertiseLevel = ((LearnableSkill)expertise).getSpentLearnPoints();
                    break;
                }
            }
            if(surpriseModifiersExpertiseLevel > 0) {
                if(world.getRandom().nextFloat() < (surpriseModifiersExpertiseLevel * 0.1F)) {
                    VaultModifierUtils.addModifierFromPool(vault, VaultMod.id("random_positive"));
                }
            }
        }
    }
}
