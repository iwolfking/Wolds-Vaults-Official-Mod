package xyz.iwolfking.woldsvaults.gods.node;

import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodNodeAttributeSource;
import xyz.iwolfking.woldsvaults.gods.GodPiety;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The one attribute provider every god tree registers: every effect of this god whose handler is a
 * {@link StatContributor} contributes. The context is built at scale {@code 1.0} rather than through
 * {@code GodNodeGate}, since the caller applies carryover itself.
 */
public final class GodTreeStatProvider implements GodTreeAttributeProviders.Provider {
    private final VaultGod god;

    public GodTreeStatProvider(VaultGod god) {
        this.god = god;
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getGearAttributes(ServerPlayer player, GodNodeAttributeSource.Scope scope) {
        List<VaultGearAttributeInstance<?>> values = new ArrayList<>();
        GodStatSink sink = GodStatSink.collecting(values);
        for (GodEffect effect : GodNodeRegistry.effectsWith(StatContributor.class)) {
            if (effect.god() != this.god) {
                continue;
            }
            if (scope == GodNodeAttributeSource.Scope.BASIC && !carriesToForeignTrees(effect.id())) {
                continue;
            }
            this.contribute(player, effect, sink);
        }
        return values;
    }

    /** Minor-transfer resolution; returns nothing while this tree is the active one. */
    @Override
    public List<VaultGearAttributeInstance<?>> getMinorTransferAttributes(ServerPlayer player, Collection<String> nodeIds) {
        if (nodeIds.isEmpty() || ActiveGodResolver.isActive(player, this.god)) {
            return List.of();
        }
        List<VaultGearAttributeInstance<?>> values = new ArrayList<>();
        GodStatSink sink = GodStatSink.collecting(values);
        for (String nodeId : nodeIds) {
            GodEffect effect = GodNodeRegistry.effect(nodeId).orElse(null);
            if (effect == null || effect.god() != this.god
                    || GodNodeRegistry.effectType(nodeId) != GodNodeType.MINOR) {
                continue;
            }
            this.contribute(player, effect, sink);
        }
        return values;
    }

    private void contribute(ServerPlayer player, GodEffect effect, GodStatSink sink) {
        StatContributor handler = GodNodeRegistry.handler(effect.id(), StatContributor.class);
        if (handler == null) {
            return;
        }
        int points = this.investedPoints(player, effect.id());
        if (points <= 0) {
            return;
        }
        handler.contribute(new GodNodeContext(player, this.god, effect.id(), points, effect.values(), 1.0F,
                GodPiety.total(player, this.god)), sink);
    }

    /** Plain stat nodes carry onto a foreign tree, and so do the constellation starts. */
    private static boolean carriesToForeignTrees(String effectId) {
        GodNodeType type = GodNodeRegistry.effectType(effectId);
        return type == GodNodeType.STAT || type == GodNodeType.ROOT;
    }

    private int investedPoints(ServerPlayer player, String effectId) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0;
        }
        return GodAlignmentData.get(server).getPointsIn(player.getUUID(), this.god, effectId);
    }
}
