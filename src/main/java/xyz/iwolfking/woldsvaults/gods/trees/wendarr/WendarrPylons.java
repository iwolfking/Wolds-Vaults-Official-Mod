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
 * Pylon Whisperer (r82): pylons the player activates give 50% greater effects.
 *
 * <p>{@code USE_PYLON} is the one choke point that carries both the player and a settable buff
 * config, so no mixin is needed. The config object handed over belongs to the pylon block and is
 * reused by everyone who touches it, so it is never mutated -  it is round-tripped through NBT into
 * a fresh instance whose magnitude fields are scaled. Durations are deliberately left alone: the
 * node promises stronger pylons, not longer ones.
 */
public final class WendarrPylons {
    public static final float PYLON_BOOST = 1.5F;

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
            if (!WendarrNodes.hasMinor(player, WendarrNodes.PYLON_WHISPERER)) {
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
        try {
            CompoundTag nbt = config.serializeNBT();
            boolean scaled = false;
            for (String key : MAGNITUDE_KEYS) {
                Tag tag = nbt.get(key);
                if (tag instanceof NumericTag numeric) {
                    nbt.put(key, scale(tag, numeric));
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

    private static Tag scale(Tag original, NumericTag numeric) {
        if (original instanceof IntTag) {
            return IntTag.valueOf(Math.round(numeric.getAsInt() * PYLON_BOOST));
        }
        if (original instanceof FloatTag) {
            return FloatTag.valueOf(numeric.getAsFloat() * PYLON_BOOST);
        }
        return DoubleTag.valueOf(numeric.getAsDouble() * PYLON_BOOST);
    }
}
