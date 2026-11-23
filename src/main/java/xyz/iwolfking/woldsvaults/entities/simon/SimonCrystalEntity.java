package xyz.iwolfking.woldsvaults.entities.simon;

import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.api.util.ColorUtil;
import xyz.iwolfking.woldsvaults.init.ModEntities;
import xyz.iwolfking.woldsvaults.abilities.arcane.BeamOptics;
import xyz.iwolfking.woldsvaults.modifiers.gear.simon.SimonsSet;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A prism crystal of a Simon's Set wearer ({@link SimonsSet}). It sits on the face of a block, its position being
 * the point on that face, and grows through four stages (Seed, Shard, Cluster, Geode) from the damage its owner
 * deals nearby, shrinking again when left unfed. It is either the one the owner planted with a thrown seed or one
 * that budded off another crystal, has one of the four Simon colours (or is Prismatic, shimmering through all of
 * them), can carry up to three facets as a Geode (two blocks tall), and can light up during a sequence. It is hit
 * in a sequence by its owner aiming at it and clicking (picked on the client from its box), never by a swing: it isn't
 * pickable or attackable, so the crosshair, swings and Better Combat sweeps pass it by and hit what's behind it. It
 * takes no damage, doesn't move, block or get pushed, and isn't saved with the world.
 */
public class SimonCrystalEntity extends Entity implements IEntityAdditionalSpawnData, BeamOptics.Tinted {
    public static final int SEED = 0;
    public static final int SHARD = 1;
    public static final int CLUSTER = 2;
    public static final int GEODE = 3;
    public static final String[] STAGE_NAMES = {"seed", "shard", "cluster", "geode"};
    public static final byte DARK = 0;
    public static final byte LIT = 1;
    public static final byte ARMED = 2;

    // The Simon colours, in the order crystals take them: boots, leggings, chestplate, helmet.
    // each colour's note, in note block clicks up from F# (red C, green E, blue D, yellow G)
    public enum Color implements StringRepresentable {
        RED("red", 0xE5322D, 6),
        GREEN("green", 0x2DBE4A, 10),
        BLUE("blue", 0x2F6FE0, 8),
        YELLOW("yellow", 0xF7D531, 13);

        public final String id;
        public final int rgb;
        public final int note;

        Color(String id, int rgb, int note) {
            this.id = id;
            this.rgb = rgb;
            this.note = note;
        }

        // The sound pitch that plays this colour's note, as a note block would.
        public float pitch() {
            return (float) Math.pow(2.0, (this.note - 12) / 12.0);
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        public Vector3f vector() {
            return ColorUtil.vector(this.rgb);
        }
    }

    // width across the face and length out from it, per stage
    private static final float[][] SIZES = {{0.35F, 0.3F}, {0.55F, 0.6F}, {0.9F, 1.2F}, {1.3F, 2.0F}};

    private static final EntityDataAccessor<Byte> DATA_STAGE = SynchedEntityData.defineId(SimonCrystalEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_LIGHT = SynchedEntityData.defineId(SimonCrystalEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_FACETS = SynchedEntityData.defineId(SimonCrystalEntity.class, EntityDataSerializers.BYTE);

    private Direction face = Direction.UP;
    private Color color = Color.RED;
    private boolean prismatic;
    // mirrors DATA_STAGE so the bounding box can be built before synced data exists
    private int stage;
    @Nullable
    private UUID owner;
    private boolean natural;
    public float growth;
    public int unfedTicks;
    public long lastFedTick = -1;
    // client: the tick the crystal last grew (or appeared), for the rise out of the ground
    private int grownAt;
    // client: the ticks it last lit up in a sequence and last went out after being lit, for its light pillar
    private int litAt = Integer.MIN_VALUE / 2;
    private int unlitAt = Integer.MIN_VALUE / 2;
    private byte shownLight = DARK;

    public SimonCrystalEntity(EntityType<? extends SimonCrystalEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public SimonCrystalEntity(Level level, ServerPlayer owner, Vec3 at, Direction face, Color color, boolean natural, boolean prismatic) {
        this(ModEntities.SIMON_CRYSTAL, level);
        this.owner = owner.getUUID();
        this.face = face;
        this.color = color;
        this.natural = natural;
        this.prismatic = prismatic;
        this.setPos(at.x, at.y, at.z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_STAGE, (byte) SEED);
        this.entityData.define(DATA_LIGHT, DARK);
        this.entityData.define(DATA_FACETS, (byte) 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_STAGE.equals(key)) {
            int stage = Mth.clamp(this.entityData.get(DATA_STAGE), SEED, GEODE);
            if (stage > this.stage) {
                this.grownAt = this.tickCount;
            }
            this.stage = stage;
            this.setBoundingBox(this.makeBoundingBox());
        }
        if (DATA_LIGHT.equals(key)) {
            byte light = this.entityData.get(DATA_LIGHT);
            if (light == LIT && this.shownLight != LIT) {
                this.litAt = this.tickCount;
            } else if (light != LIT && this.shownLight == LIT) {
                this.unlitAt = this.tickCount;
            }
            this.shownLight = light;
        }
    }

    // the box stands out from the face the crystal grows on
    @Override
    protected AABB makeBoundingBox() {
        Direction out = this.face == null ? Direction.UP : this.face;
        float[] size = SIZES[Mth.clamp(this.stage, SEED, GEODE)];
        double half = size[0] / 2.0;
        Vec3 at = this.position();
        Vec3 far = at.add(out.getStepX() * size[1], out.getStepY() * size[1], out.getStepZ() * size[1]);
        double spreadX = out.getAxis() == Direction.Axis.X ? 0.0 : half;
        double spreadY = out.getAxis() == Direction.Axis.Y ? 0.0 : half;
        double spreadZ = out.getAxis() == Direction.Axis.Z ? 0.0 : half;
        return new AABB(at, far).inflate(spreadX, spreadY, spreadZ);
    }

    // How wide the crystal has grown, as a share of a Geode's width.
    public float widthScale() {
        return SIZES[Mth.clamp(this.stage, SEED, GEODE)][0] / SIZES[GEODE][0];
    }

    // The middle of the crystal, where bursts go off and projectiles and beams bounce from.
    public Vec3 center() {
        return this.getBoundingBox().getCenter();
    }

    // The colour the crystal shows now: its own, or for a Prismatic one a hue that keeps turning.
    @Override
    public Vector3f shownColor(float partialTicks) {
        if (!this.prismatic) {
            return this.color.vector();
        }
        return ColorUtil.vector(Mth.hsvToRgb(((this.tickCount + partialTicks) * 0.02F + this.getId() * 0.13F) % 1.0F, 0.75F, 1.0F));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            byte light = this.getLight();
            // a Prismatic crystal sheds rainbow sparkles, the more often for its aura
            int chance = light == LIT ? 2 : this.prismatic ? 3 : this.stage == GEODE ? 8 : 0;
            if (chance > 0 && this.random.nextInt(chance) == 0) {
                Vec3 c = this.center();
                AABB box = this.getBoundingBox();
                Vector3f color = this.prismatic ? ColorUtil.vector(Mth.hsvToRgb(this.random.nextFloat(), 0.7F, 1.0F)) : this.shownColor(0.0F);
                this.level.addParticle(new DustParticleOptions(color, 1.0F),
                        c.x + (this.random.nextDouble() - 0.5) * box.getXsize() * 1.6,
                        c.y + (this.random.nextDouble() - 0.5) * box.getYsize() * 1.6,
                        c.z + (this.random.nextDouble() - 0.5) * box.getZsize() * 1.6, 0.0, 0.0, 0.0);
            }
            return;
        }
        SimonsSet.tickCrystal(this);
    }

    // ---------- state the server drives ----------

    @Nullable
    public UUID getOwner() {
        return this.owner;
    }

    public Direction getFace() {
        return this.face;
    }

    public Color getColor() {
        return this.color;
    }

    public boolean isPrismatic() {
        return this.prismatic;
    }

    // Budded off another crystal, rather than planted from a thrown seed.
    public boolean isNatural() {
        return this.natural;
    }

    public int getStage() {
        return this.stage;
    }

    public void setStage(int stage) {
        this.stage = Mth.clamp(stage, SEED, GEODE);
        this.entityData.set(DATA_STAGE, (byte) this.stage);
        this.setBoundingBox(this.makeBoundingBox());
        if (this.stage < GEODE) {
            this.setFacets(0);
        }
    }

    public byte getLight() {
        return this.entityData.get(DATA_LIGHT);
    }

    public void setLight(byte light) {
        this.entityData.set(DATA_LIGHT, light);
    }

    public int getFacets() {
        return this.entityData.get(DATA_FACETS);
    }

    public void setFacets(int facets) {
        this.entityData.set(DATA_FACETS, (byte) Mth.clamp(facets, 0, 3));
    }

    // Client: ticks since the crystal last grew or appeared.
    public int ticksSinceGrown() {
        return this.tickCount - this.grownAt;
    }

    // Client: ticks since the crystal last lit up in a sequence.
    public int ticksSinceLit() {
        return this.tickCount - this.litAt;
    }

    // Client: ticks since the crystal last went out after being lit (huge if it never has).
    public int ticksSinceUnlit() {
        return this.tickCount - this.unlitAt;
    }

    // ---------- an object, not a creature ----------

    // swings (sweeps included) never target it; sequence hits arrive as aimed clicks
    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64.0 * 64.0;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeByte(this.face.get3DDataValue());
        buffer.writeByte(this.color.ordinal());
        buffer.writeBoolean(this.prismatic);
        buffer.writeByte(this.stage);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.face = Direction.from3DDataValue(buffer.readByte());
        Color[] colors = Color.values();
        this.color = colors[Mth.clamp(buffer.readByte(), 0, colors.length - 1)];
        this.prismatic = buffer.readBoolean();
        this.stage = Mth.clamp(buffer.readByte(), SEED, GEODE);
        this.setBoundingBox(this.makeBoundingBox());
    }
}
