package xyz.iwolfking.woldsvaults.gods;

import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Client mirror of the server's node previews: per effect id, a live value or "no live formula". */
public final class ClientGodNodePreviews {
    public record Preview(String formulaText, double multiplier, List<Component> lines) {
    }

    private static final Map<String, Optional<Preview>> PREVIEWS = new HashMap<>();
    private static long revision = 0L;

    private ClientGodNodePreviews() {
    }

    /** Records the server's answer; a null preview marks the effect as having no live formula. */
    public static void set(String effectId, @Nullable Preview preview) {
        PREVIEWS.put(effectId, Optional.ofNullable(preview));
        revision++;
    }

    public static Optional<Preview> get(String effectId) {
        Optional<Preview> preview = PREVIEWS.get(effectId);
        return preview == null ? Optional.empty() : preview;
    }

    /** True once the server has said this effect carries no live formula. */
    public static boolean isKnownStatic(String effectId) {
        Optional<Preview> preview = PREVIEWS.get(effectId);
        return preview != null && preview.isEmpty();
    }

    public static long revision() {
        return revision;
    }

    public static void clear() {
        PREVIEWS.clear();
        revision++;
    }
}
