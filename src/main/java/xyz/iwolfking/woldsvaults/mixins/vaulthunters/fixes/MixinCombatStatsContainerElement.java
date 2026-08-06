package xyz.iwolfking.woldsvaults.mixins.vaulthunters.fixes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import iskallia.vault.client.gui.screen.summary.element.CombatStatsContainerElement;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.function.Function;

@Mixin(value = CombatStatsContainerElement.class, remap = false)
public class MixinCombatStatsContainerElement {

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/stat/StatCollector;getDamageDealt()Lit/unimi/dsi/fastutil/objects/Object2FloatMap;"))
    private Object2FloatMap<ResourceLocation> woldsVaults$sumDamageDealtInDouble(Object2FloatMap<ResourceLocation> original) {
        return woldsVaults$collapseToFiniteTotal(original, "damage dealt");
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/stat/StatCollector;getDamageReceived()Lit/unimi/dsi/fastutil/objects/Object2FloatMap;"))
    private Object2FloatMap<ResourceLocation> woldsVaults$sumDamageReceivedInDouble(Object2FloatMap<ResourceLocation> original) {
        return woldsVaults$collapseToFiniteTotal(original, "damage received");
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "FIELD", target = "Liskallia/vault/client/gui/screen/summary/element/StatLabelElement;FLOAT_FORMATTER:Ljava/util/function/Function;"))
    private Function<Float, String> woldsVaults$formatLargeDamageValues(Function<Float, String> original) {
        return value -> woldsVaults$formatDamage(value);
    }

    /**
     * Replaces the per-mob damage map with a single-entry map holding the total summed in double
     * precision and clamped to the float range, so the float summation loop in the constructor
     * cannot overflow to Infinity when the per-mob entries are large, and so non-finite entries
     * from snapshots recorded before MixinMobsStat clamped accumulation still display a number.
     */
    @Unique
    private static Object2FloatMap<ResourceLocation> woldsVaults$collapseToFiniteTotal(Object2FloatMap<ResourceLocation> original, String statName) {
        double total = 0.0D;
        boolean nonFinite = false;
        for (Object2FloatMap.Entry<ResourceLocation> entry : original.object2FloatEntrySet()) {
            float value = entry.getFloatValue();
            if (Float.isFinite(value)) {
                total += value;
            } else {
                nonFinite = true;
                total += Float.MAX_VALUE;
            }
        }
        if (nonFinite || total > Float.MAX_VALUE) {
            WoldsVaults.LOGGER.warn("Vault end screen {} total exceeded 32-bit float range (nonFinite {}, double total {}); clamping display to Float.MAX_VALUE", statName, nonFinite, total);
        }
        Object2FloatOpenHashMap<ResourceLocation> collapsed = new Object2FloatOpenHashMap<>();
        collapsed.put(ResourceLocation.tryParse("woldsvaults:damage_total"), (float) Math.min(total, (double) Float.MAX_VALUE));
        return collapsed;
    }

    /**
     * Formats damage totals like the vanilla FLOAT_FORMATTER for everyday values but switches to
     * scientific notation once the plain decimal form no longer fits the stat row, instead of
     * printing a 30+ digit string or the literal "Infinity".
     */
    @Unique
    private static String woldsVaults$formatDamage(Float value) {
        float sanitized;
        if (Float.isFinite(value)) {
            sanitized = value;
        } else {
            WoldsVaults.LOGGER.warn("Vault end screen damage value was non-finite ({}); displaying the float maximum instead", value);
            sanitized = Float.MAX_VALUE;
        }
        return sanitized < 1.0e10F ? String.format("%.01f", sanitized) : String.format("%.3e", sanitized);
    }
}
