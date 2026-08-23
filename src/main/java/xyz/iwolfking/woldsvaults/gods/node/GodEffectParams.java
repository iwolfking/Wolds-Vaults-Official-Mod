package xyz.iwolfking.woldsvaults.gods.node;

/**
 * Typed configuration for one handler type. Implementations are records registered alongside their
 * handler in {@link GodNodeHandlers} and decoded by {@link GodEffectParamsCodec}.
 */
public interface GodEffectParams {
    /** Load-time range and cross-field checks; throw {@link GodTreeConfigException} naming the field. */
    default void validate(String effectId) {
    }
}
