package xyz.iwolfking.woldsvaults.gods.trees.wendarr;

import iskallia.vault.core.card.CardDeck;
import iskallia.vault.gear.attribute.type.VaultGearAttributeTypeMerger;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.CardDeckItem;
import iskallia.vault.snapshot.AttributeSnapshotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosCapability;
import xyz.iwolfking.woldsvaults.api.lib.ICardDeckCache;
import xyz.iwolfking.woldsvaults.gods.GodFocusGear;
import xyz.iwolfking.woldsvaults.gods.GodNodeState;
import xyz.iwolfking.woldsvaults.gods.node.GodEffect;
import xyz.iwolfking.woldsvaults.gods.node.GodEffectParams;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeContext;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeHandler;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeHandlers;
import xyz.iwolfking.woldsvaults.gods.node.GodNodeRegistry;
import xyz.iwolfking.woldsvaults.gods.node.GodStatSink;
import xyz.iwolfking.woldsvaults.gods.node.GodTreeConfigException;
import xyz.iwolfking.woldsvaults.gods.node.StatContributor;
import xyz.iwolfking.woldsvaults.gods.node.TickContributor;
import xyz.iwolfking.woldsvaults.items.gear.VaultPlushieItem;

import java.util.Optional;

/**
 * Every handler type the Wendarr tree owns, and the typed parameters each one reads.
 *
 * <p>Five plain stat effects bind the shared {@code gear_attribute_scaled} type and Pious Devotion
 * binds the shared {@code piety} type, so none of those is named here. What is left is one type
 * per effect Java has to know about: the three stat effects computed from live player state, the
 * vault-clock and damage effects in {@link WendarrTimeHandlers}, and the rest as
 * {@link ListenerBound} - numbers in config, validated at load, behaviour still reached through
 * the base mod events, the shared final damage stage and the mixins that own the ordering.
 *
 * <p>Params component names are the config keys verbatim, including their underscores. The codec
 * binds by component name, so renaming one here silently means renaming it in the config file
 * too.
 */
public final class WendarrNodeHandlers {
    private WendarrNodeHandlers() {
    }

    /**
     * Registers every Wendarr handler type. Called from {@code GodNodeHandlerTypes.bootstrap()},
     * which is the only point guaranteed to run before the god tree configs are validated.
     */
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
        GodNodeHandlers.register(WendarrNodes.EXPERT_EATER, ExpertEaterParams.class, ListenerBound::new);
        GodNodeHandlers.register(WendarrNodes.PRISTINE_CONDITION, PristineConditionParams.class, ListenerBound::new);
        GodNodeHandlers.register(WendarrNodes.LEGEND_OF_THE_PEAR, ListenerBound::new);
        GodNodeHandlers.register(WendarrNodes.GLUTTON, GluttonParams.class, ListenerBound::new);
        GodNodeHandlers.register(WendarrNodes.TOUGH_STOMACH, ToughStomachParams.class, ListenerBound::new);
        GodNodeHandlers.register(WendarrNodes.GARDENER, GardenerParams.class, ListenerBound::new);
        GodNodeHandlers.register(WendarrNodes.PYLON_WHISPERER, PylonWhispererParams.class, ListenerBound::new);
        GodNodeHandlers.register(WendarrNodes.CLOCK_ARTIFICIER, ClockArtificierParams.class, ListenerBound::new);
        GodNodeHandlers.register(WendarrNodes.TEMPORAL_BREAKING, ListenerBound::new);
        GodNodeHandlers.register(WendarrNodes.MASTER_IMBUER, ListenerBound::new);
        GodNodeHandlers.register(WendarrNodes.EXTRACTION_SUPERVISER, Deferred::new);
        GodNodeHandlers.register(WendarrNodes.ARMORED_EXTRACTORS, Deferred::new);
    }

    /**
     * The loaded parameters of one Wendarr effect. Absence is a programming error rather than a
     * config error - load-time validation has already asserted that every placed effect exists
     * and resolves its handler - so it fails loudly naming the effect instead of degrading to a
     * zero that would read as a balance change.
     */
    public static <T extends GodEffectParams> T params(String effectId, Class<T> type) {
        GodEffect effect = GodNodeRegistry.effect(effectId).orElse(null);
        if (effect == null) {
            throw GodTreeConfigException.fail("Wendarr effect '" + effectId + "' was read before the god node "
                    + "registry finished loading, or is missing from god_node_effects_wendarr.json");
        }
        return effect.params(type);
    }

    /**
     * An effect whose numbers are config and whose behaviour is reached through the base mod's own
     * events, through the shared final damage stage or through a mixin. It implements no
     * capability on purpose: a handler that claimed one would be dispatched twice, once by the
     * capability driver and once by the listener that actually owns the ordering.
     */
    public record ListenerBound(GodEffect effect) implements GodNodeHandler {
    }

    /**
     * A node the tree places but has no behaviour for yet. Extraction Superviser and Armored
     * Extractors are both this: extraction vaults are shelved (SCOPING_SYNTHESIS 5-bis #17) and no
     * extractor entity exists, so their placements ship disabled. Binding them to a type of their
     * own rather than to the catch-all is what lets load-time validation tell a deliberately
     * deferred node from an unported one.
     */
    public record Deferred(GodEffect effect) implements GodNodeHandler {
    }

    /**
     * The Deckless: every empty card deck slot pays fruit efficiency, movement speed and cooldown
     * reduction, per invested point. Read from the live deck at snapshot time rather than banked,
     * so pulling a card out of the deck moves the stats inside a vault.
     */
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
     * Efficient Steps: a share of the player's fruit efficiency paid back as movement speed.
     *
     * <p>The stat leg cannot read fruit efficiency itself. It runs <em>inside</em> attribute
     * snapshot construction, so asking {@code AttributeSnapshotHelper} for the value there would
     * re-enter a half-built snapshot. The tick leg samples it on the shared ticker, well outside
     * construction, and parks the reading in {@link GodNodeState}; fruit efficiency only moves on
     * gear and node changes, so a one-second lag is invisible. That is what retires this tree's
     * private sampler and its static per-player map.
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

    /**
     * Plushie Lover doubles an equipped plushie by contributing a second copy of every attribute
     * the plushie already rolls, which lands on the snapshot exactly like the gear pass did.
     * Cheaper and far less invasive than a multiplier on the snapshot calculator, and it cannot
     * double anything the player is not actually wearing.
     */
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
