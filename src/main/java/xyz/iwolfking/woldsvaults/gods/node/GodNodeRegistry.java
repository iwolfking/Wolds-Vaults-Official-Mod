package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.influence.VaultGod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.config.gods.GodNodeEffectsConfig;
import xyz.iwolfking.woldsvaults.config.gods.GodTreeConfig;
import xyz.iwolfking.woldsvaults.config.gods.GodTreeGuiStylesConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The loaded god trees. Built once from config on every config load and read everywhere
 * afterwards; there is no second source of node data.
 *
 * <p>Effect ids are globally unique across gods, so the flat effect and handler lookups here are
 * what the gate, the ticker and the vanilla attribute bridge use without having to know which
 * tree an effect belongs to.
 */
public final class GodNodeRegistry {
    private static final List<Class<? extends GodNodeHandler>> CAPABILITIES = List.of(
            StatContributor.class, VaultContributor.class, CombatContributor.class, TickContributor.class);

    private static volatile Map<VaultGod, GodTreeModel> trees = Collections.emptyMap();
    private static volatile Map<String, GodEffect> effects = Collections.emptyMap();
    private static volatile Map<String, GodNodeHandler> handlers = Collections.emptyMap();
    private static volatile Map<Class<?>, List<GodEffect>> byCapability = Collections.emptyMap();

    private GodNodeRegistry() {
    }

    /**
     * Rebuilds every tree from config, failing loud on the first inconsistency. Nothing is
     * published until all four gods have loaded, so a failed reload leaves the previously loaded
     * trees in place rather than a half-built registry.
     */
    public static void load(Map<VaultGod, GodTreeConfig> treeConfigs,
                            Map<VaultGod, GodTreeGuiStylesConfig> styleConfigs,
                            Map<VaultGod, GodNodeEffectsConfig> effectConfigs) {
        Map<VaultGod, GodTreeModel> loadedTrees = new EnumMap<>(VaultGod.class);
        Map<String, GodEffect> loadedEffects = new LinkedHashMap<>();
        Map<String, GodNodeHandler> loadedHandlers = new LinkedHashMap<>();
        int nodeCount = 0;
        for (VaultGod god : VaultGod.values()) {
            GodTreeConfig tree = treeConfigs.get(god);
            GodTreeGuiStylesConfig styles = styleConfigs.get(god);
            GodNodeEffectsConfig godEffects = effectConfigs.get(god);
            if (tree == null || styles == null || godEffects == null) {
                throw GodTreeConfigException.fail("God tree configs for " + god.getName() + " were not read");
            }
            GodTreeModel model = GodTreeLoader.load(god, tree, styles, godEffects);
            loadedTrees.put(god, model);
            nodeCount += model.getNodes().size();
            for (GodEffect effect : model.getEffects()) {
                GodEffect previous = loadedEffects.put(effect.id(), effect);
                if (previous != null) {
                    throw GodTreeConfigException.fail("God effect id '" + effect.id() + "' is defined by both "
                            + previous.god().getName() + " and " + god.getName());
                }
                loadedHandlers.put(effect.id(), model.getHandler(effect.id()));
            }
        }
        Map<Class<?>, List<GodEffect>> capabilities = new LinkedHashMap<>();
        for (Class<? extends GodNodeHandler> capability : CAPABILITIES) {
            List<GodEffect> matching = new ArrayList<>();
            for (GodEffect effect : loadedEffects.values()) {
                if (capability.isInstance(loadedHandlers.get(effect.id()))) {
                    matching.add(effect);
                }
            }
            capabilities.put(capability, Collections.unmodifiableList(matching));
        }

        trees = Collections.unmodifiableMap(loadedTrees);
        effects = Collections.unmodifiableMap(loadedEffects);
        handlers = Collections.unmodifiableMap(loadedHandlers);
        byCapability = Collections.unmodifiableMap(capabilities);
        WoldsVaults.LOGGER.info("Loaded {} god tree nodes and {} node effects across {} trees",
                nodeCount, loadedEffects.size(), loadedTrees.size());
    }

    public static Optional<GodTreeModel> tree(VaultGod god) {
        return Optional.ofNullable(trees.get(god));
    }

    public static Collection<GodTreeModel> trees() {
        return trees.values();
    }

    public static Optional<GodNode> node(VaultGod god, String nodeId) {
        return tree(god).map(model -> model.getNode(nodeId));
    }

    public static Optional<GodEffect> effect(String effectId) {
        return Optional.ofNullable(effects.get(effectId));
    }

    public static Collection<GodEffect> effects() {
        return effects.values();
    }

    /** The node type every placement of {@code effectId} shares, or null if it is not configured. */
    @Nullable
    public static GodNodeType effectType(String effectId) {
        GodEffect effect = effects.get(effectId);
        if (effect == null) {
            return null;
        }
        GodTreeModel model = trees.get(effect.god());
        return model == null ? null : model.getEffectType(effectId);
    }

    @Nullable
    public static GodNodeHandler handler(String effectId) {
        return handlers.get(effectId);
    }

    /** The handler of {@code effectId} if it implements {@code capability}, else null. */
    @Nullable
    public static <T extends GodNodeHandler> T handler(String effectId, Class<T> capability) {
        GodNodeHandler handler = handlers.get(effectId);
        return capability.isInstance(handler) ? capability.cast(handler) : null;
    }

    /** Every effect whose handler implements {@code capability}, in config order. */
    public static List<GodEffect> effectsWith(Class<? extends GodNodeHandler> capability) {
        return byCapability.getOrDefault(capability, Collections.emptyList());
    }

    public static boolean isEmpty() {
        return effects.isEmpty();
    }
}
