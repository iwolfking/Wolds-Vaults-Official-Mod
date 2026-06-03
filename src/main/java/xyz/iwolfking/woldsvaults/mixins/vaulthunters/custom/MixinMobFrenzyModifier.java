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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.GameruleHelper;
import xyz.iwolfking.woldsvaults.init.ModGameRules;

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

    @Inject(method = "initServer", at = @At("TAIL"))
    private void addIncreasedDamageEffect(VirtualWorld world, Vault vault, ModifierContext context, CallbackInfo ci) {
        if(GameruleHelper.isEnabled(ModGameRules.OLD_OVERPOWER_MECHANIC, world)) {
            return;
        }

        CommonEvents.ENTITY_DAMAGE.register(context.getUUID(), livingDamageEvent -> {
            if(world.getLevel().equals(livingDamageEvent.getEntity().getLevel())) {
                if(livingDamageEvent.getSource().getEntity() instanceof ServerPlayer) {
                    livingDamageEvent.setAmount(livingDamageEvent.getAmount() * this.properties().getMaxHealth());
                }
            }
        });
    }
}
