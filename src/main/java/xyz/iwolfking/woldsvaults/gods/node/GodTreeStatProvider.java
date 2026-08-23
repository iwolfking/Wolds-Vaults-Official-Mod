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
 * The one attribute provider every god tree registers. It walks the registry rather than a
 * hand-written list, so a tree's stat nodes are entirely config: every effect of this god whose
 * handler is a {@link StatContributor} contributes, and nothing else has to be told about it.
 *
 * <p>Points are read straight from the purchase ledger, unscaled. The caller
 * ({@link xyz.iwolfking.woldsvaults.gods.GodCarryover}) already decides whether this tree is the
 * active one and scales a foreign tree's basic nodes to a quarter itself, so scaling here as well
 * would apply the carryover twice - which is why the context is built at scale {@code 1.0}
 * instead of through {@link xyz.iwolfking.woldsvaults.gods.GodNodeGate}.
 *
 * <p>{@link GodNodeAttributeSource.Scope#BASIC} is exactly the {@link GodNodeType#STAT} nodes -
 * the subset eligible for carryover - and {@link GodNodeAttributeSource.Scope#ALL} is every stat
 * contribution the tree has, including the ones minor and major nodes make.
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
            if (scope == GodNodeAttributeSource.Scope.BASIC
                    && GodNodeRegistry.effectType(effect.id()) != GodNodeType.STAT) {
                continue;
            }
            this.contribute(player, effect, sink);
        }
        return values;
    }

    /**
     * Minor-transfer resolution. Returns nothing while this tree is the active one: the
     * {@link GodNodeAttributeSource.Scope#ALL} pass above already contributed every minor of this
     * god, and a god's transfer slots only ever carry that god's own learned minors
     * ({@code MinorTransferSlots.isTransferable}), so the slots are by definition dormant while
     * their god is the active one.
     */
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

    private int investedPoints(ServerPlayer player, String effectId) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return 0;
        }
        return GodAlignmentData.get(server).getPointsIn(player.getUUID(), this.god, effectId);
    }
}
