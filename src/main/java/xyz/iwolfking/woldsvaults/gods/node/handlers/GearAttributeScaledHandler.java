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
 * The shared handler of every node whose whole effect is "adds N to a float gear attribute per
 * point invested". That is the single largest class of god node across all four trees, so this
 * type exists once and every such node is config alone - a new one needs no Java at all.
 *
 * <p>{@code values} is the per-point table and {@code attribute} names the gear attribute the
 * table pays into:
 *
 * <pre>{@code { "handler": "gear_attribute_scaled", "attribute": "the_vault:health_percentile",
 *   "values": [0.25] }}</pre>
 *
 * <p>The total is the sum of the table over the points held, with the last entry repeating, so a
 * one-entry table is simply linear and uncapped in the star count - which is what every shipped
 * stat node does today. Contributing through {@link StatContributor} rather than a vanilla
 * modifier is what makes the value recompute on every snapshot rebuild, show on the stats screen,
 * and vanish the moment the gate stops paying.
 */
public record GearAttributeScaledHandler(GodEffect effect) implements StatContributor {
    /** The type string config binds; registered from {@code GodNodeHandlerTypes}. */
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
