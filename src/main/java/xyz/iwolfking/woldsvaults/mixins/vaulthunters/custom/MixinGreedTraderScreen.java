package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import iskallia.vault.client.data.ClientShardTradeData;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.DynamicLabelElement;
import iskallia.vault.client.gui.framework.element.ItemStackDisplayElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IMutableSpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.framework.text.TextBorder;
import iskallia.vault.client.gui.screen.greed.GreedTraderScreen;
import iskallia.vault.container.GreedTraderContainer;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CoinPouchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.lib.ui.CountDownElement;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

//TODO: Do we need coin display still? Have to check in-game
@Mixin(value = GreedTraderScreen.class, remap = false)
public class MixinGreedTraderScreen  extends AbstractElementContainerScreen<GreedTraderContainer> {
    public MixinGreedTraderScreen(GreedTraderContainer container, Inventory inventory, Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementContainerScreen<GreedTraderContainer>> tooltipRendererFactory) {
        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
    }
//    public MixinGreedTraderScreen(GreedTraderContainer container, Inventory inventory, Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementContainerScreen<GreedTraderContainer>> tooltipRendererFactory) {
//        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
//    }
//
//    @Inject(method = "init", at = @At("TAIL"), remap = true)
//    private void addGreedCoinDisplay(CallbackInfo ci) {
//            IMutableSpatial var10003 = Spatials.positionXYZ(20, -10, 200);
//            Objects.requireNonNull(TextBorder.DEFAULT_FONT.get());
//            this.addElement((new VaultArtisanStationScreen.CoinCountElement(var10003, Spatials.size(100, 9), () -> this.getMenu().getPlayerCoinCount(), LabelTextStyle.shadowFromTextColor())).layout((screen, gui, parent, world) -> world.translateXY(gui.x(), gui.y())));
//            this.addElement(((new TextureAtlasElement<>(Spatials.positionXY(0, -ScreenTextures.TAB_SOULSHARD_BACKGROUND.height()), ScreenTextures.TAB_SOULSHARD_BACKGROUND)).layout((screen, gui, parent, world) -> world.translateXY(gui))).tooltip(Tooltips.multi(() -> List.of(new TextComponent("Greed Coins")))));
//            ItemStackDisplayElement<?> itemStackDisplayElement = (new ItemStackDisplayElement<>(Spatials.positionXY(5, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height() + 3), new ItemStack(ModItems.GREED_COIN, 64))).layout((screen, gui, parent, world) -> world.translateXY(gui));
//            itemStackDisplayElement.setScale(0.72F);
//            this.addElement(itemStackDisplayElement);
//
//            //Countdown
//            LocalDateTime endTime = ClientShardTradeData.getNextReset();
//            LocalDateTime nowTime = LocalDateTime.now(ZoneId.of("UTC")).withNano(0);
//            LocalTime diff = LocalTime.MIN.plusSeconds(ChronoUnit.SECONDS.between(nowTime, endTime));
//            Component component = new TextComponent(diff.format(DateTimeFormatter.ISO_LOCAL_TIME));
//            this.addElement(
//                new CountDownElement(
//                        Spatials.positionXYZ(this.getGuiSpatial().width() / 2 - 70, -10, 200),
//                        Spatials.size(TextBorder.DEFAULT_FONT.get().width(component), 9),
//                        (() -> component),
//                        LabelTextStyle.shadowFromTextColor()
//                )
//                        .layout((screen, gui, parent, world) -> world.translateXY(gui.x(), gui.y()))
//            );
//            this.addElement(
//                (new TextureAtlasElement<>(
//                        Spatials.positionXY(this.getGuiSpatial().width() / 2 - 79, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height()),
//                        ScreenTextures.TAB_COUNTDOWN_BACKGROUND
//                )
//                        .layout((screen, gui, parent, world) -> world.translateXY(gui)))
//                        .tooltip(Tooltips.multi(() -> List.of(new TextComponent("Shop resets in"))))
//            );
//    }


}
