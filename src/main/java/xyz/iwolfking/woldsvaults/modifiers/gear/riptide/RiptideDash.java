package xyz.iwolfking.woldsvaults.modifiers.gear.riptide;

import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.snapshot.AttributeSnapshot;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.items.gear.VaultTridentItem;
import xyz.iwolfking.woldsvaults.spellcraft.scope.ChainScope;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The single entry point for riptide dashes. Tracks each dash (direction, the mobs it touched, the final damage of
 * its first contact) separately on the logical client and server, tells the riptide mechanics when a dash starts,
 * ticks, lands a contact and ends, and keeps the riptide item cooldown from dropping the trident out of the
 * player's attribute snapshot.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class RiptideDash {
    private static final Map<UUID, Dash> SERVER_DASHES = new ConcurrentHashMap<>();
    private static final Map<UUID, Dash> CLIENT_DASHES = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> LAST_REFERENCE = new ConcurrentHashMap<>();
    private static final Set<UUID> RIPTIDE_COOLDOWNS = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<Boolean> STARTING_COOLDOWN = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<LivingEntity> CONTACT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> CONTACT_TICKER = new ThreadLocal<>();
    private static final ThreadLocal<Float> CONTACT_FALL = new ThreadLocal<>();
    private static final ThreadLocal<ServerPlayer> EQUIPMENT_SCAN = new ThreadLocal<>();
    private static final List<Listener> LISTENERS = List.of(new RiptideShockCone(), new RiptideStorm());

    private RiptideDash() {
    }

    /** Server-side callbacks for one riptide mechanic. */
    public interface Listener {
        default void onStart(ServerPlayer player, Dash dash) {
        }

        default void onTick(ServerPlayer player, Dash dash) {
        }

        default void onContact(ServerPlayer player, Dash dash, LivingEntity target, float finalDamage) {
        }

        default void onEnd(ServerPlayer player, Dash dash) {
        }
    }

    /** One dash: its launch direction, the mobs it touched or shocked, and the first contact's final damage. */
    public static final class Dash {
        private final Vec3 direction;
        private final Set<Integer> contacted = new HashSet<>();
        private final Set<Integer> shocked = new HashSet<>();
        private final Set<Integer> struck = new HashSet<>();
        private final List<LivingEntity> pendingShock = new ArrayList<>();
        private float referenceDamage;
        private int ticks;
        private int visualBolts;

        private Dash(Vec3 direction) {
            this.direction = direction;
        }

        public Vec3 getDirection() {
            return this.direction;
        }

        public int getTicks() {
            return this.ticks;
        }

        public float getReferenceDamage() {
            return this.referenceDamage;
        }

        /** Marks a mob as struck by the storm this dash; false if it already was. */
        public boolean markStruck(LivingEntity entity) {
            return this.struck.add(entity.getId());
        }

        /** Marks a mob as shocked this dash; false if it already was. */
        public boolean markShocked(LivingEntity entity) {
            return this.shocked.add(entity.getId());
        }

        public List<LivingEntity> getPendingShock() {
            return this.pendingShock;
        }

        /** Counts a visual lightning bolt for this dash and returns the new count. */
        public int countVisualBolt() {
            return ++this.visualBolts;
        }
    }

    private static Map<UUID, Dash> dashes(Entity entity) {
        return entity.level.isClientSide ? CLIENT_DASHES : SERVER_DASHES;
    }

    /**
     * The riptide level a vault trident launches with: the player's summed snapshot value, so riptide on other gear
     * counts, but only when the trident itself has riptide. Never below the trident's own level.
     */
    public static int riptideLevel(Player player, ItemStack trident) {
        int own = VaultGearData.read(trident).get(ModGearAttributes.TRIDENT_RIPTIDE, VaultGearAttributeTypeMerger.intSum());
        if (own <= 0) {
            return 0;
        }
        if (!AttributeSnapshotHelper.canHaveSnapshot(player)) {
            return own;
        }
        int total = AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(ModGearAttributes.TRIDENT_RIPTIDE, VaultGearAttributeTypeMerger.intSum());
        return Math.max(own, total);
    }

    public static AttributeSnapshot snapshot(Player player) {
        return AttributeSnapshotHelper.getInstance().getSnapshot(player);
    }

    public static boolean hasPierce(Player player) {
        return snapshot(player).getAttributeValue(ModGearAttributes.RIPTIDE_PIERCE, VaultGearAttributeTypeMerger.anyTrue());
    }

    /** Starts tracking a dash on whichever side calls it; {@code VaultTridentItem.releaseUsing} runs on both. */
    public static void start(Player player) {
        Dash dash = new Dash(player.getLookAngle().normalize());
        dashes(player).put(player.getUUID(), dash);
        if (player instanceof ServerPlayer serverPlayer) {
            LISTENERS.forEach(listener -> listener.onStart(serverPlayer, dash));
        }
    }

    /**
     * Whether this touch is the first between the dash and {@code target}. A dash that was not started through a
     * vault trident (a vanilla riptide) is created on its first touch.
     */
    public static boolean markContact(Player player, LivingEntity target) {
        Dash dash = dashes(player).computeIfAbsent(player.getUUID(), id -> new Dash(player.getLookAngle().normalize()));
        return dash.contacted.add(target.getId());
    }

    /**
     * Starts a riptide contact on {@code target}. A vault trident contact is a full-strength swing: the attack
     * strength ticker is filled for the attack and the player's own swing timer is put back by {@link #endContact},
     * so attack-strength gates (Battle Cry, Rampage, lucky hits, on-hit procs) treat every contact as a full swing.
     * The fall distance is cleared for the attack too: a dash is airborne, and the base mod reads an airborne full
     * swing as a jump crit, which skips lucky hits and on-hit procs.
     */
    public static void beginContact(Player player, LivingEntity target) {
        CONTACT.set(target);
        if (!(player.getMainHandItem().getItem() instanceof VaultTridentItem)) {
            return;
        }
        CONTACT_TICKER.set(player.attackStrengthTicker);
        CONTACT_FALL.set(player.fallDistance);
        player.attackStrengthTicker = (int) (1.0 / player.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0) + 1;
        player.fallDistance = 0.0F;
    }

    public static void endContact(Player player) {
        CONTACT.remove();
        Integer ticker = CONTACT_TICKER.get();
        CONTACT_TICKER.remove();
        if (ticker != null) {
            player.attackStrengthTicker = ticker;
        }
        Float fall = CONTACT_FALL.get();
        CONTACT_FALL.remove();
        if (fall != null) {
            player.fallDistance = fall;
        }
    }

    /**
     * Lands a riptide contact on {@code target} without the spin touching it: the same full-strength attack the spin
     * makes on touch, marked as this dash's contact. The shock cone uses it so a dash that touches nothing still has
     * a contact to draw from. False if the target was already contacted this dash.
     */
    public static boolean forceContact(ServerPlayer player, LivingEntity target) {
        if (!markContact(player, target)) {
            return false;
        }
        beginContact(player, target);
        try {
            player.attack(target);
        } finally {
            endContact(player);
        }
        return true;
    }

    /** Whether the hurt event in progress is a riptide contact on {@code target}. */
    public static boolean isContact(LivingEntity target) {
        return CONTACT.get() == target;
    }

    /** Puts the riptide cooldown on the player so the snapshot keeps counting the trident while it runs. */
    public static void startCooldown(ServerPlayer player, Item item, int ticks) {
        STARTING_COOLDOWN.set(true);
        try {
            player.getCooldowns().addCooldown(item, ticks);
        } finally {
            STARTING_COOLDOWN.set(false);
        }
    }

    /** A cooldown on a vault trident is a riptide cooldown only if {@link #startCooldown} set it. */
    public static void onCooldownStarted(ServerPlayer player, Item item) {
        if (!(item instanceof VaultTridentItem)) {
            return;
        }
        if (STARTING_COOLDOWN.get()) {
            RIPTIDE_COOLDOWNS.add(player.getUUID());
        } else {
            RIPTIDE_COOLDOWNS.remove(player.getUUID());
        }
    }

    public static void onCooldownEnded(ServerPlayer player, Item item) {
        if (item instanceof VaultTridentItem) {
            RIPTIDE_COOLDOWNS.remove(player.getUUID());
        }
    }

    public static boolean isRiptideCooldown(ServerPlayer player, Item item) {
        return item instanceof VaultTridentItem && RIPTIDE_COOLDOWNS.contains(player.getUUID());
    }

    public static void beginEquipmentScan(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            EQUIPMENT_SCAN.set(player);
        }
    }

    public static void endEquipmentScan() {
        EQUIPMENT_SCAN.remove();
    }

    /**
     * Whether {@code cooldowns} should report {@code item} as off cooldown: true only during the equipment scan of
     * the player those cooldowns belong to, for a vault trident on its own riptide cooldown.
     */
    public static boolean ignoresRiptideCooldown(ItemCooldowns cooldowns, Item item) {
        ServerPlayer player = EQUIPMENT_SCAN.get();
        return player != null && player.getCooldowns() == cooldowns && isRiptideCooldown(player, item);
    }

    /** The first contact's final damage of the player's latest dash that landed one; 0 if none yet. */
    public static float lastReference(ServerPlayer player) {
        return LAST_REFERENCE.getOrDefault(player.getUUID(), 0.0F);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onContactHurt(LivingHurtEvent event) {
        if (ChainScope.active()) {
            return;
        }
        LivingEntity target = event.getEntityLiving();
        if (target.level.isClientSide || !isContact(target) || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Dash dash = SERVER_DASHES.get(player.getUUID());
        if (dash == null) {
            return;
        }
        float finalDamage = event.getAmount();
        if (dash.referenceDamage <= 0.0F && finalDamage > 0.0F) {
            dash.referenceDamage = finalDamage;
            LAST_REFERENCE.put(player.getUUID(), finalDamage);
        }
        LISTENERS.forEach(listener -> listener.onContact(player, dash, target, finalDamage));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Dash dash = SERVER_DASHES.get(player.getUUID());
        if (dash == null) {
            return;
        }
        if (!player.isAutoSpinAttack()) {
            SERVER_DASHES.remove(player.getUUID());
            LISTENERS.forEach(listener -> listener.onEnd(player, dash));
            return;
        }
        dash.ticks++;
        LISTENERS.forEach(listener -> listener.onTick(player, dash));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getPlayer().getUUID();
        SERVER_DASHES.remove(id);
        CLIENT_DASHES.remove(id);
        LAST_REFERENCE.remove(id);
        RIPTIDE_COOLDOWNS.remove(id);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        SERVER_DASHES.clear();
        CLIENT_DASHES.clear();
        LAST_REFERENCE.clear();
        RIPTIDE_COOLDOWNS.clear();
    }
}
