package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.card.CardDeck;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.CardDeckItem;
import xyz.iwolfking.woldsvaults.items.gear.VaultPlushieItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosCapability;
import xyz.iwolfking.woldsvaults.api.lib.ICardDeckCache;
import xyz.iwolfking.woldsvaults.gods.GodAlignmentData;
import xyz.iwolfking.woldsvaults.gods.GodNodeAttributeSource;
import xyz.iwolfking.woldsvaults.gods.GodTreeAttributeProviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Turns the player's spent Wendarr points into vault gear attributes. Plain stat rows come
 * straight from {@link WendarrNodes#BASIC_STATS}; the three stat-shaped behaviour nodes
 * (The Deckless, Efficient Steps, Plushie Lover) compute their contribution from live player
 * state and are therefore only emitted at {@link GodNodeAttributeSource.Scope#ALL}.
 */
public final class WendarrAttributeProvider implements GodTreeAttributeProviders.Provider {
    public static final float DECKLESS_FRUIT_PER_SLOT = 0.01F;
    public static final float DECKLESS_SPEED_PER_SLOT = 0.05F;
    public static final float DECKLESS_COOLDOWN_PER_SLOT = 0.05F;
    public static final float EFFICIENT_STEPS_RATIO = 0.25F;

    @Override
    public List<VaultGearAttributeInstance<?>> getGearAttributes(ServerPlayer player, GodNodeAttributeSource.Scope scope) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return List.of();
        }
        Map<String, Integer> ledger = GodAlignmentData.get(server).getSpentLedger(player.getUUID(), WendarrNodes.GOD);
        List<VaultGearAttributeInstance<?>> result = new ArrayList<>();
        ledger.forEach((nodeId, points) -> {
            if (points <= 0) {
                return;
            }
            WendarrNodes.StatEntry basic = WendarrNodes.BASIC_STATS.get(nodeId);
            if (basic != null) {
                result.add(VaultGearAttributeInstance.cast(basic.attribute(), basic.perPoint() * points));
                return;
            }
            if (scope == GodNodeAttributeSource.Scope.ALL) {
                addNonBasic(player, nodeId, points, result);
            }
        });
        return result;
    }

    @Override
    public List<VaultGearAttributeInstance<?>> getMinorTransferAttributes(ServerPlayer player, Collection<String> nodeIds) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return List.of();
        }
        GodAlignmentData data = GodAlignmentData.get(server);
        List<VaultGearAttributeInstance<?>> result = new ArrayList<>();
        for (String nodeId : nodeIds) {
            if (!WendarrNodes.owns(nodeId) || !WendarrNodes.MINORS.contains(nodeId)) {
                continue;
            }
            int points = data.getPointsIn(player.getUUID(), WendarrNodes.GOD, nodeId);
            if (points > 0) {
                addNonBasic(player, nodeId, points, result);
            }
        }
        return result;
    }

    private static void addNonBasic(ServerPlayer player, String nodeId, int points, List<VaultGearAttributeInstance<?>> result) {
        WendarrNodes.StatEntry minor = WendarrNodes.MINOR_STATS.get(nodeId);
        if (minor != null) {
            result.add(VaultGearAttributeInstance.cast(minor.attribute(), minor.perPoint() * points));
            return;
        }
        switch (nodeId) {
            case WendarrNodes.THE_DECKLESS -> addDeckless(player, points, result);
            case WendarrNodes.EFFICIENT_STEPS -> addEfficientSteps(player, points, result);
            case WendarrNodes.PLUSHIE_LOVER -> addPlushieCopy(player, result);
            default -> {
            }
        }
    }

    private static void addDeckless(ServerPlayer player, int points, List<VaultGearAttributeInstance<?>> result) {
        int emptySlots = emptyDeckSlots(player);
        if (emptySlots <= 0) {
            return;
        }
        float scale = emptySlots * points;
        result.add(VaultGearAttributeInstance.cast(ModGearAttributes.FRUIT_EFFECTIVENESS, DECKLESS_FRUIT_PER_SLOT * scale));
        result.add(VaultGearAttributeInstance.cast(ModGearAttributes.MOVEMENT_SPEED, DECKLESS_SPEED_PER_SLOT * scale));
        result.add(VaultGearAttributeInstance.cast(ModGearAttributes.COOLDOWN_REDUCTION, DECKLESS_COOLDOWN_PER_SLOT * scale));
    }

    private static void addEfficientSteps(ServerPlayer player, int points, List<VaultGearAttributeInstance<?>> result) {
        float fruitEfficiency = WendarrStatCache.getFruitEfficiency(player);
        if (fruitEfficiency <= 0.0F) {
            return;
        }
        result.add(VaultGearAttributeInstance.cast(ModGearAttributes.MOVEMENT_SPEED, fruitEfficiency * EFFICIENT_STEPS_RATIO * points));
    }

    /**
     * Plushie Lover doubles an equipped plushie by contributing a second copy of every attribute
     * the plushie already rolls, which lands on the snapshot exactly like the gear pass did.
     * Cheaper and far less invasive than a multiplier on the snapshot calculator, and it cannot
     * double anything the player is not actually wearing.
     */
    private static void addPlushieCopy(ServerPlayer player, List<VaultGearAttributeInstance<?>> result) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty() || !(offhand.getItem() instanceof VaultPlushieItem)) {
            return;
        }
        WendarrFocusGear.copyAttributes(offhand, result);
    }

    private static int emptyDeckSlots(ServerPlayer player) {
        Optional<CardDeck> deck = player.getCapability(CuriosCapability.INVENTORY)
                .map(inventory -> inventory.getStacksHandler("deck")
                        .map(handler -> handler.getStacks().getStackInSlot(0))
                        .orElse(ItemStack.EMPTY))
                .filter(stack -> !stack.isEmpty())
                .flatMap(CardDeckItem::getCardDeck);
        return deck.map(cardDeck -> ((ICardDeckCache) cardDeck).wv$getEmptySlotCount()).orElse(0);
    }

}
