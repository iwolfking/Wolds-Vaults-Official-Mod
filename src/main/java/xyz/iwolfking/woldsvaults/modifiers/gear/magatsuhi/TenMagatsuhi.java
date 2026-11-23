package xyz.iwolfking.woldsvaults.modifiers.gear.magatsuhi;

import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.BlockEvent;
import xyz.iwolfking.woldsvaults.integration.bettercombat.BetterCombatAttacks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.gear.sets.GearSetHelper;
import xyz.iwolfking.woldsvaults.config.MagatsuhiConfig;
import xyz.iwolfking.woldsvaults.init.ModGearAttributes;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.modifiers.gear.riptide.RiptideDerivedHits;
import xyz.iwolfking.woldsvaults.network.message.MagatsuhiFxMessage;
import xyz.iwolfking.woldsvaults.network.message.MagatsuhiSetMessage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Ten Magatsuhi, the dual katana set: Daikyo and Shokyo, two swords dual wielded (either hand counts, see GearSetHelper).
 * With the magatsuhi_combo grant (one piece) every comboHits-th weapon swing in a row, from either hand, is a finisher:
 * each enemy that swing hits takes the grant's extra damage, both katanas crossing through it in an X. Only the swing's
 * own targets count, not sweeps or other damage; the count starts over when the wearer stops hitting for comboResetTicks.
 * With the magatsuhi_cut grant (both pieces) a right click with either katana works in three steps. The first has its twin cut a
 * 1 wide, 2 tall square out of what's aimed at: the blocks of a wall, floor or ceiling, an enemy, or the air. The second
 * swaps the wearer with the cut: an enemy trades places with them; a wall's blocks are lifted out to where they stood
 * (into air only; anything that can't go there is held) and they step into the hole; the air just takes them there.
 * Both katanas then slash every enemy around where they land. The third puts it all back: the moved blocks return to
 * the hole, an enemy still close enough trades places again, and the wearer goes back to where they started. A block
 * broken while it was moved stays gone, so nothing can be duplicated. The swap also ends by itself after returnTicks,
 * and a logout, death, dimension change or server stop puts the blocks back. Only blocks the wearer could break here
 * can be cut: no tile entities, fluids, unbreakable or wither-immune blocks, doors, beds, tall plants or portals, and
 * nothing that protection (claims, spawn protection, adventure mode) refuses to let them break. Clients draw the cut, a translucent afterimage where the wearer left from on each
 * teleport, and the crossed slash.
 */
@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID)
public final class TenMagatsuhi {
    private static final int SYNC_INTERVAL = 10;
    private static final int STEP_GAP_TICKS = 6;
    private static final int AFTERIMAGE_TICKS = 14;
    private static final int SLASH_TICKS = 8;
    private static final int CROSS_TICKS = 10;
    private static final double FX_RANGE = 64.0;
    private static final Set<ResourceLocation> KATANAS = Set.of(WoldsVaults.id("daikyo"), WoldsVaults.id("shokyo"));

    private static final Map<UUID, Rift> RIFTS = new HashMap<>();
    private static final Map<UUID, Combo> COMBOS = new HashMap<>();
    private static final Map<UUID, Swing> SWINGS = new HashMap<>();
    private static final Map<UUID, Long> READY = new HashMap<>();
    private static final Map<UUID, Boolean> SYNCED = new HashMap<>();

    private enum Kind { BLOCKS, ENEMY, AIR }

    /** A cut, then the swap it led to. */
    private static final class Rift {
        final ServerLevel level;
        final Kind kind;
        final BlockPos bottom;
        @Nullable final Mob enemy;
        final Vec3 point;
        final MagatsuhiFxMessage mark;
        long expires;
        long lastStep;
        boolean swapped;
        Vec3 origin;
        final BlockState[] cut = new BlockState[2];
        final boolean[] placed = new boolean[2];
        BlockPos placedAt;

        Rift(ServerLevel level, Kind kind, BlockPos bottom, @Nullable Mob enemy, Vec3 point, MagatsuhiFxMessage mark) {
            this.level = level;
            this.kind = kind;
            this.bottom = bottom;
            this.enemy = enemy;
            this.point = point;
            this.mark = mark;
        }
    }

    private static final class Combo {
        int hits;
        long lastHit;
        long lastSwingTick = -1L;
        long finisherTick = -1L;
    }

    private record Swing(int targetId, long tick) {
    }

    private TenMagatsuhi() {
    }

    private static float floatGrant(Player player, VaultGearAttribute<Float> attribute) {
        return AttributeSnapshotHelper.getInstance().getSnapshot(player).getAttributeValue(attribute, VaultGearAttributeTypeMerger.floatSum());
    }

    @Nullable
    private static MagatsuhiConfig tuning() {
        return MagatsuhiConfig.installed();
    }

    /** Records the target of a weapon swing from either hand, so only that swing's own hit can count toward the combo. */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || floatGrant(player, ModGearAttributes.MAGATSUHI_COMBO) <= 0.0F) {
            return;
        }
        if (!isWeapon(player.getItemInHand(BetterCombatAttacks.attackingHand(player)))) {
            return;
        }
        SWINGS.put(player.getUUID(), new Swing(event.getTarget().getId(), player.getLevel().getGameTime()));
    }

    private static boolean isWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem;
    }

    /** Every comboHits-th swing in a row is a finisher: all of that swing's targets take the extra damage. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player) || source.getDirectEntity() != player || !"player".equals(source.getMsgId())
                || source instanceof RiptideDerivedHits.Source || !(event.getEntityLiving() instanceof Mob mob)) {
            return;
        }
        long now = player.getLevel().getGameTime();
        Swing swing = SWINGS.get(player.getUUID());
        if (swing == null || swing.targetId() != mob.getId() || swing.tick() != now) {
            return;
        }
        SWINGS.remove(player.getUUID());
        MagatsuhiConfig tuning = tuning();
        float bonus = floatGrant(player, ModGearAttributes.MAGATSUHI_COMBO);
        if (tuning == null || bonus <= 0.0F) {
            return;
        }
        Combo combo = COMBOS.computeIfAbsent(player.getUUID(), id -> new Combo());
        if (combo.lastSwingTick != now) {
            if (now - combo.lastHit > tuning.comboResetTicks) {
                combo.hits = 0;
            }
            combo.lastHit = now;
            combo.lastSwingTick = now;
            combo.hits++;
            if (combo.hits >= Math.max(1, tuning.comboHits)) {
                combo.hits = 0;
                combo.finisherTick = now;
            }
        }
        if (combo.finisherTick != now) {
            return;
        }
        event.setAmount(event.getAmount() * (1.0F + bonus));
        AABB box = mob.getBoundingBox();
        Vec3 at = box.getCenter();
        player.swing(InteractionHand.MAIN_HAND, true);
        player.swing(InteractionHand.OFF_HAND, true);
        float size = (float) Math.max(1.6, Math.max(box.getXsize(), box.getYsize()) * 1.3);
        ModNetwork.sendNear(player.getLevel(), at, FX_RANGE, new MagatsuhiFxMessage(MagatsuhiFxMessage.Kind.CROSS, player.getId(), at, -1, player.getYRot(), -1,
                size, 0.0F, CROSS_TICKS));
        player.getLevel().sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 12, 0.3, 0.4, 0.3, 0.3);
        sound(player.getLevel(), at, SoundEvents.PLAYER_ATTACK_CRIT, 1.0F, 0.7F);
        sound(player.getLevel(), at, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.3F);
    }

    /** A right click with a katana: cut if there's no cut yet, swap with the cut, or return from the swap. */
    public static void act(ServerPlayer player) {
        MagatsuhiConfig tuning = tuning();
        if (tuning == null || !player.isAlive() || player.isSpectator()
                || !GearSetHelper.uniqueKey(player.getMainHandItem()).filter(KATANAS::contains).isPresent()) {
            return;
        }
        UUID id = player.getUUID();
        long now = player.getLevel().getGameTime();
        Rift rift = RIFTS.get(id);
        if (rift != null) {
            if (now - rift.lastStep < STEP_GAP_TICKS) {
                return;
            }
            rift.lastStep = now;
            if (rift.swapped) {
                end(player, rift, true);
            } else {
                swap(player, rift, tuning);
            }
            return;
        }
        if (floatGrant(player, ModGearAttributes.MAGATSUHI_CUT) <= 0.0F || now < READY.getOrDefault(id, 0L)) {
            return;
        }
        cut(player, tuning, now);
    }

    private static void cut(ServerPlayer player, MagatsuhiConfig tuning, long now) {
        ServerLevel level = player.getLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(tuning.cutRange));
        BlockHitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double reach = block.getType() == HitResult.Type.MISS ? tuning.cutRange : block.getLocation().distanceTo(eye);
        Vec3 reachEnd = eye.add(look.scale(reach));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(level, player, eye, reachEnd, player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0),
                entity -> entity instanceof Mob mob && mob.isAlive() && GearSetHelper.isEnemy(mob));
        float yaw = player.getYRot();
        Rift rift;
        if (hit != null && hit.getEntity() instanceof Mob mob) {
            AABB box = mob.getBoundingBox();
            float size = (float) Math.max(1.0, Math.max(box.getXsize(), box.getYsize()) + 0.3);
            MagatsuhiFxMessage mark = new MagatsuhiFxMessage(MagatsuhiFxMessage.Kind.MARK, player.getId(), box.getCenter(), -1, yaw, mob.getId(), size, size, tuning.markTicks);
            rift = new Rift(level, Kind.ENEMY, mob.blockPosition(), mob, mob.position(), mark);
        } else if (block.getType() == HitResult.Type.BLOCK) {
            Direction face = block.getDirection();
            BlockPos pos = block.getBlockPos();
            BlockPos bottom = face == Direction.UP ? pos.below() : pos;
            if (!cuttable(player, level, bottom) || !cuttable(player, level, bottom.above())) {
                fizz(player);
                return;
            }
            Vec3 center;
            float height;
            if (face.getAxis().isHorizontal()) {
                center = Vec3.atBottomCenterOf(bottom).add(0.0, 1.0, 0.0).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.505));
                height = 2.0F;
            } else {
                center = Vec3.atCenterOf(pos).add(0.0, face == Direction.UP ? 0.505 : -0.505, 0.0);
                height = 1.0F;
            }
            MagatsuhiFxMessage mark = new MagatsuhiFxMessage(MagatsuhiFxMessage.Kind.MARK, player.getId(), center, face.ordinal(), yaw, -1, 1.0F, height, tuning.markTicks);
            rift = new Rift(level, Kind.BLOCKS, bottom, null, Vec3.atBottomCenterOf(bottom), mark);
        } else {
            Vec3 center = eye.add(look.scale(Math.min(tuning.airDistance, reach)));
            MagatsuhiFxMessage mark = new MagatsuhiFxMessage(MagatsuhiFxMessage.Kind.MARK, player.getId(), center, -1, yaw, -1, 1.0F, 2.0F, tuning.markTicks);
            rift = new Rift(level, Kind.AIR, new BlockPos(center), null, center.subtract(0.0, 1.0, 0.0), mark);
        }
        rift.expires = now + tuning.markTicks;
        rift.lastStep = now;
        RIFTS.put(player.getUUID(), rift);
        player.swing(InteractionHand.OFF_HAND, true);
        ModNetwork.sendNear(level, player.position(), FX_RANGE, rift.mark);
        sound(level, markAt(rift), SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.6F);
        sound(level, player.position(), SoundEvents.ENDER_EYE_DEATH, 0.6F, 1.5F);
    }

    /**
     * Whether the block at pos can be lifted out: air, or a block the player could break here with a tool. Tile entities,
     * fluids, unbreakable and wither-immune blocks, two-block blocks (doors, beds, tall plants), pistons and portals are
     * refused, as is anything protection refuses: adventure mode, spawn protection or a cancelled break event.
     */
    private static boolean cuttable(ServerPlayer player, ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        if (state.hasBlockEntity() || !state.getFluidState().isEmpty() || state.getDestroySpeed(level, pos) < 0.0F
                || state.is(BlockTags.WITHER_IMMUNE)) {
            return false;
        }
        Block block = state.getBlock();
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) || state.hasProperty(BlockStateProperties.BED_PART)
                || block instanceof DoorBlock || block instanceof DoublePlantBlock || block instanceof PistonHeadBlock
                || block instanceof NetherPortalBlock) {
            return false;
        }
        if (player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer()) || !level.mayInteract(player, pos)) {
            return false;
        }
        return !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player));
    }

    /** Whether the player may set a block down at pos: spawn protection and a cancelled place event refuse it. */
    private static boolean mayPlace(ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (!level.mayInteract(player, pos)) {
            return false;
        }
        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
        return !MinecraftForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(snapshot, level.getBlockState(pos.below()), player));
    }

    private static void swap(ServerPlayer player, Rift rift, MagatsuhiConfig tuning) {
        ServerLevel level = player.getLevel();
        if (level != rift.level) {
            drop(player, rift);
            return;
        }
        Vec3 from = player.position();
        switch (rift.kind) {
            case ENEMY -> {
                Mob enemy = rift.enemy;
                if (enemy == null || !enemy.isAlive() || enemy.level != level) {
                    drop(player, rift);
                    fizz(player);
                    return;
                }
                Vec3 to = enemy.position();
                if (!fits(player, to)) {
                    fizz(player);
                    return;
                }
                afterimage(player, from);
                enemy.teleportTo(from.x, from.y, from.z);
                teleport(player, to, true);
            }
            case BLOCKS -> {
                for (int i = 0; i < 2; i++) {
                    if (!cuttable(player, level, rift.bottom.above(i))) {
                        drop(player, rift);
                        fizz(player);
                        return;
                    }
                }
                BlockPos feet = player.blockPosition();
                afterimage(player, from);
                for (int i = 0; i < 2; i++) {
                    BlockPos at = rift.bottom.above(i);
                    BlockState state = level.getBlockState(at);
                    rift.cut[i] = state.isAir() ? null : state;
                    if (rift.cut[i] != null) {
                        level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
                teleport(player, rift.point, true);
                rift.placedAt = feet;
                for (int i = 0; i < 2; i++) {
                    BlockState state = rift.cut[i];
                    BlockPos at = feet.above(i);
                    if (state != null && !(state.getBlock() instanceof FallingBlock) && level.getBlockState(at).isAir() && state.canSurvive(level, at)
                            && mayPlace(player, level, at)) {
                        level.setBlock(at, state, Block.UPDATE_ALL);
                        rift.placed[i] = true;
                    }
                }
            }
            case AIR -> {
                if (!fits(player, rift.point)) {
                    fizz(player);
                    return;
                }
                afterimage(player, from);
                teleport(player, rift.point, true);
            }
        }
        rift.origin = from;
        rift.swapped = true;
        rift.expires = level.getGameTime() + tuning.returnTicks;
        clearMark(player, rift);
        slash(player, tuning);
    }

    /**
     * Both katanas slash on arrival, hitting every enemy around for the magatsuhi_cut grant's share of attack damage.
     */
    private static void slash(ServerPlayer player, MagatsuhiConfig tuning) {
        ServerLevel level = player.getLevel();
        Vec3 at = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
        player.swing(InteractionHand.MAIN_HAND, true);
        player.swing(InteractionHand.OFF_HAND, true);
        float damage = Math.max(1.0F, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE)) * floatGrant(player, ModGearAttributes.MAGATSUHI_CUT);
        if (damage > 0.0F) {
            for (Mob mob : GearSetHelper.enemiesNear(level, at, tuning.slashRadius)) {
                RiptideDerivedHits.queue(player, mob, damage);
                Vec3 c = mob.getBoundingBox().getCenter();
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, c.x, c.y, c.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        ModNetwork.sendNear(level, at, FX_RANGE, new MagatsuhiFxMessage(MagatsuhiFxMessage.Kind.SLASH, player.getId(), at, -1, player.getYRot(), -1,
                tuning.slashRadius * 2.0F, 0.0F, SLASH_TICKS));
        sound(level, at, SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 0.8F);
        sound(level, at, SoundEvents.PLAYER_ATTACK_STRONG, 1.0F, 0.9F);
    }

    /**
     * Puts everything back. teleport says whether the wearer goes back too (not after a death or a dimension change).
     */
    private static void end(ServerPlayer player, Rift rift, boolean teleport) {
        if (!rift.swapped) {
            drop(player, rift);
            return;
        }
        MagatsuhiConfig tuning = tuning();
        RIFTS.remove(player.getUUID());
        READY.put(player.getUUID(), player.getLevel().getGameTime() + (tuning == null ? 0 : tuning.cooldownTicks));
        ServerLevel level = rift.level;
        boolean back = teleport && player.getLevel() == level && player.isAlive();
        Vec3 here = player.position();
        switch (rift.kind) {
            case BLOCKS -> {
                boolean[] held = new boolean[2];
                for (int i = 0; i < 2; i++) {
                    if (rift.cut[i] == null) {
                        continue;
                    }
                    if (!rift.placed[i]) {
                        held[i] = true;
                        continue;
                    }
                    BlockPos at = rift.placedAt.above(i);
                    if (level.getBlockState(at) == rift.cut[i]) {
                        level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        held[i] = true;
                    }
                }
                if (back) {
                    afterimage(player, here);
                    teleport(player, rift.origin, true);
                }
                for (int i = 0; i < 2; i++) {
                    if (!held[i]) {
                        continue;
                    }
                    BlockPos hole = rift.bottom.above(i);
                    if (level.getBlockState(hole).isAir()) {
                        level.setBlock(hole, rift.cut[i], Block.UPDATE_ALL);
                    } else {
                        Block.dropResources(rift.cut[i], level, hole);
                    }
                }
            }
            case ENEMY -> {
                if (back) {
                    afterimage(player, here);
                    teleport(player, rift.origin, true);
                    Mob enemy = rift.enemy;
                    double range = tuning == null ? 32.0 : tuning.swapBackRange;
                    if (enemy != null && enemy.isAlive() && enemy.level == level && enemy.position().distanceTo(here) <= range) {
                        enemy.teleportTo(here.x, here.y, here.z);
                    }
                }
            }
            case AIR -> {
                if (back) {
                    afterimage(player, here);
                    teleport(player, rift.origin, true);
                }
            }
        }
    }

    /** A cut that never got its swap. */
    private static void drop(ServerPlayer player, Rift rift) {
        RIFTS.remove(player.getUUID());
        clearMark(player, rift);
    }

    private static void clearMark(ServerPlayer player, Rift rift) {
        ModNetwork.sendNear(rift.level, markAt(rift), FX_RANGE, new MagatsuhiFxMessage(MagatsuhiFxMessage.Kind.CLEAR, player.getId(), markAt(rift), -1, 0.0F, -1, 0.0F, 0.0F, 0));
    }

    private static Vec3 markAt(Rift rift) {
        return new Vec3(rift.mark.x(), rift.mark.y(), rift.mark.z());
    }

    private static boolean fits(ServerPlayer player, Vec3 feet) {
        return player.getLevel().noCollision(player, player.getBoundingBox().move(feet.subtract(player.position())));
    }

    /**
     * Live = through the connection; otherwise (the player is leaving) just moved, so it's saved where it should be
     */
    private static void teleport(ServerPlayer player, Vec3 to, boolean live) {
        player.fallDistance = 0.0F;
        if (live) {
            player.teleportTo(to.x, to.y, to.z);
        } else {
            player.moveTo(to.x, to.y, to.z);
        }
        sound(player.getLevel(), to, SoundEvents.ENDERMAN_TELEPORT, 0.5F, 1.6F);
    }

    private static void afterimage(ServerPlayer player, Vec3 feet) {
        ModNetwork.sendNear(player.getLevel(), feet, FX_RANGE, new MagatsuhiFxMessage(MagatsuhiFxMessage.Kind.AFTERIMAGE, player.getId(), feet, -1, player.getYRot(), -1,
                player.getBbWidth(), player.getBbHeight(), AFTERIMAGE_TICKS));
    }

    private static void fizz(ServerPlayer player) {
        sound(player.getLevel(), player.position(), SoundEvents.SHIELD_BLOCK, 0.6F, 1.6F);
    }

    private static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        UUID id = player.getUUID();
        if (player.tickCount % SYNC_INTERVAL == 0) {
            boolean canCut = floatGrant(player, ModGearAttributes.MAGATSUHI_CUT) > 0.0F;
            if (canCut != SYNCED.getOrDefault(id, false)) {
                SYNCED.put(id, canCut);
                ModNetwork.sendToClient(new MagatsuhiSetMessage(canCut), player);
            }
            Rift rift = RIFTS.get(id);
            if (rift != null && !canCut) {
                end(player, rift, true);
            }
        }
        Rift rift = RIFTS.get(id);
        if (rift == null) {
            return;
        }
        if (player.getLevel() != rift.level) {
            end(player, rift, false);
        } else if (!rift.swapped && (player.getLevel().getGameTime() > rift.expires || rift.enemy != null && !rift.enemy.isAlive())) {
            drop(player, rift);
        } else if (rift.swapped && player.getLevel().getGameTime() > rift.expires) {
            end(player, rift, true);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
            Rift rift = RIFTS.get(player.getUUID());
            if (rift != null) {
                end(player, rift, false);
            }
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            Rift rift = RIFTS.get(player.getUUID());
            if (rift != null) {
                end(player, rift, false);
            }
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            UUID id = player.getUUID();
            Rift rift = RIFTS.get(id);
            if (rift != null) {
                leave(player, rift);
            }
            COMBOS.remove(id);
            SWINGS.remove(id);
            READY.remove(id);
            SYNCED.remove(id);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for (ServerPlayer player : new ArrayList<>(event.getServer().getPlayerList().getPlayers())) {
            Rift rift = RIFTS.get(player.getUUID());
            if (rift != null) {
                leave(player, rift);
            }
        }
        RIFTS.clear();
        COMBOS.clear();
        SWINGS.clear();
        READY.clear();
        SYNCED.clear();
    }

    /** The wearer is leaving the server: put everything back and save them where they started. */
    private static void leave(ServerPlayer player, Rift rift) {
        Vec3 origin = rift.origin;
        boolean swapped = rift.swapped && player.getLevel() == rift.level;
        end(player, rift, false);
        if (swapped && origin != null) {
            teleport(player, origin, false);
        }
    }
}
