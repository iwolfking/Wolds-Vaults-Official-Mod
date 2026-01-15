package xyz.iwolfking.woldsvaults.api.core.layout.tooltip;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.world.generator.layout.ArchitectRoomEntry;
import iskallia.vault.init.ModConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import xyz.iwolfking.woldsvaults.api.core.layout.tooltip.component.LayoutTooltipComponent;

import javax.annotation.Nonnull;
import java.util.*;

public class ArchitectLayoutTooltip {
    public static @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data, LayoutTooltipComponent existingComponent) {
        return getTooltipImage(data, existingComponent, 0L);
    }

    public static @Nonnull Optional<TooltipComponent> getTooltipImage(CompoundTag data, LayoutTooltipComponent existingComponent, long seed) {
        ListTag entries = data.getList("entries", Tag.TAG_COMPOUND);
        List<ArchitectRoomEntry> roomEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            roomEntries.add(ArchitectRoomEntry.fromNBT(entries.getCompound(i)));
        }

        generateInscriptions(seed, existingComponent, roomEntries, ModConfigs.INSCRIPTION.getRingWeights(), false);


        return Optional.of(existingComponent);
    }

    public static void generateInscriptions(
            long vaultSeed,
            LayoutTooltipComponent component,
            List<ArchitectRoomEntry> entries,
            List<Integer> ringWeights,
            boolean hasRuneBoss
    ) {
        LayoutTooltipComponent.RoomType[][] rooms = component.rooms();
        int h = rooms.length;
        int w = rooms[0].length;
        int centerR = h / 2;
        int centerC = w / 2;

        List<int[]> allRooms = new ArrayList<>();
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (rooms[r][c] == LayoutTooltipComponent.RoomType.ROOM) allRooms.add(new int[]{r, c});
            }
        }

        int inscriptionCount = entries.stream().mapToInt(e -> e.get(ArchitectRoomEntry.COUNT)).sum();
        if (inscriptionCount == 0) return;

        // Build rings
        List<List<int[]>> rings = new ArrayList<>();
        int maxDistance = Math.max(centerR, centerC);
        for (int d = 0; d <= maxDistance; d++) {
            List<int[]> ring = new ArrayList<>();
            for (int[] pos : allRooms) {
                int dist = Math.max(Math.abs(pos[0] - centerR), Math.abs(pos[1] - centerC));
                if (dist == d) ring.add(pos);
            }
            rings.add(ring);
        }

        JavaRandom random = JavaRandom.ofScrambled(vaultSeed);
        Collections.shuffle(allRooms, random.asRandomView());

        int placed = 0;

        Set<String> used = new HashSet<>();

        while (placed < inscriptionCount) {
            int ringIndex = pickRingWeighted(rings, ringWeights, random);
            List<int[]> ring = (ringIndex >= 0 && ringIndex < rings.size()) ? rings.get(ringIndex) : Collections.emptyList();

            // Filter out used positions in this ring
            List<int[]> available = new ArrayList<>();
            for (int[] pos : ring) {
                String key = pos[0] + "," + pos[1];
                if (!used.contains(key)) available.add(pos);
            }

            int[] chosen;
            if (!available.isEmpty()) {
                int roomIdx = random.nextInt(available.size());
                chosen = available.get(roomIdx);
            } else if (!allRooms.isEmpty()) {
                // pick any unused room globally
                List<int[]> globalAvailable = new ArrayList<>();
                for (int[] pos : allRooms) {
                    String key = pos[0] + "," + pos[1];
                    if (!used.contains(key)) globalAvailable.add(pos);
                }
                if (globalAvailable.isEmpty()) break;
                int roomIdx = random.nextInt(globalAvailable.size());
                chosen = globalAvailable.get(roomIdx);
            } else {
                break;
            }

            if (chosen == null) break;
            if (hasRuneBoss && chosen[0] == centerR && chosen[1] == centerC) continue;

            // Mark chosen as used
            used.add(chosen[0] + "," + chosen[1]);
            rooms[chosen[0]][chosen[1]] = LayoutTooltipComponent.RoomType.INSCRIPTION;
            placed++;
        }

    }

    private static int pickRingWeighted(List<List<int[]>> rings, List<Integer> weights, JavaRandom random) {
        int totalWeight = 0;
        for (int i = 0; i < rings.size() && i < weights.size(); i++) {
            if (!rings.get(i).isEmpty()) totalWeight += weights.get(i);
        }
        if (totalWeight <= 0) return -1;

        int r = random.nextInt(totalWeight);
        for (int i = 0; i < rings.size() && i < weights.size(); i++) {
            List<int[]> ring = rings.get(i);
            if (ring.isEmpty()) continue;
            int w = weights.get(i);
            if (r < w) return i;
            r -= w;
        }
        return -1;
    }

}