package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import iskallia.vault.client.data.ClientShardTradeData;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.DynamicLabelElement;
import iskallia.vault.client.gui.framework.element.ItemStackDisplayElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IMutableSpatial;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.framework.text.TextBorder;
import iskallia.vault.client.gui.screen.CatalystInfusionTableScreen;
import iskallia.vault.client.gui.screen.GreedTraderScreen;
import iskallia.vault.client.gui.screen.ShardTradeScreen;
import iskallia.vault.client.gui.screen.block.ToolStationScreen;
import iskallia.vault.client.gui.screen.block.VaultArtisanStationScreen;
import iskallia.vault.client.gui.screen.block.base.ForgeRecipeContainerScreen;
import iskallia.vault.container.GreedTraderContainer;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CoinPouchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.lib.ui.CountDownElement;
import xyz.iwolfking.woldsvaults.api.util.GreedShopHelper;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Mixin(value = GreedTraderScreen.class, remap = false)
public class MixinGreedTraderScreen  extends AbstractElementContainerScreen<GreedTraderContainer> {
    public MixinGreedTraderScreen(GreedTraderContainer container, Inventory inventory, Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementContainerScreen<GreedTraderContainer>> tooltipRendererFactory) {
        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
    }

    /**
     * Drops the retired Quests tab from the trader's tab strip.
     *
     * <p>Base builds the strip as three {@code createTab(...)} results handed straight to
     * {@code addElement}. The quest system is dead - every message the Quests screen can send is
     * refused server-side - so its tab is simply never filed; the element is still built and then
     * discarded, which keeps the redirect free of a null the element store would choke on.</p>
     *
     * <p>The ordinal is pinned against the shipped 3.21.6 jar. {@code m_7856_} issues exactly six
     * {@code addElement} calls, in order: the left window background (63), the right window
     * background (106), the Shop tab (142), the Quests tab (174), the Challenges tab (206) and the
     * tab title label (261) - so the Quests tab is ordinal 3. Sibling mixins that append elements
     * in this method do so from their own merged handler methods, not from {@code m_7856_} itself,
     * so they cannot shift this ordinal.</p>
     */
    @Redirect(method = "init", remap = true,
            at = @At(value = "INVOKE", ordinal = 3, remap = false,
                    target = "Liskallia/vault/client/gui/screen/GreedTraderScreen;addElement(Liskallia/vault/client/gui/framework/element/spi/IElement;)Liskallia/vault/client/gui/framework/element/spi/IElement;"))
    private IElement dropRetiredQuestsTab(GreedTraderScreen screen, IElement questsTab) {
        return questsTab;
    }

    /**
     * Closes the hole the removed Quests tab leaves. Base pitches the strip at 30px from y = 5, and
     * the greed rework's Achievements tab is appended at y = 95 by a sibling mixin, so pushing Shop
     * down one slot lands the three surviving tabs on 35 / 65 / 95 with no gap between them. The
     * ordinal is the first of the three {@code createTab} calls in {@code m_7856_} (bytecode 139,
     * against 171 for Quests and 203 for Challenges); index 2 is the {@code int y} argument.
     *
     * <p>If the Achievements tab is ever moved up to y = 65, delete this injector and the strip
     * lands back on base's own 5 / 35 / 65.</p>
     */
    @ModifyArg(method = "init", remap = true, index = 2,
            at = @At(value = "INVOKE", ordinal = 0, remap = false,
                    target = "Liskallia/vault/client/gui/screen/GreedTraderScreen;createTab(ZLiskallia/vault/client/atlas/TextureAtlasRegion;ILjava/lang/Runnable;)Liskallia/vault/client/gui/framework/element/TabElement;"))
    private int closeGapLeftByQuestsTab(int y) {
        return y + 30;
    }

    /**
     * Reprices the Restock tooltip from reputation to greedy tickets.
     *
     * <p>{@code getResetCost()} is now a ticket count (see {@code MixinPlayerGreedTraderData}), so
     * the affordability check has to read tickets too - the container's reputation balance is no
     * longer what pays for a reroll. Ticket counting here is for display only; the server does its
     * own count before it takes anything.</p>
     *
     * <p>Targeted by lambda name against the shipped 3.21.6 jar, where
     * {@code addShopRestockButton} compiles to three synthetic methods: {@code $6} the click
     * handler, {@code $7} the tooltip {@code Supplier<Component>}, {@code $8} the disabled
     * {@code Supplier<Boolean>}. Mixin-added lambdas are renamed on merge, so sibling mixins
     * cannot shift these indices. The two colour literals are base's own: -1 white,
     * -43691 (0xFFFF5555) red.</p>
     */
    @Inject(method = "lambda$addShopRestockButton$7", at = @At("HEAD"), cancellable = true)
    private void showRestockPriceInTickets(CallbackInfoReturnable<Component> cir) {
        int resetCost = this.getMenu().getResetCost();
        boolean affordable = countHeldGreedyTickets() >= resetCost;
        cir.setReturnValue(new TextComponent("Restock (" + resetCost + " Greedy Tickets)")
                .setStyle(Style.EMPTY.withColor(affordable ? -1 : -43691)));
    }

    /**
     * Greys the Restock button out on greedy tickets rather than reputation. Pairs with
     * {@link #showRestockPriceInTickets}, which documents how the lambda index is pinned.
     */
    @Inject(method = "lambda$addShopRestockButton$8", at = @At("HEAD"), cancellable = true)
    private void disableRestockWithoutTickets(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(countHeldGreedyTickets() < this.getMenu().getResetCost());
    }

    private static int countHeldGreedyTickets() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player == null ? 0 : GreedShopHelper.countGreedyTickets(minecraft.player);
    }

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    private void addGreedCoinDisplay(CallbackInfo ci) {
            IMutableSpatial var10003 = Spatials.positionXYZ(20, -10, 200);
            Objects.requireNonNull(TextBorder.DEFAULT_FONT.get());
            this.addElement((new VaultArtisanStationScreen.CoinCountElement(var10003, Spatials.size(100, 9), () -> this.getMenu().getPlayerCoinCount(), LabelTextStyle.shadowFromTextColor())).layout((screen, gui, parent, world) -> world.translateXY(gui.x(), gui.y())));
            this.addElement(((new TextureAtlasElement<>(Spatials.positionXY(0, -ScreenTextures.TAB_SOULSHARD_BACKGROUND.height()), ScreenTextures.TAB_SOULSHARD_BACKGROUND)).layout((screen, gui, parent, world) -> world.translateXY(gui))).tooltip(Tooltips.multi(() -> List.of(new TextComponent("Greed Coins")))));
            ItemStackDisplayElement<?> itemStackDisplayElement = (new ItemStackDisplayElement<>(Spatials.positionXY(5, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height() + 3), new ItemStack(ModItems.GREED_COIN, 64))).layout((screen, gui, parent, world) -> world.translateXY(gui));
            itemStackDisplayElement.setScale(0.72F);
            this.addElement(itemStackDisplayElement);

            //Countdown
            LocalDateTime endTime = ClientShardTradeData.getNextReset();
            LocalDateTime nowTime = LocalDateTime.now(ZoneId.of("UTC")).withNano(0);
            LocalTime diff = LocalTime.MIN.plusSeconds(ChronoUnit.SECONDS.between(nowTime, endTime));
            Component component = new TextComponent(diff.format(DateTimeFormatter.ISO_LOCAL_TIME));
            this.addElement(
                new CountDownElement(
                        Spatials.positionXYZ(this.getGuiSpatial().width() / 2 - 70, -10, 200),
                        Spatials.size(TextBorder.DEFAULT_FONT.get().width(component), 9),
                        (() -> component),
                        LabelTextStyle.shadowFromTextColor()
                )
                        .layout((screen, gui, parent, world) -> world.translateXY(gui.x(), gui.y()))
            );
            this.addElement(
                (new TextureAtlasElement<>(
                        Spatials.positionXY(this.getGuiSpatial().width() / 2 - 79, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height()),
                        ScreenTextures.TAB_COUNTDOWN_BACKGROUND
                )
                        .layout((screen, gui, parent, world) -> world.translateXY(gui)))
                        .tooltip(Tooltips.multi(() -> List.of(new TextComponent("Shop resets in"))))
            );
    }


}
