package xyz.iwolfking.woldsvaults.milestones.client;

import xyz.iwolfking.woldsvaults.milestones.MilestoneDefinition;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client mirror of the local player's milestone counters. Data only — the milestones screen is
 * a later wave and reads this.
 */
public class ClientMilestoneData {
    private static final Map<String, Long> VALUES = new HashMap<>();

    private ClientMilestoneData() {
    }

    public static void replaceAll(Map<String, Long> values) {
        VALUES.clear();
        VALUES.putAll(values);
    }

    public static void apply(Map<String, Long> values) {
        VALUES.putAll(values);
    }

    public static void clear() {
        VALUES.clear();
    }

    public static long getValue(String milestoneId) {
        return VALUES.getOrDefault(milestoneId, 0L);
    }

    public static int getCompletedTiers(String milestoneId) {
        MilestoneDefinition definition = MilestoneRegistry.get(milestoneId);
        return definition == null ? 0 : definition.getCompletedTiers(getValue(milestoneId));
    }

    public static Map<String, Long> getAll() {
        return Collections.unmodifiableMap(VALUES);
    }
}
