package xyz.iwolfking.woldsvaults.builds.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import xyz.iwolfking.woldsvaults.builds.BuildException;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Converts gear attribute values to and from build JSON through each attribute type's own NBT reader and writer, so
 * every attribute type works without a parser of its own. A value is written as a plain JSON number, boolean or text
 * when that form reads back to the identical tag; anything else is written as {@code snbt}.
 */
public final class ValueCodec {
    private static final int MAX_ENUM_SEARCH = 4096;

    private ValueCodec() {
    }

    /** Writes {@code value} into {@code out} as "value" (plain) or "snbt" (exact tag text) plus a readable "about". */
    public static <T> void write(VaultGearAttribute<T> attribute, T value, JsonObject out) {
        VaultGearAttributeType<T> type = attribute.getType();
        Tag tag = type.nbtWrite(value);
        JsonElement plain = toPlain(type, value, tag);
        if (plain != null) {
            try {
                T back = readPlain(type, plain);
                if (back != null && Objects.equals(type.nbtWrite(back), tag)) {
                    out.add("value", plain);
                    return;
                }
            } catch (RuntimeException ignored) {
            }
        }
        out.addProperty("snbt", tag.toString());
        try {
            out.add("about", type.serialize(value));
        } catch (RuntimeException ignored) {
        }
    }

    /** Reads the value of a build JSON entry for {@code attribute}; {@code path} names the entry in errors. */
    public static <T> T read(VaultGearAttribute<T> attribute, JsonObject in, String path) {
        VaultGearAttributeType<T> type = attribute.getType();
        JsonElement snbt = in.get("snbt");
        JsonElement plain = in.get("value");
        if (snbt != null && plain != null) {
            throw new BuildException(path + ": give either 'value' or 'snbt', not both");
        }
        T result;
        if (snbt != null) {
            if (!snbt.isJsonPrimitive() || !snbt.getAsJsonPrimitive().isString()) {
                throw new BuildException(path + ".snbt: expected text");
            }
            Tag tag = parseTag(snbt.getAsString(), path + ".snbt");
            try {
                result = type.nbtRead(tag);
            } catch (RuntimeException e) {
                throw new BuildException(path + ".snbt: not a valid value for " + attribute.getRegistryName()
                        + " (" + e.getClass().getSimpleName() + ")");
            }
        } else if (plain != null && !plain.isJsonNull()) {
            try {
                result = readPlain(type, plain);
            } catch (BuildException e) {
                throw new BuildException(path + ".value: " + e.getMessage());
            } catch (RuntimeException e) {
                throw new BuildException(path + ".value: " + plain + " is not a valid value for "
                        + attribute.getRegistryName() + " (" + e.getClass().getSimpleName() + "); write it as 'snbt'");
            }
        } else {
            throw new BuildException(path + ": missing 'value'");
        }
        if (result == null) {
            throw new BuildException(path + ": the value does not resolve for " + attribute.getRegistryName()
                    + " (an unknown id?)");
        }
        boolean valid;
        try {
            valid = type.isValid(result);
        } catch (RuntimeException e) {
            valid = false;
        }
        if (!valid) {
            throw new BuildException(path + ": " + attribute.getRegistryName() + " rejects this value");
        }
        return result;
    }

    @Nullable
    private static <T> JsonElement toPlain(VaultGearAttributeType<T> type, T value, Tag tag) {
        JsonElement readable = null;
        try {
            readable = type.serialize(value);
        } catch (RuntimeException ignored) {
        }
        if (tag instanceof NumericTag numeric) {
            if (readable != null && readable.isJsonPrimitive() && readable.getAsJsonPrimitive().isString()) {
                return readable;
            }
            if (tag instanceof FloatTag) {
                return new JsonPrimitive(new BigDecimal(Float.toString(numeric.getAsFloat())));
            }
            if (tag instanceof DoubleTag) {
                return new JsonPrimitive(new BigDecimal(Double.toString(numeric.getAsDouble())));
            }
            return new JsonPrimitive(numeric.getAsLong());
        }
        if (tag instanceof StringTag) {
            if (readable != null && readable.isJsonPrimitive()) {
                return readable;
            }
            return new JsonPrimitive(tag.getAsString());
        }
        return null;
    }

    private static <T> T readPlain(VaultGearAttributeType<T> type, JsonElement plain) {
        if (!plain.isJsonPrimitive()) {
            throw new BuildException("a list or object value must be written as 'snbt'");
        }
        JsonPrimitive primitive = plain.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return type.nbtRead(StringTag.valueOf(Boolean.toString(primitive.getAsBoolean())));
        }
        if (primitive.isNumber()) {
            return type.nbtRead(numberTag(primitive));
        }
        String text = primitive.getAsString();
        try {
            return type.nbtRead(StringTag.valueOf(text));
        } catch (ClassCastException notText) {
            return enumByName(type, text);
        }
    }

    private static Tag numberTag(JsonPrimitive primitive) {
        BigDecimal number = primitive.getAsBigDecimal();
        if (number.signum() == 0 || number.stripTrailingZeros().scale() <= 0) {
            try {
                long whole = number.longValueExact();
                if (whole >= Integer.MIN_VALUE && whole <= Integer.MAX_VALUE) {
                    return IntTag.valueOf((int) whole);
                }
                return LongTag.valueOf(whole);
            } catch (ArithmeticException ignored) {
            }
        }
        return DoubleTag.valueOf(number.doubleValue());
    }

    /** Enum attributes store ordinals; their JSON form is the constant name, found by reading ordinals in turn. */
    private static <T> T enumByName(VaultGearAttributeType<T> type, String name) {
        for (int ordinal = 0; ordinal < MAX_ENUM_SEARCH; ordinal++) {
            T candidate;
            try {
                candidate = type.nbtRead(IntTag.valueOf(ordinal));
            } catch (RuntimeException end) {
                break;
            }
            if (candidate == null) {
                break;
            }
            JsonElement serialized = type.serialize(candidate);
            if (serialized != null && serialized.isJsonPrimitive() && name.equals(serialized.getAsString())) {
                return candidate;
            }
        }
        throw new BuildException("'" + name + "' is not a known value");
    }

    /** Parses one SNBT value of any tag type. */
    public static Tag parseTag(String snbt, String path) {
        try {
            CompoundTag wrapper = TagParser.parseTag("{v:" + snbt + "}");
            Tag tag = wrapper.get("v");
            if (tag == null) {
                throw new BuildException(path + ": empty SNBT");
            }
            return tag;
        } catch (CommandSyntaxException e) {
            throw new BuildException(path + ": invalid SNBT (" + e.getMessage() + ")");
        }
    }

    /** Parses SNBT that must be a compound tag. */
    public static CompoundTag parseCompound(String snbt, String path) {
        Tag tag = parseTag(snbt, path);
        if (!(tag instanceof CompoundTag compound)) {
            throw new BuildException(path + ": expected an SNBT compound {...}");
        }
        return compound;
    }
}
