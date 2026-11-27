package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.idona;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.gear.attribute.custom.ability.ArcaneNovaOnHitAttribute;
import iskallia.vault.util.ArcaneNovaOnHitHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import xyz.iwolfking.woldsvaults.gods.trees.idona.IdonaPowerDump;

/** A Starforge nova that procs while sneaking under Power Dump takes the bonus for the whole bar and spends it. */
@Mixin(value = ArcaneNovaOnHitHelper.class, remap = false)
public class MixinArcaneNovaOnHitHelperPowerDump {

    @WrapMethod(method = "triggerNova")
    private static void woldsvaults$powerDumpNova(ServerPlayer player, LivingEntity target, ArcaneNovaOnHitAttribute attribute,
                                                  Operation<Void> original) {
        IdonaPowerDump.triggerNova(player, () -> original.call(player, target, attribute));
    }
}
