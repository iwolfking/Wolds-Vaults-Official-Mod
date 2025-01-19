package xyz.iwolfking.woldsvaults.modifiers.clock;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.SupplierKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;
import iskallia.vault.core.vault.time.TickClock;
import iskallia.vault.core.vault.time.modifier.ClockModifier;
import net.minecraft.server.level.ServerLevel;

import static iskallia.vault.core.vault.time.TickClock.DISPLAY_TIME;

public class KillMobTimeExtension extends ClockModifier {
    public static final SupplierKey<ClockModifier> KEY = SupplierKey.of("kill_mobs", ClockModifier.class)
            .with(Version.v1_0, KillMobTimeExtension::new);

    public static final FieldRegistry FIELDS = ClockModifier.FIELDS.merge(new FieldRegistry());

    public static final FieldKey<Integer> TIMES_APPLIED = FieldKey.of("times_applied", Integer.class)
            .with(Version.v1_0, Adapters.INT_SEGMENTED_7, DISK.all())
            .register(FIELDS);

    public static final FieldKey<Integer> INCREMENT = FieldKey.of("increment", Integer.class)
            .with(Version.v1_0, Adapters.INT_SEGMENTED_7, DISK.all())
            .register(FIELDS);


    private KillMobTimeExtension() {

    }

    public KillMobTimeExtension(int increment) {
        this.set(TIMES_APPLIED, 0);
        this.set(INCREMENT, increment);
    }

    @Override
    public SupplierKey<ClockModifier> getKey() {
        return KEY;
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    @Override
    protected void apply(ServerLevel serverLevel, TickClock tickClock) {
        if(this.get(TIMES_APPLIED) >= 5) {
            this.set(CONSUMED);
            return;
        }

        int tickIncreaseDivided = Math.round((float) this.get(INCREMENT) / 5);

        this.set(TIMES_APPLIED, this.get(TIMES_APPLIED) + 1);

        tickClock.set(DISPLAY_TIME, tickClock.get(DISPLAY_TIME) + tickIncreaseDivided);

    }
}
