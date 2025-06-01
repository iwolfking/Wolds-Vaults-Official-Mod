package xyz.iwolfking.woldsvaults.client.data;

import iskallia.vault.core.net.ArrayBitBuffer;
import iskallia.vault.skill.base.TieredSkill;
import xyz.iwolfking.woldsvaults.gui.menus.divinity.DivinityTree;
import xyz.iwolfking.woldsvaults.network.message.KnownDivinityTreeMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClientDivinityData {
    private static DivinityTree DIVINITY_TREE = new DivinityTree();


    @Nonnull
    public static List<TieredSkill> getLearnedTalentNodes() {
        List<TieredSkill> talents = new ArrayList<>();
        DIVINITY_TREE.iterate(TieredSkill.class, (talent) -> {
            if (talent.isUnlocked()) {
                talents.add(talent);
            }

        });
        return talents;
    }

    public static DivinityTree getDivinityTree() {
        return DIVINITY_TREE;
    }

    @Nullable
    public static TieredSkill getLearnedTalentNode(String talentName) {
        Iterator<TieredSkill> var1 = getLearnedTalentNodes().iterator();

        TieredSkill node;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            node = var1.next();
        } while(!node.getId().equals(talentName));

        return node;
    }

    public static void update(KnownDivinityTreeMessage msg) {
        ArrayBitBuffer buffer = ArrayBitBuffer.empty();
        msg.getTree().writeBits(buffer);
        buffer.setPosition(0);
        DIVINITY_TREE.readBits(buffer);
    }
}
