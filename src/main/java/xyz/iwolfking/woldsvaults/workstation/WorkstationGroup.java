package xyz.iwolfking.woldsvaults.workstation;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.*;

public final class WorkstationGroup {
    private static final List<WorkstationGroup> VALUES = new ArrayList<>();

    public final String name;
    public final TagKey<Block> tag;

    private WorkstationGroup(String name) {
        this.name = name;
        this.tag = TagKey.create(Registry.BLOCK_REGISTRY, ResourceLocation.fromNamespaceAndPath(WoldsVaults.MOD_ID, "workstation_group_" + name));
    }

    public static WorkstationGroup register(String name) {
        WorkstationGroup group = new WorkstationGroup(name);
        VALUES.add(group);
        return group;
    }

    public static List<WorkstationGroup> values() {
        return Collections.unmodifiableList(new ArrayList<>(VALUES));
    }

    public boolean contains(Block block) {
        if (block == null) {
            return false;
        }
        try {
            return block.defaultBlockState().is(tag);
        } catch (Exception e) {
            return false;
        }
    }

    public static List<WorkstationGroup> groupsOf(Block block) {
        List<WorkstationGroup> result = new ArrayList<>();
        if (block == null) {
            return result;
        }
        for (WorkstationGroup group : VALUES) {
            if (group.contains(block)) {
                result.add(group);
            }
        }
        return result;
    }

    public static Set<TagKey<Block>> allTags() {
        LinkedHashSet<TagKey<Block>> result = new LinkedHashSet<>();
        for (WorkstationGroup group : VALUES) {
            result.add(group.tag);
        }
        return result;
    }
}
