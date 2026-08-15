package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods.tenos;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import iskallia.vault.event.PlayerEvents;
import iskallia.vault.mana.Mana;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.gods.trees.tenos.TenosMana;

/**
 * Mana Starved (r100): mana regeneration ramps from 1x at half mana to 2x at empty.
 *
 * <p>The per-tick regen call is the only writable point in the mana pipeline -
 * {@code CommonEvents.MANA_MODIFY} exposes no setters, and the base and regen attribute base
 * values are rewritten every tick so they cannot be touched either.
 */
@Mixin(value = PlayerEvents.class, remap = false)
public abstract class MixinPlayerEventsManaStarved {
    @ModifyExpressionValue(method = "onManaRegen", at = @At(value = "INVOKE", target = "Liskallia/vault/mana/Mana;getRegenPerSecond(Lnet/minecraft/world/entity/player/Player;)F"))
    private static float woldsvaults$manaStarvedRegen(float regen, TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player) {
            return regen * TenosMana.manaStarvedMultiplier(player);
        }
        return regen;
    }
}
