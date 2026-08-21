package xyz.iwolfking.woldsvaults.gods.node;

import java.util.Locale;

/**
 * The four node kinds every god tree shares. The type drives gating: a root is a free entry
 * point, a stat node carries at a quarter onto a foreign tree, a minor node also runs when it is
 * bound to one of the active god's minor-transfer slots, and a major node is strictly bound to
 * the active tree.
 */
public enum GodNodeType {
    ROOT,
    STAT,
    MINOR,
    MAJOR;

    /**
     * Parses the {@code type} field of a config node. An unknown type is fatal and names the
     * offending node, rather than degrading it to a type that silently never applies.
     */
    public static GodNodeType fromName(String name, String nodeId) {
        if (name == null || name.isBlank()) {
            throw GodTreeConfigException.fail("God tree node '" + nodeId + "' has no type");
        }
        for (GodNodeType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw GodTreeConfigException.fail("God tree node '" + nodeId + "' has unknown type '" + name + "'");
    }

    public String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
