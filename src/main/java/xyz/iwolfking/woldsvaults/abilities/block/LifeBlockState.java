package xyz.iwolfking.woldsvaults.abilities.block;

import iskallia.vault.event.ActiveFlags;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;
import xyz.iwolfking.woldsvaults.gods.combat.GlobalDamageMultiplierRegistry;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LifeBlockState {
    public static final String LIFE_BOND_DAMAGE = "woldsvaults.life_bond";

    private static final ResourceLocation LIFE_BOND_STAGE = WoldsVaults.id("life_block_life_bond");
    private static final ResourceLocation TRANSFUSION_FACTOR = WoldsVaults.id("life_block_transfusion");
    private static final int PROGRESS_INTERVAL_TICKS = 10;

    private static final Map<UUID, BlockProgress<LifeBlockAbility>> BLOCKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Buff> BUFFS = new ConcurrentHashMap<>();

    private static final ThreadLocal<Boolean> BONDING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private LifeBlockState() {
    }

    public static void register() {
        FinalDamageStage.register(LIFE_BOND_STAGE, FinalDamageStage.ORDER_SPLIT, LifeBlockState::lifeBond);
        GlobalDamageMultiplierRegistry.excludeDamageSource(LIFE_BOND_DAMAGE);
    }

    /** Whether the damage is Life Bond's reflected hit, which never rolls a lucky hit. */
    public static boolean isLifeBond(DamageSource source) {
        return source instanceof LifeBondDamageSource;
    }

    public static boolean isBlocking(Player player) {
        return BLOCKS.containsKey(player.getUUID());
    }

    public static void raise(ServerPlayer player, LifeBlockAbility ability) {
        BlockProgress<LifeBlockAbility> block = new BlockProgress<>(ability,
                player.getMaxHealth() * ability.getBlockMultiplier(), player.server.getTickCount());
        BLOCKS.put(player.getUUID(), block);
        block.showProgress(player, "message.woldsvaults.life_block.progress", ChatFormatting.RED);
    }

    public static void onHeal(LivingHealEvent event) {
        if (!(event.getEntityLiving() instanceof ServerPlayer player) || event.getAmount() <= 0.0F) {
            return;
        }
        BlockProgress<LifeBlockAbility> block = BLOCKS.get(player.getUUID());
        if (block != null) {
            event.setCanceled(true);
            if (block.lift(event.getAmount())) {
                BLOCKS.remove(player.getUUID());
                grantBuffs(player, block);
            }
            return;
        }
        Buff buff = BUFFS.get(player.getUUID());
        if (buff != null) {
            buff.addStack(player);
        }
    }

    private static void grantBuffs(ServerPlayer player, BlockProgress<LifeBlockAbility> block) {
        LifeBlockAbility ability = block.ability;
        Buff previous = BUFFS.remove(player.getUUID());
        if (previous != null) {
            end(player, previous);
        }
        int clearTicks = block.clearTicks(player);
        int buffTicks = ability.getBuffTicks(clearTicks);
        float before = player.getAbsorptionAmount();
        float shield = player.getMaxHealth() * ability.getAbsorption();
        player.setAbsorptionAmount(Math.max(before, shield));
        BUFFS.put(player.getUUID(), new Buff(ability, buffTicks, Math.max(0.0F, shield - before)));
        BlockProgress.announceLift(player, "message.woldsvaults.life_block.lifted", ChatFormatting.RED, clearTicks, buffTicks);
    }

    private static float lifeBond(LivingDamageEvent event, float amount) {
        if (amount <= 0.0F || BONDING.get() || event.getSource().isBypassInvul()
                || !(event.getEntityLiving() instanceof ServerPlayer player)) {
            return amount;
        }
        Buff buff = BUFFS.get(player.getUUID());
        if (buff == null || buff.lifeBond <= 0.0F || !(event.getSource().getEntity() instanceof LivingEntity attacker)
                || attacker == player || !attacker.isAlive()) {
            return amount;
        }
        float passed = amount * buff.lifeBond;
        BONDING.set(Boolean.TRUE);
        ActiveFlags.IS_THORNS_REFLECTING.push();
        try {
            attacker.hurt(new LifeBondDamageSource(player), passed);
        } finally {
            ActiveFlags.IS_THORNS_REFLECTING.pop();
            BONDING.set(Boolean.FALSE);
        }
        return amount - passed;
    }

    public static void tick(MinecraftServer server) {
        int now = server.getTickCount();
        if (!BLOCKS.isEmpty() && now % PROGRESS_INTERVAL_TICKS == 0) {
            BLOCKS.forEach((id, block) -> {
                ServerPlayer player = server.getPlayerList().getPlayer(id);
                if (player != null) {
                    block.showProgress(player, "message.woldsvaults.life_block.progress", ChatFormatting.RED);
                }
            });
        }
        if (BUFFS.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, Buff>> buffs = BUFFS.entrySet().iterator();
        while (buffs.hasNext()) {
            Map.Entry<UUID, Buff> entry = buffs.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            Buff buff = entry.getValue();
            if (player == null) {
                buffs.remove();
                continue;
            }
            if (--buff.remainingTicks <= 0) {
                buffs.remove();
                end(player, buff);
                continue;
            }
            if (buff.expireStacks(now)) {
                buff.applyTransfusion(player);
            }
        }
    }

    private static void end(Player player, Buff buff) {
        GlobalDamageMultiplierRegistry.remove(player, TRANSFUSION_FACTOR);
        float left = Math.min(player.getAbsorptionAmount(), buff.shieldGranted);
        if (left > 0.0F) {
            player.setAbsorptionAmount(player.getAbsorptionAmount() - left);
        }
    }

    public static void clear(Player player) {
        BLOCKS.remove(player.getUUID());
        Buff buff = BUFFS.remove(player.getUUID());
        if (buff != null) {
            end(player, buff);
        }
    }

    public static void clearAll() {
        BLOCKS.clear();
        BUFFS.clear();
    }

    private static final class Buff {
        private final float transfusionPerStack;
        private final int transfusionMaxStacks;
        private final int transfusionStackTicks;
        private final float lifeBond;
        private final float shieldGranted;
        private final ArrayDeque<Integer> stacks = new ArrayDeque<>();
        private int remainingTicks;

        private Buff(LifeBlockAbility ability, int durationTicks, float shieldGranted) {
            this.transfusionPerStack = ability.getTransfusionPerStack();
            this.transfusionMaxStacks = Math.max(ability.getTransfusionMaxStacks(), 1);
            this.transfusionStackTicks = Math.max(ability.getTransfusionStackTicks(), 1);
            this.lifeBond = ability.getLifeBond();
            this.shieldGranted = shieldGranted;
            this.remainingTicks = durationTicks;
        }

        private void addStack(ServerPlayer player) {
            if (this.stacks.size() >= this.transfusionMaxStacks) {
                this.stacks.pollFirst();
            }
            this.stacks.addLast(player.server.getTickCount() + this.transfusionStackTicks);
            this.applyTransfusion(player);
        }

        private boolean expireStacks(int now) {
            boolean expired = false;
            while (!this.stacks.isEmpty() && this.stacks.peekFirst() <= now) {
                this.stacks.pollFirst();
                expired = true;
            }
            return expired;
        }

        private void applyTransfusion(ServerPlayer player) {
            if (this.stacks.isEmpty()) {
                GlobalDamageMultiplierRegistry.remove(player, TRANSFUSION_FACTOR);
                return;
            }
            int untilNewestExpires = this.stacks.peekLast() - player.server.getTickCount() + 1;
            GlobalDamageMultiplierRegistry.register(player, TRANSFUSION_FACTOR,
                    1.0F + this.transfusionPerStack * this.stacks.size(), Math.max(untilNewestExpires, 1));
        }
    }

    private static final class LifeBondDamageSource extends EntityDamageSource {
        private LifeBondDamageSource(Player player) {
            super(LIFE_BOND_DAMAGE, player);
            this.bypassArmor();
        }
    }
}
