package xyz.iwolfking.woldsvaults.objectives.extraction;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.DataObject;
import iskallia.vault.core.data.adapter.Adapters;
import iskallia.vault.core.data.adapter.IBitAdapter;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.data.key.registry.FieldRegistry;

import java.util.UUID;

public class ExtractionSite extends DataObject<ExtractionSite> {
    public static final int STATE_UNTOUCHED = 0;
    public static final int STATE_DRILLING = 1;
    public static final int STATE_COMPLETE = 2;
    public static final int STATE_ARMED = 3;

    public static final int THUMPER_SLOTS = 2;
    public static final FieldRegistry FIELDS = new FieldRegistry();
    public static final FieldKey<Integer> REGION_X = FieldKey.of("region_x", Integer.class)
            .with(Version.v1_0, Adapters.INT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> REGION_Z = FieldKey.of("region_z", Integer.class)
            .with(Version.v1_0, Adapters.INT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<String> GOD = FieldKey.of("god", String.class)
            .with(Version.v1_0, Adapters.UTF_8, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> STATE = FieldKey.of("state", Integer.class)
            .with(Version.v1_0, Adapters.INT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Float> PROGRESS = FieldKey.of("progress", Float.class)
            .with(Version.v1_0, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> REQUIRED = FieldKey.of("required", Integer.class)
            .with(Version.v1_0, Adapters.INT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<UUID> ENTITY = FieldKey.of("entity", UUID.class)
            .with(Version.v1_0, Adapters.UUID, DISK.all()).register(FIELDS);
    public static final FieldKey<Float> HP = FieldKey.of("hp", Float.class)
            .with(Version.v1_0, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Float> MAX_HP = FieldKey.of("max_hp", Float.class)
            .with(Version.v1_0, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Float> SHIELD = FieldKey.of("shield", Float.class)
            .with(Version.v1_0, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Float> MAX_SHIELD = FieldKey.of("max_shield", Float.class)
            .with(Version.v1_0, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Float> ENERGY = FieldKey.of("energy", Float.class)
            .with(Version.v1_0, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Float> MAX_ENERGY = FieldKey.of("max_energy", Float.class)
            .with(Version.v1_0, Adapters.FLOAT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Boolean> OCCUPIED = FieldKey.of("occupied", Boolean.class)
            .with(Version.v1_0, Adapters.BOOLEAN, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> SPAN = FieldKey.of("span", Integer.class)
            .with(Version.v1_0, Adapters.INT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<Integer> POINT = FieldKey.of("point", Integer.class)
            .with(Version.v1_0, Adapters.INT, DISK.all().or(CLIENT.all())).register(FIELDS);
    public static final FieldKey<UUID> RIG_KEY = FieldKey.of("rig_key", UUID.class)
            .with(Version.v1_0, Adapters.UUID, DISK.all()).register(FIELDS);
    public static final FieldKey<long[]> CACHE_CHESTS = FieldKey.of("cache_chests", long[].class)
            .with(Version.v1_0, Adapters.LONG_ARRAY, DISK.all()).register(FIELDS);
    public static final FieldKey<UUID>[] THUMPER_ENTITY = thumperKeys("entity", UUID.class, Adapters.UUID, false);
    public static final FieldKey<Integer>[] THUMPER_STATE = thumperKeys("state", Integer.class, Adapters.INT, true);
    public static final FieldKey<Float>[] THUMPER_HP = thumperKeys("hp", Float.class, Adapters.FLOAT, true);
    public static final FieldKey<Float>[] THUMPER_MAX_HP = thumperKeys("max_hp", Float.class, Adapters.FLOAT, true);
    public static final FieldKey<Float>[] THUMPER_SHIELD = thumperKeys("shield", Float.class, Adapters.FLOAT, true);
    public static final FieldKey<Float>[] THUMPER_MAX_SHIELD = thumperKeys("max_shield", Float.class, Adapters.FLOAT, true);

    public static final int THUMPER_NONE = 0;
    public static final int THUMPER_STANDING = 1;
    public static final int THUMPER_WRECKED = 2;

    @SuppressWarnings("unchecked")
    private static <T> FieldKey<T>[] thumperKeys(String name, Class<T> type, IBitAdapter<T, ?> adapter, boolean client) {
        FieldKey<T>[] keys = new FieldKey[THUMPER_SLOTS];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = FieldKey.of("thumper_" + (i + 1) + "_" + name, type)
                    .with(Version.v1_0, adapter, client ? DISK.all().or(CLIENT.all()) : DISK.all()).register(FIELDS);
        }
        return keys;
    }

    public ExtractionSite() {
    }

    public ExtractionSite(int regionX, int regionZ, int span, String god, int requiredTicks) {
        this.set(REGION_X, regionX);
        this.set(REGION_Z, regionZ);
        this.set(SPAN, span);
        this.set(POINT, -1);
        this.set(GOD, god);
        this.set(STATE, STATE_UNTOUCHED);
        this.set(PROGRESS, 0.0F);
        this.set(REQUIRED, requiredTicks);
        this.set(HP, 0.0F);
        this.set(MAX_HP, 0.0F);
        this.set(SHIELD, 0.0F);
        this.set(MAX_SHIELD, 0.0F);
        this.set(ENERGY, 0.0F);
        this.set(MAX_ENERGY, 0.0F);
        this.set(OCCUPIED, false);
    }

    @Override
    public FieldRegistry getFields() {
        return FIELDS;
    }

    public boolean isUntouched() {
        return this.get(STATE) == STATE_UNTOUCHED;
    }

    public boolean isDrilling() {
        return this.get(STATE) == STATE_DRILLING;
    }

    public boolean isComplete() {
        return this.get(STATE) == STATE_COMPLETE;
    }

    /** Placed but not started: the rig waits for its inputs and a press of Start, with no progress and no waves. */
    public boolean isArmed() {
        return this.get(STATE) == STATE_ARMED;
    }

    /** Width of the site's room in grid cells; sites saved before multi-cell rooms are single-cell. */
    public int getSpan() {
        return this.has(SPAN) ? this.get(SPAN) : 1;
    }

    /** Index of the pedestal the extractor was placed on, or -1 before the first placement. */
    public int getPoint() {
        return this.has(POINT) ? this.get(POINT) : -1;
    }

    /** Thumper slot {@code slot}'s state for the HUD and placement rules: none, standing or wrecked. */
    public int getThumperState(int slot) {
        return this.has(THUMPER_STATE[slot]) ? this.get(THUMPER_STATE[slot]) : THUMPER_NONE;
    }

    /** How many thumper slots hold a thumper, standing or wrecked. */
    public int thumpersPlaced() {
        int placed = 0;
        for (int slot = 0; slot < THUMPER_SLOTS; slot++) {
            if (this.getThumperState(slot) != THUMPER_NONE) {
                placed++;
            }
        }
        return placed;
    }

    /** How many thumpers on this site were wrecked during the current job. */
    public int thumpersWrecked() {
        int wrecked = 0;
        for (int slot = 0; slot < THUMPER_SLOTS; slot++) {
            if (this.getThumperState(slot) == THUMPER_WRECKED) {
                wrecked++;
            }
        }
        return wrecked;
    }

    /** True while an extractor entity stands on this site; drives the placed HUD. */
    public boolean isOccupied() {
        return this.has(OCCUPIED) && this.get(OCCUPIED);
    }
}
