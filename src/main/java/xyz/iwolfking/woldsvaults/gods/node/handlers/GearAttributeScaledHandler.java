package xyz.iwolfking.woldsvaults.gods.node.handlers;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodEffectParams;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodStatSink;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;
import xyz.iwolfking.woldsvaults.gods.node.StatContributor;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/**
 * The shared handler of every node that adds N to a float gear attribute per point invested. The
 * total sums the {@code values} table over the points held, with the last entry repeating. An effect
 * may name a second attribute, which is paid the same total, or the total of its own
 * {@code secondValues} table when it has one - that is what the constellation start nodes use to
 * hand out a pair of stats.
 */
public record GearAttributeScaledHandler(GodEffect effect) implements StatContributor {
    public static final String TYPE = "gear_attribute_scaled";

    /**
     * The float-valued gear attributes this effect's per-point table pays into. {@code secondAttribute}
     * is optional and receives the same total as {@code attribute}, unless the optional
     * {@code secondValues} per-point table gives it its own.
     */
    public record Params(ResourceLocation attribute,
                         @GodEffectParams.Optional ResourceLocation secondAttribute,
                         @GodEffectParams.Optional float[] secondValues)
            implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            requireRegistered(effectId, this.attribute);
            if (this.secondAttribute != null) {
                requireRegistered(effectId, this.secondAttribute);
            }
            if (this.secondValues != null && (this.secondAttribute == null || this.secondValues.length == 0)) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has a secondValues table without "
                        + "a secondAttribute to pay, or the table is empty");
            }
        }

        private static void requireRegistered(String effectId, ResourceLocation attribute) {
            if (VaultGearAttributeRegistry.getAttribute(attribute) == null) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' pays into gear attribute '"
                        + attribute + "', which is not registered");
            }
        }
    }

    @Override
    public void contribute(GodNodeContext context, GodStatSink sink) {
        Params params = this.effect.params(Params.class);
        float[] secondValues = params.secondValues();
        float total = 0.0F;
        float secondTotal = 0.0F;
        for (int point = 0; point < context.points(); point++) {
            total += context.value(point);
            if (secondValues != null) {
                secondTotal += secondValues[Math.min(point, secondValues.length - 1)] * context.scale();
            }
        }
        if (total != 0.0F) {
            pay(context, sink, params.attribute(), total);
        }
        if (params.secondAttribute() != null) {
            float paid = secondValues != null ? secondTotal : total;
            if (paid != 0.0F) {
                pay(context, sink, params.secondAttribute(), paid);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void pay(GodNodeContext context, GodStatSink sink, ResourceLocation name, float total) {
        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(name);
        if (attribute == null) {
            WoldLogger.error("God effect {} pays into gear attribute {}, which is no longer registered; "
                    + "its contribution is dropped.", context.effectId(), name);
            return;
        }
        sink.add((VaultGearAttribute<Float>) attribute, total);
    }
}
