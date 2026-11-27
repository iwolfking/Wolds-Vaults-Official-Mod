package xyz.iwolfking.woldsvaults.gods.node;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Typed configuration for one handler type. Implementations are records registered alongside their
 * handler in {@link GodNodeHandlers} and decoded by {@link GodEffectParamsCodec}.
 */
public interface GodEffectParams {
    /**
     * Marks a params component the config may leave out; it decodes to null instead of failing.
     * <p>
     * Declared here rather than reused from an annotation library on purpose:
     * {@link GodEffectParamsCodec} reads it reflectively, which loads the annotation class, and both
     * the JSR-305 and the JetBrains annotations are compile-time only. Asking for one of those at
     * runtime is a {@code NoClassDefFoundError} that takes the whole mod's setup down with it.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @interface Optional {
    }

    /** Load-time range and cross-field checks; throw {@link GodTreeConfigException} naming the field. */
    default void validate(String effectId) {
    }
}
