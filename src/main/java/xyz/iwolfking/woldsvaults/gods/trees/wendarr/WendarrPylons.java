package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.vault.pylon.PylonBuff;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import java.util.List;

/**
 * Pylon Whisperer: scales the magnitude fields, not the durations, of the buff config on pylons the
 * player activates. The shared config is round-tripped through NBT, never mutated.
 */
public final class WendarrPylons {
    private static final List<String> MAGNITUDE_KEYS = List.of(
            "ticks", "addend", "capAddend", "amount", "amplifier", "charges", "missingManaPercent");
    private static final Object OWNER = new Object();

    private WendarrPylons() {
    }

    static void register() {
        CommonEvents.USE_PYLON.register(OWNER, data -> {
            if (!(data.getPlayer() instanceof ServerPlayer player)) {
                return;
            }
            if (!WendarrNodes.isActive(player, WendarrNodes.PYLON_WHISPERER)) {
                return;
            }
            PylonBuff.Config<?> boosted = boost(data.getPylonBuffConfig());
            if (boosted != null) {
                data.setPylonBuffConfig(boosted);
            }
        });
    }

    private static PylonBuff.Config<?> boost(PylonBuff.Config<?> config) {
        if (config == null) {
            return null;
        }
        float boost = WendarrNodeHandlers.params(WendarrNodes.PYLON_WHISPERER,
                WendarrNodeHandlers.PylonWhispererParams.class).boost();
        try {
            CompoundTag nbt = config.serializeNBT();
            boolean scaled = false;
            for (String key : MAGNITUDE_KEYS) {
                Tag tag = nbt.get(key);
                if (tag instanceof NumericTag numeric) {
                    nbt.put(key, scale(tag, numeric, boost));
                    scaled = true;
                }
            }
            return scaled ? PylonBuff.Config.fromNBT(nbt) : null;
        } catch (RuntimeException e) {
            WoldsVaults.LOGGER.error("Pylon Whisperer could not rebuild pylon buff config {}; the pylon applied unchanged.",
                    config.getClass().getName(), e);
            return null;
        }
    }

    private static Tag scale(Tag original, NumericTag numeric, float boost) {
        if (original instanceof IntTag) {
            return IntTag.valueOf(Math.round(numeric.getAsInt() * boost));
        }
        if (original instanceof FloatTag) {
            return FloatTag.valueOf(numeric.getAsFloat() * boost);
        }
        return DoubleTag.valueOf(numeric.getAsDouble() * boost);
    }
}
