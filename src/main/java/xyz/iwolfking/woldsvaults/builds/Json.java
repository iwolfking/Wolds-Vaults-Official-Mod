package xyz.iwolfking.woldsvaults.builds;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Set;

/** Strict readers for build JSON. Every failure is a {@link BuildException} naming the JSON path. */
public final class Json {
    private Json() {
    }

    @Nullable
    public static JsonObject optObject(JsonObject parent, String key, String path) {
        JsonElement element = parent.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonObject()) {
            throw new BuildException(path + "." + key + ": expected an object");
        }
        return element.getAsJsonObject();
    }

    public static JsonObject object(JsonObject parent, String key, String path) {
        JsonObject object = optObject(parent, key, path);
        if (object == null) {
            throw new BuildException(path + "." + key + ": missing");
        }
        return object;
    }

    @Nullable
    public static JsonArray optArray(JsonObject parent, String key, String path) {
        JsonElement element = parent.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonArray()) {
            throw new BuildException(path + "." + key + ": expected a list");
        }
        return element.getAsJsonArray();
    }

    public static JsonObject asObject(JsonElement element, String path) {
        if (element == null || !element.isJsonObject()) {
            throw new BuildException(path + ": expected an object");
        }
        return element.getAsJsonObject();
    }

    @Nullable
    public static String optString(JsonObject parent, String key, String path) {
        JsonElement element = parent.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new BuildException(path + "." + key + ": expected text");
        }
        return element.getAsString();
    }

    public static String string(JsonObject parent, String key, String path) {
        String value = optString(parent, key, path);
        if (value == null) {
            throw new BuildException(path + "." + key + ": missing");
        }
        return value;
    }

    public static String asString(JsonElement element, String path) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new BuildException(path + ": expected text");
        }
        return element.getAsString();
    }

    @Nullable
    public static Long optLong(JsonObject parent, String key, String path) {
        JsonElement element = parent.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return asLong(element, path + "." + key);
    }

    public static long asLong(JsonElement element, String path) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new BuildException(path + ": expected a whole number");
        }
        double value = element.getAsDouble();
        if (value != Math.rint(value) || Double.isInfinite(value)) {
            throw new BuildException(path + ": expected a whole number, got " + element.getAsString());
        }
        return element.getAsLong();
    }

    @Nullable
    public static Integer optInt(JsonObject parent, String key, String path) {
        Long value = optLong(parent, key, path);
        if (value == null) {
            return null;
        }
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new BuildException(path + "." + key + ": number out of range");
        }
        return value.intValue();
    }

    public static int intValue(JsonObject parent, String key, String path) {
        Integer value = optInt(parent, key, path);
        if (value == null) {
            throw new BuildException(path + "." + key + ": missing");
        }
        return value;
    }

    public static int asInt(JsonElement element, String path) {
        long value = asLong(element, path);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new BuildException(path + ": number out of range");
        }
        return (int) value;
    }

    public static double asDouble(JsonElement element, String path) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new BuildException(path + ": expected a number");
        }
        return element.getAsDouble();
    }

    @Nullable
    public static Boolean optBoolean(JsonObject parent, String key, String path) {
        JsonElement element = parent.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new BuildException(path + "." + key + ": expected true or false");
        }
        return element.getAsBoolean();
    }

    public static ResourceLocation id(String text, String path) {
        ResourceLocation id = ResourceLocation.tryParse(text);
        if (id == null) {
            throw new BuildException(path + ": '" + text + "' is not a valid id");
        }
        return id;
    }

    /** Rejects keys the schema does not know, so a typo is an error instead of a silently ignored field. */
    public static void onlyKeys(JsonObject object, String path, Set<String> allowed) {
        for (String key : object.keySet()) {
            if (!allowed.contains(key)) {
                throw new BuildException(path + ": unknown field '" + key + "' (allowed: " + String.join(", ", allowed) + ")");
            }
        }
    }

    public static JsonPrimitive text(String value) {
        return new JsonPrimitive(value);
    }
}
