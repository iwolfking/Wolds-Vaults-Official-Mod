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

    /** Drops the Quests tab from the trader's tab strip; the element is still built, then discarded. */
    @Redirect(method = "init", remap = true,
            at = @At(value = "INVOKE", ordinal = 3, remap = false,
                    target = "Liskallia/vault/client/gui/screen/GreedTraderScreen;addElement(Liskallia/vault/client/gui/framework/element/spi/IElement;)Liskallia/vault/client/gui/framework/element/spi/IElement;"))
    private IElement dropRetiredQuestsTab(GreedTraderScreen screen, IElement questsTab) {
        return questsTab;
    }

    /** Moves the Shop tab down one slot, landing the surviving tabs on y = 35 / 65 / 95 with no gap. */
    @ModifyArg(method = "init", remap = true, index = 2,
            at = @At(value = "INVOKE", ordinal = 0, remap = false,
                    target = "Liskallia/vault/client/gui/screen/GreedTraderScreen;createTab(ZLiskallia/vault/client/atlas/TextureAtlasRegion;ILjava/lang/Runnable;)Liskallia/vault/client/gui/framework/element/TabElement;"))
    private int closeGapLeftByQuestsTab(int y) {
        return y + 30;
    }

    /** Reprices the Restock tooltip from reputation to greedy tickets. The count here is display only. */
    @Inject(method = "lambda$addShopRestockButton$7", at = @At("HEAD"), cancellable = true)
    private void showRestockPriceInTickets(CallbackInfoReturnable<Component> cir) {
        int resetCost = this.getMenu().getResetCost();
        boolean affordable = countHeldGreedyTickets() >= resetCost;
        cir.setReturnValue(new TextComponent("Restock (" + resetCost + " Greedy Tickets)")
                .setStyle(Style.EMPTY.withColor(affordable ? -1 : -43691)));
    }

    /** Greys the Restock button out on greedy tickets rather than reputation. */
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
