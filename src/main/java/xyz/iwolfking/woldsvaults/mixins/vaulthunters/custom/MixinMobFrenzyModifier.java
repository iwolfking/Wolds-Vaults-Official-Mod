package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.Event;
import iskallia.vault.core.event.common.EntityTickEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.modifier.MobFrenzyModifier;
import iskallia.vault.core.vault.modifier.spi.ModifierContext;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.api.util.VaultModifierUtils;
import xyz.iwolfking.woldsvaults.init.ModGameRules;
import xyz.iwolfking.woldsvaults.objectives.HyperVaultObjective;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(value = MobFrenzyModifier.class, remap = false)
public abstract class MixinMobFrenzyModifier extends VaultModifier<MobFrenzyModifier.Properties> {
    public MixinMobFrenzyModifier(ResourceLocation id, MobFrenzyModifier.Properties properties, Display display) {
        super(id, properties, display);
    }

    @WrapOperation(method = "initServer", at = @At(value = "INVOKE", target = "Liskallia/vault/core/event/common/EntityTickEvent;register(Ljava/lang/Object;Ljava/util/function/Consumer;)Liskallia/vault/core/event/Event;"))
    private Event dontRegisterTickMethod(EntityTickEvent instance, Object o, Consumer consumer, Operation<Event> original, @Local(argsOnly = true) VirtualWorld world) {
        if(GameruleHelper.isEnabled(ModGameRules.OLD_OVERPOWER_MECHANIC, world)) {
            return original.call(instance, o, consumer);
        }

        return instance;
    }

    // In hyper vaults the frenzy-family bonus must not compound (every stack registers its
    // own hook below, so N stacks would multiply x factor^N). The first hook to see a damage
    // event applies the whole additive factor for its modifier id and the rest skip; damage
    // events resolve synchronously on the server thread, so an event-identity ThreadLocal is
    // enough coordination.
    @Unique
    private static final ThreadLocal<Object> woldsVaults$lastAdditiveEvent = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<Set<ResourceLocation>> woldsVaults$appliedAdditiveIds =
            ThreadLocal.withInitial(HashSet::new);

    @Inject(method = "initServer", at = @At("TAIL"))
    private void addIncreasedDamageEffect(VirtualWorld world, Vault vault, ModifierContext context, CallbackInfo ci) {
        if(GameruleHelper.isEnabled(ModGameRules.OLD_OVERPOWER_MECHANIC, world)) {
            return;
        }

        CommonEvents.ENTITY_DAMAGE.register(context.getUUID(), livingDamageEvent -> {
            if(!world.getLevel().equals(livingDamageEvent.getEntity().getLevel())) {
                return;
            }
            if(!(livingDamageEvent.getSource().getEntity() instanceof ServerPlayer)) {
                return;
            }
            if(vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty()) {
                livingDamageEvent.setAmount(livingDamageEvent.getAmount() * this.properties().getMaxHealth());
                return;
            }
            // Hyper: stacks add their bonus instead of compounding — x(1 + (factor-1) x stacks).
            if(woldsVaults$lastAdditiveEvent.get() != livingDamageEvent) {
                woldsVaults$lastAdditiveEvent.set(livingDamageEvent);
                woldsVaults$appliedAdditiveIds.get().clear();
            }
            if(!woldsVaults$appliedAdditiveIds.get().add(this.getId())) {
                return;
            }
            long stacks = Math.max(1L, VaultModifierUtils.getCountOfModifiers(vault, this.getId()));
            float factor = 1.0F + (this.properties().getMaxHealth() - 1.0F) * stacks;
            livingDamageEvent.setAmount(livingDamageEvent.getAmount() * factor);
        });
    }
}
