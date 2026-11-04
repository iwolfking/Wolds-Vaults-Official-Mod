package xyz.iwolfking.woldsvaults.config.patch;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A field-level patch Wold's Vaults applies to one base Vault Hunters config file as the game reads it. The patch
 * carries only the values Wold's Vaults changes, so every other upstream value keeps flowing through.
 *
 * <p>Ops address nodes by a path of object keys, array indices and array selectors. A selector is a one-entry
 * object: {@code {"id": "X"}} picks the single array element whose {@code id} equals {@code "X"}, and
 * {@code {"$value": v}} picks the single element equal to {@code v}. Each op is strict: {@code set} and
 * {@code remove} need the target to exist, {@code add} needs the key to be absent, {@code append} needs an array.
 * An op that does not resolve is reported and skipped, leaving the upstream value in place.</p>
 *
 * <p>{@code create} supplies the whole file for a config upstream does not ship; the ops then apply on top.</p>
 */
public final class ConfigPatch {
    public static final String SET = "set";
    public static final String ADD = "add";
    public static final String REMOVE = "remove";
    public static final String APPEND = "append";
    public static final String VALUE_SELECTOR = "$value";

    @Nullable
    private final JsonElement create;
    private final List<Op> ops;

    public ConfigPatch(@Nullable JsonElement create, List<Op> ops) {
        this.create = create;
        this.ops = List.copyOf(ops);
    }

    @Nullable
    public JsonElement create() {
        return this.create;
    }

    public List<Op> ops() {
        return this.ops;
    }

    public static ConfigPatch fromJson(JsonObject json) {
        JsonElement create = json.has("create") ? json.get("create") : null;
        List<Op> ops = new ArrayList<>();
        if (json.has("ops")) {
            for (JsonElement element : json.getAsJsonArray("ops")) {
                JsonObject op = element.getAsJsonObject();
                ops.add(new Op(op.get("op").getAsString(), op.getAsJsonArray("path"), op.get("value")));
            }
        }
        return new ConfigPatch(create, ops);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (this.create != null) {
            json.add("create", this.create);
        }
        JsonArray array = new JsonArray();
        for (Op op : this.ops) {
            JsonObject entry = new JsonObject();
            entry.addProperty("op", op.type());
            entry.add("path", op.path());
            if (op.value() != null) {
                entry.add("value", op.value());
            }
            array.add(entry);
        }
        json.add("ops", array);
        return json;
    }

    /**
     * Applies every op to a copy of {@code base}. Ops that do not resolve are skipped and described in the result's
     * failures, so the caller decides whether that is an error log (runtime) or a failed datagen run.
     */
    public Result apply(JsonElement base) {
        JsonElement root = base.deepCopy();
        List<String> failures = new ArrayList<>();
        for (int i = 0; i < this.ops.size(); i++) {
            Op op = this.ops.get(i);
            String problem = applyOp(root, op);
            if (problem != null) {
                failures.add("op " + i + " " + op.type() + " " + op.path() + ": " + problem);
            }
        }
        return new Result(root, failures);
    }

    @Nullable
    private static String applyOp(JsonElement root, Op op) {
        JsonArray path = op.path();
        if (path.size() == 0) {
            return "empty path";
        }
        if (!REMOVE.equals(op.type()) && op.value() == null) {
            return op.type() + " has no value";
        }
        if (APPEND.equals(op.type())) {
            Located target = locate(root, path, path.size());
            if (target.problem != null) {
                return target.problem;
            }
            if (!target.node.isJsonArray()) {
                return "target is not an array";
            }
            target.node.getAsJsonArray().add(op.value().deepCopy());
            return null;
        }
        Located parent = locate(root, path, path.size() - 1);
        if (parent.problem != null) {
            return parent.problem;
        }
        JsonElement last = path.get(path.size() - 1);
        if (parent.node.isJsonObject()) {
            if (!isKey(last)) {
                return "object step needs a key, got " + last;
            }
            JsonObject object = parent.node.getAsJsonObject();
            String key = last.getAsString();
            switch (op.type()) {
                case SET -> {
                    if (!object.has(key)) {
                        return "key " + key + " is missing";
                    }
                    object.add(key, op.value().deepCopy());
                }
                case ADD -> {
                    if (object.has(key)) {
                        return "key " + key + " already exists";
                    }
                    object.add(key, op.value().deepCopy());
                }
                case REMOVE -> {
                    if (object.remove(key) == null) {
                        return "key " + key + " is missing";
                    }
                }
                default -> {
                    return "unknown op";
                }
            }
            return null;
        }
        if (parent.node.isJsonArray()) {
            JsonArray array = parent.node.getAsJsonArray();
            Selected selected = select(array, last);
            if (selected.problem != null) {
                return selected.problem;
            }
            switch (op.type()) {
                case SET -> array.set(selected.index, op.value().deepCopy());
                case REMOVE -> array.remove(selected.index);
                default -> {
                    return op.type() + " cannot target an array element";
                }
            }
            return null;
        }
        return "parent is not an object or array";
    }

    private static Located locate(JsonElement root, JsonArray path, int steps) {
        JsonElement node = root;
        for (int i = 0; i < steps; i++) {
            JsonElement step = path.get(i);
            if (node.isJsonObject()) {
                if (!isKey(step)) {
                    return Located.failed("step " + i + " needs a key, got " + step);
                }
                JsonElement next = node.getAsJsonObject().get(step.getAsString());
                if (next == null) {
                    return Located.failed("key " + step.getAsString() + " is missing");
                }
                node = next;
            } else if (node.isJsonArray()) {
                Selected selected = select(node.getAsJsonArray(), step);
                if (selected.problem != null) {
                    return Located.failed(selected.problem);
                }
                node = node.getAsJsonArray().get(selected.index);
            } else {
                return Located.failed("step " + i + " walks into a value");
            }
        }
        return new Located(node, null);
    }

    private static Selected select(JsonArray array, JsonElement step) {
        if (step.isJsonPrimitive() && step.getAsJsonPrimitive().isNumber()) {
            int index = step.getAsInt();
            if (index < 0 || index >= array.size()) {
                return Selected.failed("index " + index + " is outside 0.." + (array.size() - 1));
            }
            return new Selected(index, null);
        }
        if (!step.isJsonObject() || step.getAsJsonObject().size() != 1) {
            return Selected.failed("array step needs an index or a one-entry selector, got " + step);
        }
        Map.Entry<String, JsonElement> selector = step.getAsJsonObject().entrySet().iterator().next();
        int found = -1;
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            boolean matches;
            if (VALUE_SELECTOR.equals(selector.getKey())) {
                matches = element.equals(selector.getValue());
            } else {
                matches = element.isJsonObject() && selector.getValue().equals(element.getAsJsonObject().get(selector.getKey()));
            }
            if (matches) {
                if (found >= 0) {
                    return Selected.failed("selector " + step + " matches more than one element");
                }
                found = i;
            }
        }
        if (found < 0) {
            return Selected.failed("selector " + step + " matches nothing");
        }
        return new Selected(found, null);
    }

    private static boolean isKey(JsonElement step) {
        return step.isJsonPrimitive() && step.getAsJsonPrimitive().isString();
    }

    public record Op(String type, JsonArray path, @Nullable JsonElement value) {
    }

    public record Result(JsonElement json, List<String> failures) {
    }

    private record Located(JsonElement node, @Nullable String problem) {
        static Located failed(String problem) {
            return new Located(null, problem);
        }
    }

    private record Selected(int index, @Nullable String problem) {
        static Selected failed(String problem) {
            return new Selected(-1, problem);
        }
    }
}
