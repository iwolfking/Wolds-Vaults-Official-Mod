package xyz.iwolfking.woldsvaults.client.invhud;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import iskallia.vault.client.gui.framework.element.SliderElement;
import iskallia.vault.client.gui.framework.element.spi.AbstractSpatialElement;
import iskallia.vault.client.render.HudPosition;
import iskallia.vault.client.render.hud.InventoryHudHelper;
import iskallia.vault.client.render.hud.module.AbstractHudModule;
import iskallia.vault.client.render.hud.module.context.ModuleRenderContext;
import iskallia.vault.client.render.hud.module.settings.ModuleSetting;
import iskallia.vault.client.render.hud.module.settings.SliderSetting;
import iskallia.vault.client.render.hud.module.settings.ToggleButtonSetting;
import iskallia.vault.options.VaultOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

public class LightmanWalletHudModule extends AbstractHudModule<ModuleRenderContext> {

    private final VaultOption<LightmanWalletHudOptions> option;

    public LightmanWalletHudModule(String key, VaultOption<LightmanWalletHudOptions> option) {
        super(key, "Wallet", "Wallet HUD");
        this.option = option;
    }

    private static boolean hasCurioSlot(Player player) {
        if (player == null) {return false;}
        return CuriosApi.getCuriosHelper().getCuriosHandler(player).map(handler -> {
            var walletCurios = handler.getCurios().get("wallet");
            return walletCurios != null && walletCurios.getSlots() > 0;
        }).orElse(false);
    }

    @Override
    protected void renderModule(ModuleRenderContext context) {
        PoseStack poseStack = context.getPoseStack();
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || !hasCurioSlot(player)) {
            return;
        }

        LightmanWalletHudOptions opts = this.option.getValue();
        ItemStack wallet = LightmansCurrency.getWalletStack(player);

        CoinValue walletValue = MoneyUtil.getCoinValue(WalletItem.getWalletInventory(wallet));


        if (context.isEditing() && wallet.isEmpty()) {
            wallet = new ItemStack(ModItems.WALLET_COPPER.get());
            ArrayList<ItemStack> coins = new ArrayList<>();
            int cnt = 1;
            for (Item coin: MoneyUtil.getAllCoins(false)) {
                ItemStack coinStack = new ItemStack(coin, cnt++);
                coins.add(coinStack);
            }
            walletValue = MoneyUtil.getCoinValue(coins);
        }

        String valueString = walletValue.getString();
        Font font = Minecraft.getInstance().font;
        String text = String.valueOf(valueString);

        int iconX = 0;
        int iconY = 0;

        if (opts.shouldShowWalletIcon()) {
            InventoryHudHelper.renderScaledGuiItem(
                poseStack,
                wallet,
                iconX,
                iconY,
                false
            );
            iconX += 18;
        }


        if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.ITEMS) {
            int offsetAmount = opts.getItemGap();
            for (ItemStack coin : walletValue.getAsItemList()) {
                InventoryHudHelper.renderScaledGuiItem(
                    poseStack,
                    coin,
                    iconX,
                    iconY,
                    true
                );
                iconX += offsetAmount;
            }
        }
        if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TEXT) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 999.0);

            font.drawShadow(
                poseStack,
                text,
                iconX,
                iconY + 4,
                0xFFFFFFFF
            );
            poseStack.popPose();
        }
    }

    @Override
    public HudPosition getPosition() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!hasCurioSlot(player)) {
            return null;
        }
        return this.option.getValue().getHudPosition();
    }

    @Override
    public void setPosition(HudPosition pos) {
        LightmanWalletHudOptions opts = this.option.getValue();
        opts.setHudPosition(pos);
        this.option.setValue(opts);
    }

    @Override
    public float getScale() {
        return this.option.getValue().getSize();
    }

    @Override
    public void resetToDefaultPosition() {
        LightmanWalletHudOptions def = LightmanWalletHudOptions.createDefault(this.option.getDefaultValue().getHudPosition());
        LightmanWalletHudOptions current = this.option.getValue();

        current.setHudPosition(def.getHudPosition());
        current.setItemGap(def.getItemGap());
        current.setDisplayMode(def.getDisplayMode());
        current.setEnabled(def.isEnabled());
        current.setSize(def.getSize());
        current.setShowWalletIcon(def.shouldShowWalletIcon());

        this.option.setValue(current);
    }

    @Override
    public List<ModuleSetting> getSettings() {
        LightmanWalletHudOptions opts = this.option.getValue();

        return List.of(
            new ToggleButtonSetting(
                "enabled",
                new TextComponent("Enabled"),
                new TextComponent("Enabled"),
                () -> opts.isEnabled() ? "ON" : "OFF",
                () -> opts.setEnabled(!opts.isEnabled())
            ),
            new SliderSetting(
                "size",
                new TextComponent("Size"),
                new TextComponent("Controls the size of the wallet in the inventory HUD."),
                () -> new TextComponent("Size"), () -> opts.getSize() - 0.5F,
                (value) -> this.option.setValue(opts.setSize(value + 0.5F))),
            new ToggleButtonSetting(
                "wallet_icon",
                new TextComponent("Wallet Icon"),
                new TextComponent("Display wallet icon."),
                () -> opts.shouldShowWalletIcon() ? "ON" : "OFF",
                () -> opts.setShowWalletIcon(!opts.shouldShowWalletIcon())
            ),
            new ToggleButtonSetting( // items or text
                "display_type",
                new TextComponent("Display"),
                new TextComponent("Controls how is the coin value displayed."),
                () -> opts.getDisplayMode().toString(),
                () -> opts.setDisplayMode(opts.getDisplayMode().next())
            ),
            new SliderSetting(
                "itemGap",
                new TextComponent("Item Gap"),
                new TextComponent("Controls the size of the gap between coin items."),
                () -> new TextComponent("Item Gap: " + opts.getItemGap()),
                () -> opts.getItemGap() / 30F,
                (value) -> this.option.setValue(opts.setItemGap(Math.round(value*30)))
            ) {
                @Override // this is pretty stupid, but there is no int slider
                public AbstractSpatialElement<?> createElement(int x, int y, int width, final Runnable onChange) {
                    AbstractSpatialElement<?> el = super.createElement(x,y,width,onChange);
                    if (el instanceof SliderElement slider) slider.hidePercentage(true);
                    return el;
                }
            }
        );
    }

    @Override
    protected int baseWidth(ModuleRenderContext context) {
        LightmanWalletHudOptions opts = this.option.getValue();
        Font font = Minecraft.getInstance().font;
        int textWidth = 0;

        Player player = Minecraft.getInstance().player;
        boolean isEmpty = true;
        int gapCount = 0;
        if (player != null) {
            ItemStack wallet = LightmansCurrency.getWalletStack(player);
            CoinValue walletValue = MoneyUtil.getCoinValue(WalletItem.getWalletInventory(wallet));

            if (context.isEditing() && wallet.isEmpty()) {
                ArrayList<ItemStack> coins = new ArrayList<>();
                int cnt = 1;
                for (Item coin: MoneyUtil.getAllCoins(false)) {
                    ItemStack coinStack = new ItemStack(coin, cnt++);
                    coins.add(coinStack);
                }
                walletValue = MoneyUtil.getCoinValue(coins);
            }

            List<ItemStack> coinItems = walletValue.getAsItemList();
            gapCount = coinItems.isEmpty() ? 0 : coinItems.size() - 1;
            isEmpty = coinItems.isEmpty();

            String valueString = walletValue.getString();
            String text = String.valueOf(valueString);
            textWidth = font.width(text);
        }

        int walletWidth = opts.shouldShowWalletIcon() ? 18 : 0;

        if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.ITEMS) {
            return walletWidth + opts.getItemGap() * gapCount + (isEmpty ? 0 : 18);
        }

        if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TEXT) {
            return walletWidth + textWidth + 2;
        }
        return 16;

    }

    @Override
    public boolean isHidden(ModuleRenderContext context) {
        return !this.option.getValue().isEnabled();
    }

}