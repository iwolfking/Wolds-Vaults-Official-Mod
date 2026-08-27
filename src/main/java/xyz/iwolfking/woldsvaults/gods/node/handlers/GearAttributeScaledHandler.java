package xyz.iwolfking.woldsvaults.gods.node.handlers;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodEffectParams;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodStatSink;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;
import xyz.iwolfking.woldsvaults.gods.node.StatContributor;

/**
 * The shared handler of every node that adds N to a float gear attribute per point invested. The
 * total sums the {@code values} table over the points held, with the last entry repeating. An effect
 * may name a second attribute, which is paid the same total - that is what the constellation start
 * nodes use to hand out a pair of stats.
 */
public record GearAttributeScaledHandler(GodEffect effect) implements StatContributor {
    public static final String TYPE = "gear_attribute_scaled";

    /**
     * The float-valued gear attributes this effect's per-point table pays into. {@code secondAttribute}
     * is optional and receives the same total as {@code attribute}.
     */
    public record Params(ResourceLocation attribute,
                         @GodEffectParams.Optional ResourceLocation secondAttribute)
            implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            requireRegistered(effectId, this.attribute);
            if (this.secondAttribute != null) {
                requireRegistered(effectId, this.secondAttribute);
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
        float total = 0.0F;
        for (int point = 0; point < context.points(); point++) {
            total += context.value(point);
        }
        if (total == 0.0F) {
            return;
        }
        Params params = this.effect.params(Params.class);
        pay(context, sink, params.attribute(), total);
        if (params.secondAttribute() != null) {
            pay(context, sink, params.secondAttribute(), total);
        }
    }

    @SuppressWarnings("unchecked")
    private static void pay(GodNodeContext context, GodStatSink sink, ResourceLocation name, float total) {
        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(name);
        if (attribute == null) {
            WoldsVaults.LOGGER.error("God effect {} pays into gear attribute {}, which is no longer registered; "
                    + "its contribution is dropped.", context.effectId(), name);
            return;
        }
        sink.add((VaultGearAttribute<Float>) attribute, total);
    }
}
