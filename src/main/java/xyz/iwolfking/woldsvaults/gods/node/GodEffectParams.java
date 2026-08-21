package xyz.iwolfking.woldsvaults.gods.node;

/**
 * Typed configuration for one handler type. Implementations are records registered alongside
 * their handler in {@link GodNodeHandlers}; {@link GodEffectParamsCodec} decodes them from the
 * effect's config object, so a malformed value fails at load naming the field rather than at
 * first use inside a vault.
 */
public interface GodEffectParams {
    /**
     * Range and cross-field checks the codec cannot infer from the record's shape. Called once
     * per effect at load; throw {@link GodTreeConfigException} naming {@code effectId} and the
     * offending field.
     */
    default void validate(String effectId) {
    }
}
