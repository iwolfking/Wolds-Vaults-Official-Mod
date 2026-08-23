package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.card.CardDeck;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.CardDeckItem;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import top.theillusivec4.curios.api.CuriosCapability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import xyz.iwolfking.woldsvaults.api.lib.ICardDeckCache;
import xyz.iwolfking.woldsvaults.gods.GodFocusGear;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.node.DeferredHandler;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodEffectParams;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeHandlers;
import xyz.iwolfking.woldsvaults.gods.node.GodNodePreviews;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodStatSink;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;
import xyz.iwolfking.woldsvaults.gods.node.ListenerBoundHandler;
import xyz.iwolfking.woldsvaults.gods.node.StatContributor;
import xyz.iwolfking.woldsvaults.gods.node.TickContributor;
import xyz.iwolfking.woldsvaults.items.gear.VaultPlushieItem;

import java.util.Optional;

/** Wendarr handler types and their params, whose component names are the config keys verbatim. */
public final class WendarrNodeHandlers {
    private WendarrNodeHandlers() {
    }

    /** Registers every Wendarr handler type. Must run before the god tree configs are validated. */
    public static void register() {
        GodNodeHandlers.register(WendarrNodes.THE_DECKLESS, TheDecklessParams.class, TheDecklessHandler::new);
        GodNodeHandlers.register(WendarrNodes.EFFICIENT_STEPS, EfficientStepsParams.class, EfficientStepsHandler::new);
        GodNodeHandlers.register(WendarrNodes.PLUSHIE_LOVER, PlushieLoverHandler::new);
        GodNodeHandlers.register(WendarrNodes.EXTENDER, ExtenderParams.class,
                WendarrTimeHandlers.ExtenderHandler::new);
        GodNodeHandlers.register(WendarrNodes.SPEED_DEMON, SpeedDemonParams.class,
                WendarrTimeHandlers.SpeedDemonHandler::new);
        GodNodeHandlers.register(WendarrNodes.QUICK_SEARCH, QuickSearchParams.class,
                WendarrTimeHandlers.QuickSearchHandler::new);
        GodNodeHandlers.register(WendarrNodes.PACED_STRIKES, PacedStrikesParams.class,
                WendarrTimeHandlers.PacedStrikesHandler::new);
        GodNodeHandlers.register(WendarrNodes.EDGE_OF_TIME, EdgeOfTimeParams.class,
                WendarrTimeHandlers.EdgeOfTimeHandler::new);
        GodNodeHandlers.register(WendarrNodes.TEMPORAL_SHIELDING, TemporalShieldingParams.class,
                WendarrTimeHandlers.TemporalShieldingHandler::new);
        GodNodeHandlers.register(WendarrNodes.EXPERT_EATER, ExpertEaterParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(WendarrNodes.PRISTINE_CONDITION, PristineConditionParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(WendarrNodes.LEGEND_OF_THE_PEAR, ListenerBoundHandler::new);
        GodNodeHandlers.register(WendarrNodes.GLUTTON, GluttonParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(WendarrNodes.TOUGH_STOMACH, ToughStomachParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(WendarrNodes.GARDENER, GardenerParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(WendarrNodes.PYLON_WHISPERER, PylonWhispererParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(WendarrNodes.CLOCK_ARTIFICIER, ClockArtificierParams.class, ListenerBoundHandler::new);
        GodNodeHandlers.register(WendarrNodes.TEMPORAL_BREAKING, ListenerBoundHandler::new);
        GodNodeHandlers.register(WendarrNodes.MASTER_IMBUER, ListenerBoundHandler::new);
        GodNodeHandlers.register(WendarrNodes.EXTRACTION_SUPERVISER, DeferredHandler::new);
        GodNodeHandlers.register(WendarrNodes.ARMORED_EXTRACTORS, DeferredHandler::new);
        GodNodePreviews.register(WendarrNodes.PACED_STRIKES, GodNodePreviews.PACED_STRIKES_FORMULA,
                WendarrTimeHandlers::previewPacedStrikes);
    }

    /** The loaded parameters of one Wendarr effect. Throws naming the effect if it is absent. */
    public static <T extends GodEffectParams> T params(String effectId, Class<T> type) {
        return GodNodeRegistry.params(VaultGod.WENDARR, effectId, type);
    }

    /** The Deckless: each empty deck slot pays fruit efficiency, speed and cooldown, per point. */
    public record TheDecklessHandler(GodEffect effect) implements StatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            int emptySlots = emptyDeckSlots(context.player());
            if (emptySlots <= 0) {
                return;
            }
            TheDecklessParams params = this.effect.params(TheDecklessParams.class);
            float scale = emptySlots * context.points();
            sink.add(ModGearAttributes.FRUIT_EFFECTIVENESS, params.fruit_per_slot() * scale);
            sink.add(ModGearAttributes.MOVEMENT_SPEED, params.speed_per_slot() * scale);
            sink.add(ModGearAttributes.COOLDOWN_REDUCTION, params.cooldown_per_slot() * scale);
        }
    }

    /**
     * Efficient Steps: a share of fruit efficiency paid back as movement speed. The tick leg
     * samples it into {@link GodNodeState}; the stat leg reads only that sample.
     */
    public record EfficientStepsHandler(GodEffect effect) implements StatContributor, TickContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            float efficiency = sampledEfficiency(context.player(), context.effectId());
            if (efficiency <= 0.0F) {
                return;
            }
            sink.add(ModGearAttributes.MOVEMENT_SPEED,
                    efficiency * this.effect.params(EfficientStepsParams.class).ratio() * context.points());
        }

        @Override
        public void tick(GodNodeContext context) {
            ServerPlayer player = context.player();
            float efficiency = AttributeSnapshotHelper.getInstance().getSnapshot(player)
                    .getAttributeValue(ModGearAttributes.FRUIT_EFFECTIVENESS, VaultGearAttributeTypeMerger.floatSum());
            GodNodeState.put(player.getUUID(), context.effectId(), efficiency);
        }

        @Override
        public void onDeactivated(ServerPlayer player, String effectId) {
            GodNodeState.clear(player.getUUID(), effectId);
        }
    }

    /** Plushie Lover: contributes a second copy of every attribute on an offhand plushie. */
    public record PlushieLoverHandler(GodEffect effect) implements StatContributor {
        @Override
        public void contribute(GodNodeContext context, GodStatSink sink) {
            ItemStack offhand = context.player().getOffhandItem();
            if (offhand.isEmpty() || !(offhand.getItem() instanceof VaultPlushieItem)) {
                return;
            }
            GodFocusGear.copyAttributes(offhand, sink);
        }
    }

    private static float sampledEfficiency(ServerPlayer player, String effectId) {
        return GodNodeState.<Float>peek(player.getUUID(), effectId).orElse(0.0F);
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

    public record TheDecklessParams(float fruit_per_slot, float speed_per_slot,
                                    float cooldown_per_slot) implements GodEffectParams {
    }

    public record EfficientStepsParams(float ratio) implements GodEffectParams {
    }

    public record ExtenderParams(int ticks) implements GodEffectParams {
    }

    public record SpeedDemonParams(float rate, float stat_multiplier) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.rate <= 0.0F) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has rate " + this.rate
                        + "; it divides the length of a vault second and must be greater than zero");
            }
        }
    }

    public record QuickSearchParams(float rate) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.rate <= 0.0F) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has rate " + this.rate
                        + "; it divides the length of a vault second and must be greater than zero");
            }
        }
    }

    public record PacedStrikesParams(float reference_minutes) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.reference_minutes <= 0.0F) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has reference_minutes "
                        + this.reference_minutes + "; it is a divisor and must be greater than zero");
            }
        }
    }

    public record EdgeOfTimeParams(float multiplier, int drain_min_ticks,
                                   int drain_max_ticks) implements GodEffectParams {
        @Override
        public void validate(String effectId) {
            if (this.drain_min_ticks < 0) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has drain_min_ticks "
                        + this.drain_min_ticks + "; it cannot be negative");
            }
            if (this.drain_max_ticks < this.drain_min_ticks) {
                throw GodTreeConfigException.fail("God effect '" + effectId + "' has drain_max_ticks "
                        + this.drain_max_ticks + " below drain_min_ticks " + this.drain_min_ticks
                        + "; the pair is the bound of a random roll");
            }
        }
    }

    public record TemporalShieldingParams(float reduction) implements GodEffectParams {
    }

    public record ExpertEaterParams(float save_chance) implements GodEffectParams {
    }

    public record PristineConditionParams(float rot_multiplier) implements GodEffectParams {
    }

    public record GluttonParams(float time_multiplier, float rot_multiplier) implements GodEffectParams {
    }

    public record ToughStomachParams(double health_scaling) implements GodEffectParams {
    }

    public record GardenerParams(float extra_fruit_chance,
                                 float starfruit_upgrade_chance) implements GodEffectParams {
    }

    public record PylonWhispererParams(float boost) implements GodEffectParams {
    }

    public record ClockArtificierParams(float multiplier) implements GodEffectParams {
    }
}
