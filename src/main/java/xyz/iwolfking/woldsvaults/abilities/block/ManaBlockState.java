package xyz.iwolfking.woldsvaults.abilities.block;

import iskallia.vault.core.event.CommonEvents;
import iskallia.vault.core.event.common.ManaModifyEvent;
import iskallia.vault.core.event.common.PlayerStatEvent;
import iskallia.vault.mana.Mana;
import iskallia.vault.mana.ManaAction;
import iskallia.vault.util.calc.PlayerStat;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.combat.FinalDamageStage;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ManaBlockState {
    private static final ResourceLocation MANA_SHIELD_STAGE = WoldsVaults.id("mana_block_mana_shield");
    private static final Object LISTENER_REF = new Object();
    private static final int PROGRESS_INTERVAL_TICKS = 10;

    private static final float MANA_PER_DAMAGE = 1.0F;

    private static final Map<UUID, BlockProgress<ManaBlockAbility>> BLOCKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Buff> BUFFS = new ConcurrentHashMap<>();

    private ManaBlockState() {
    }

    public static void register() {
        FinalDamageStage.register(MANA_SHIELD_STAGE, FinalDamageStage.ORDER_REDUCTION, ManaBlockState::manaShield);
        CommonEvents.MANA_MODIFY.register(LISTENER_REF, ManaBlockState::onManaModify);
        CommonEvents.PLAYER_STAT.of(PlayerStat.AREA_OF_EFFECT).register(LISTENER_REF, ManaBlockState::surgingMana);
    }

    public static boolean isBlocking(Player player) {
        return BLOCKS.containsKey(player.getUUID());
    }

    public static void raise(ServerPlayer player, ManaBlockAbility ability) {
        BlockProgress<ManaBlockAbility> block = new BlockProgress<>(ability,
                Mana.getMax(player) * ability.getBlockMultiplier(), player.server.getTickCount());
        BLOCKS.put(player.getUUID(), block);
        block.showProgress(player, "message.woldsvaults.mana_block.progress", ChatFormatting.AQUA);
    }

    private static void onManaModify(ManaModifyEvent.Data data) {
        if (data.getAction() == ManaAction.SYSTEM || data.getNewAmount() <= data.getOldAmount()
                || !(data.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        BlockProgress<ManaBlockAbility> block = BLOCKS.get(player.getUUID());
        if (block == null) {
            return;
        }
        Mana.set(player, ManaAction.SYSTEM, data.getOldAmount());
        if (block.lift(data.getNewAmount() - data.getOldAmount())) {
            BLOCKS.remove(player.getUUID());
            grantBuffs(player, block);
        }
    }

    private static void grantBuffs(ServerPlayer player, BlockProgress<ManaBlockAbility> block) {
        ManaBlockAbility ability = block.ability;
        int clearTicks = block.clearTicks(player);
        int buffTicks = ability.getBuffTicks(clearTicks);
        BUFFS.put(player.getUUID(), new Buff(ability, buffTicks));
        BlockProgress.announceLift(player, "message.woldsvaults.mana_block.lifted", ChatFormatting.AQUA, clearTicks, buffTicks);
    }

    private static float manaShield(LivingDamageEvent event, float amount) {
        if (amount <= 0.0F || event.getSource().isBypassInvul()
                || !(event.getEntityLiving() instanceof ServerPlayer player)) {
            return amount;
        }
        Buff buff = BUFFS.get(player.getUUID());
        if (buff == null || buff.manaShield <= 0.0F) {
            return amount;
        }
        float taken = Math.min(amount * buff.manaShield, Mana.get(player) / MANA_PER_DAMAGE);
        if (taken <= 0.0F) {
            return amount;
        }
        Mana.decrease(player, ManaAction.NEGATIVE_GAME_MECHANIC, taken * MANA_PER_DAMAGE);
        return amount - taken;
    }

    private static void surgingMana(PlayerStatEvent.Data data) {
        if (data.getEntity() instanceof ServerPlayer player) {
            Buff buff = BUFFS.get(player.getUUID());
            if (buff != null) {
                data.setValue(data.getValue() + buff.surgingAoe);
            }
        }
    }

    public static void tick(MinecraftServer server) {
        if (!BLOCKS.isEmpty() && server.getTickCount() % PROGRESS_INTERVAL_TICKS == 0) {
            BLOCKS.forEach((id, block) -> {
                ServerPlayer player = server.getPlayerList().getPlayer(id);
                if (player != null) {
                    block.showProgress(player, "message.woldsvaults.mana_block.progress", ChatFormatting.AQUA);
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
            if (player == null || --buff.remainingTicks <= 0) {
                buffs.remove();
                continue;
            }
            float max = Mana.getMax(player);
            if (buff.manaRegen > 0.0F && Mana.get(player) < max) {
                Mana.increase(player, ManaAction.POSITIVE_GAME_MECHANIC, max * buff.manaRegen / 20.0F);
            }
        }
    }

    public static void clear(Player player) {
        BLOCKS.remove(player.getUUID());
        BUFFS.remove(player.getUUID());
    }

    public static void clearAll() {
        BLOCKS.clear();
        BUFFS.clear();
    }

    private static final class Buff {
        private final float manaShield;
        private final float manaRegen;
        private final float surgingAoe;
        private int remainingTicks;

        private Buff(ManaBlockAbility ability, int durationTicks) {
            this.manaShield = ability.getManaShield();
            this.manaRegen = ability.getManaRegen();
            this.surgingAoe = ability.getSurgingAoe();
            this.remainingTicks = durationTicks;
        }
    }
}
