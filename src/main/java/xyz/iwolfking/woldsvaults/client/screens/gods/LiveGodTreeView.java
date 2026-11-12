package xyz.iwolfking.woldsvaults.client.screens.gods;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.client.Minecraft;
import xyz.iwolfking.woldsvaults.gods.ActiveGodResolver;
import xyz.iwolfking.woldsvaults.gods.ClientGodAlignmentData;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The viewer's own standing: {@link ClientGodAlignmentData} and the charm the client player wears. */
public final class LiveGodTreeView implements GodTreeView {
    public static final LiveGodTreeView INSTANCE = new LiveGodTreeView();

    private LiveGodTreeView() {
    }

    @Override
    public boolean isLive() {
        return true;
    }

    @Override
    public long revision() {
        return ClientGodAlignmentData.revision();
    }

    @Override
    public long getXp(VaultGod god) {
        return ClientGodAlignmentData.getXp(god);
    }

    @Override
    public int getLevel(VaultGod god) {
        return ClientGodAlignmentData.getLevel(god);
    }

    @Override
    public int getSacrifices(VaultGod god) {
        return ClientGodAlignmentData.getSacrifices(god);
    }

    @Override
    public int getUnspentPoints(VaultGod god) {
        return ClientGodAlignmentData.getUnspentPoints(god);
    }

    @Override
    public int getPiety(VaultGod god) {
        return ClientGodAlignmentData.getPiety(god);
    }

    @Override
    public int getReputation(VaultGod god) {
        return ClientGodAlignmentData.getReputation(god);
    }

    @Override
    public Map<String, Integer> getSpentLedger(VaultGod god) {
        return ClientGodAlignmentData.getSpentLedger(god);
    }

    @Override
    public Set<String> getPurchasedTreeNodes(VaultGod god) {
        return ClientGodAlignmentData.getPurchasedTreeNodes(god);
    }

    @Override
    public boolean isTreeNodePurchased(VaultGod god, String nodeId) {
        return ClientGodAlignmentData.isTreeNodePurchased(god, nodeId);
    }

    @Override
    public Optional<String> getMinorTransferSlot(VaultGod god, int slot) {
        return ClientGodAlignmentData.getMinorTransferSlot(god, slot);
    }

    @Override
    public int findMinorTransferSlot(VaultGod god, String effectId) {
        return ClientGodAlignmentData.findMinorTransferSlot(god, effectId);
    }

    @Override
    public int getMinorTransferSlots(VaultGod god) {
        return ClientGodAlignmentData.getMinorTransferSlots(god);
    }

    @Override
    public boolean isMinorTransferLive(VaultGod god, int slot) {
        return ClientGodAlignmentData.isMinorTransferLive(god, slot);
    }

    @Override
    public Optional<VaultGod> charmGod() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? Optional.empty() : ActiveGodResolver.getActiveGod(minecraft.player);
    }
}
