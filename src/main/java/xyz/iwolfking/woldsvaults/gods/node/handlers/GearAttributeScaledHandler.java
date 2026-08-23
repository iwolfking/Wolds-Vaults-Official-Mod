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
 * total sums the {@code values} table over the points held, with the last entry repeating.
 */
public record GearAttributeScaledHandler(GodEffect effect) implements StatContributor {
    public static final String TYPE = "gear_attribute_scaled";

    /** The float-valued gear attribute this effect's per-point table pays into. */
    public record Params(ResourceLocation attribute) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (VaultGearAttributeRegistry.getAttribute(this.attribute) == null) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' pays into gear attribute '"
                        + this.attribute + "', which is not registered");
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void contribute(GodNodeContext context, GodStatSink sink) {
        float total = 0.0F;
        for (int point = 0; point < context.points(); point++) {
            total += context.value(point);
        }
        if (total == 0.0F) {
            return;
        }
        ResourceLocation name = this.effect.params(Params.class).attribute();
        VaultGearAttribute<?> attribute = VaultGearAttributeRegistry.getAttribute(name);
        if (attribute == null) {
            WoldsVaults.LOGGER.error("God effect {} pays into gear attribute {}, which is no longer registered; "
                    + "its contribution is dropped.", context.effectId(), name);
            return;
        }
        sink.add((VaultGearAttribute<Float>) attribute, total);
    }
}
