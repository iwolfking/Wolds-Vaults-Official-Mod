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
    // event resolves the per-event state once — is this a hyper vault, and which modifier ids
    // already applied their whole additive factor — and every later hook for the same event
    // reuses it. Damage events resolve synchronously on their vault's tick thread, so
    // event-identity ThreadLocals are enough coordination; the event itself is held only
    // WEAKLY so a closed vault world can never be pinned between hits.
    @Unique
    private static final ThreadLocal<java.lang.ref.WeakReference<Object>> woldsVaults$lastEvent = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<Set<ResourceLocation>> woldsVaults$appliedAdditiveIds =
            ThreadLocal.withInitial(HashSet::new);
    @Unique
    private static final ThreadLocal<Boolean> woldsVaults$eventInHyperVault = new ThreadLocal<>();

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
            java.lang.ref.WeakReference<Object> lastRef = woldsVaults$lastEvent.get();
            if(lastRef == null || lastRef.get() != livingDamageEvent) {
                // First hook for this event (all hooks passing the world filter above belong
                // to the same vault): cache the hyper check so the remaining stacks' hooks
                // don't re-scan the objective list on every hit.
                woldsVaults$lastEvent.set(new java.lang.ref.WeakReference<>(livingDamageEvent));
                woldsVaults$appliedAdditiveIds.get().clear();
                woldsVaults$eventInHyperVault.set(
                        !vault.get(Vault.OBJECTIVES).getAll(HyperVaultObjective.class).isEmpty());
            }
            if(!Boolean.TRUE.equals(woldsVaults$eventInHyperVault.get())) {
                livingDamageEvent.setAmount(livingDamageEvent.getAmount() * this.properties().getMaxHealth());
                return;
            }
            // Hyper: stacks add their bonus instead of compounding — x(1 + (factor-1) x stacks).
            if(!woldsVaults$appliedAdditiveIds.get().add(this.getId())) {
                return;
            }
            long stacks = Math.max(1L, VaultModifierUtils.getCountOfModifiers(vault, this.getId()));
            float factor = 1.0F + (this.properties().getMaxHealth() - 1.0F) * stacks;
            livingDamageEvent.setAmount(livingDamageEvent.getAmount() * factor);
        });
    }
}
