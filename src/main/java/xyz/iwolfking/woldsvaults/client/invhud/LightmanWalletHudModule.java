package xyz.iwolfking.woldsvaults.client.invhud;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import iskallia.vault.client.render.HudPosition;
import iskallia.vault.client.render.hud.InventoryHudHelper;
import iskallia.vault.client.render.hud.module.AbstractHudModule;
import iskallia.vault.client.render.hud.module.context.ModuleRenderContext;
import iskallia.vault.client.render.hud.module.settings.ConditionalModuleSetting;
import iskallia.vault.client.render.hud.module.settings.ModuleSetting;
import iskallia.vault.client.render.hud.module.settings.SliderSetting;
import iskallia.vault.client.render.hud.module.settings.ToggleButtonSetting;
import iskallia.vault.options.VaultOption;
import iskallia.vault.util.Alignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.Collections;
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
        boolean left = opts.getAlignment() == LightmanWalletHudOptions.Alignment.LEFT;
        int iconX = left ? 0 : baseWidth(context);
        int iconY = 0;

        if (opts.getShowWalletIcon()) {
            iconX += left ? 0 : -16;
            InventoryHudHelper.renderScaledGuiItem(
                poseStack,
                wallet,
                iconX,
                iconY,
                false
            );
            iconX += left ? 18 : 0;
        }

        List<ItemStack> coinsToRender = new ArrayList<>();
        List<Item> allCoins = new ArrayList<>(MoneyUtil.getAllCoins(false));
        Collections.reverse(allCoins);

        if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.ITEMS) {
            coinsToRender = walletValue.getAsItemList();
        } else if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_BRONZE && !allCoins.isEmpty()) {
            Item bronzeCoin = allCoins.get(0);
            long unitVal = MoneyUtil.getCoinValue(new ItemStack(bronzeCoin)).getValueNumber();
            if (unitVal > 0) {
                long totalCount = walletValue.getValueNumber() / unitVal;
                if (totalCount > 0) {
                    ItemStack coinStack = new ItemStack(bronzeCoin);
                    coinStack.setCount((int) Math.min(totalCount, 64));
                    coinStack.getOrCreateTag().putLong("TrueTotalCount", totalCount);
                    coinsToRender.add(coinStack);
                }
            }
        } else if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_GOLD && allCoins.size() >= 3) {
            Item goldCoin = allCoins.get(2);
            long unitVal = MoneyUtil.getCoinValue(new ItemStack(goldCoin)).getValueNumber();
            if (unitVal > 0) {
                long totalCount = walletValue.getValueNumber() / unitVal;
                if (totalCount > 0) {
                    ItemStack coinStack = new ItemStack(goldCoin);
                    coinStack.setCount((int) Math.min(totalCount, 64));
                    coinStack.getOrCreateTag().putLong("TrueTotalCount", totalCount);
                    coinsToRender.add(coinStack);
                }
            }
        }

        boolean isItemMode = opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.ITEMS ||
                opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_BRONZE ||
                opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_GOLD;

        if (isItemMode) {
            Font font = Minecraft.getInstance().font;
            int offsetAmount = (18 + opts.getItemGap()) * (left ? 1 : -1);
            if (!left) {
                iconX -= 18;
            }

            for (ItemStack coin : coinsToRender) {
                InventoryHudHelper.renderScaledGuiItem(
                        poseStack,
                        coin,
                        iconX,
                        iconY,
                        false
                );

                long displayCount = coin.getOrCreateTag().contains("TrueTotalCount")
                        ? coin.getOrCreateTag().getLong("TrueTotalCount")
                        : coin.getCount();

                String text = String.valueOf(displayCount);

                int textX = iconX + 8 - (font.width(text) / 2);
                int textY = iconY + 16;

                poseStack.pushPose();
                poseStack.translate(0.0, 0.0, 999.0);
                font.drawShadow(
                        poseStack,
                        text,
                        textX,
                        textY,
                        0xFFFFFFFF
                );
                poseStack.popPose();

                iconX += offsetAmount;
            }
        }

        if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TEXT) {
            String valueString = walletValue.getString();
            Font font = Minecraft.getInstance().font;
            String text = String.valueOf(valueString);

            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 999.0);

            if (!left) {
                iconX -= font.width(text);
            }
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
        current.setShowWalletIcon(def.getShowWalletIcon());

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
                () -> opts.getShowWalletIcon() ? "ON" : "OFF",
                () -> opts.setShowWalletIcon(!opts.getShowWalletIcon())
            ),
            new ToggleButtonSetting( // left or right
                "alignment",
                new TextComponent("Alignment"),
                new TextComponent("Controls alignment"),
                () -> opts.getAlignment().toString(),
                () -> opts.setAlignment(opts.getAlignment().next())
            ),
            new ToggleButtonSetting( // items or text
                "display_type",
                new TextComponent("Display"),
                new TextComponent("Controls how is the coin value displayed."),
                () -> opts.getDisplayMode().toString(),
                () -> {
                    LightmanWalletHudOptions.DisplayMode mode = opts.getDisplayMode().next();
                    opts.setDisplayMode(mode);
                    this.option.setValue(opts.setDisplayMode(mode));
                }
            ),
            new ConditionalModuleSetting(
                new SliderSettingWithoutPercentage(
                    "itemGap",
                    new TextComponent("Item Gap"),
                    new TextComponent("Controls the size of the gap between coin items."),
                    () -> new TextComponent("Item Gap: " + opts.getItemGap()),
                    () -> opts.getItemGap() / 30F,
                    (value) -> this.option.setValue(opts.setItemGap(Math.round(value*30)))
                ),
                () -> opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.ITEMS || opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_BRONZE || opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_GOLD
            )
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

            int itemSize = 0;
            List<Item> allCoins = new ArrayList<>(MoneyUtil.getAllCoins(false));
            java.util.Collections.reverse(allCoins);

            if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.ITEMS) {
                itemSize = walletValue.getAsItemList().size();
            } else if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_BRONZE && !allCoins.isEmpty()) {
                long unitVal = MoneyUtil.getCoinValue(new ItemStack(allCoins.get(0))).getValueNumber();
                if (unitVal > 0 && walletValue.getValueNumber() / unitVal > 0) {
                    itemSize = 1;
                }
            } else if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_GOLD && allCoins.size() >= 3) {
                long unitVal = MoneyUtil.getCoinValue(new ItemStack(allCoins.get(2))).getValueNumber();
                if (unitVal > 0 && walletValue.getValueNumber() / unitVal > 0) {
                    itemSize = 1;
                }
            }

            gapCount = itemSize == 0 ? 0 : itemSize - 1;
            isEmpty = (itemSize == 0);

            String valueString = walletValue.getString();
            String text = String.valueOf(valueString);
            textWidth = font.width(text);
        }

        int walletWidth = opts.getShowWalletIcon() ? 16 : 0;

        boolean isItemMode = opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.ITEMS ||
                opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_BRONZE ||
                opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_GOLD;

        if (isItemMode) {
            if (isEmpty) {
                return walletWidth;
            }
            int totalItemsCount = gapCount + 1;
            return walletWidth + (totalItemsCount * 18) + (gapCount * opts.getItemGap());
        }

        if (opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TEXT) {
            return walletWidth + 2 + textWidth;
        }
        return 16;
    }

    @Override
    protected int baseHeight(ModuleRenderContext context) {
        LightmanWalletHudOptions opts = this.option.getValue();

        boolean isItemMode = opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.ITEMS ||
                opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_BRONZE ||
                opts.getDisplayMode() == LightmanWalletHudOptions.DisplayMode.TOTAL_GOLD;

        if (isItemMode) {
            return 26;
        }

        return 16;
    }

    @Override protected Alignment getAnchorPoint() {
        return this.option.getValue().getAlignment().toVanilla();
    }

    @Override
    public boolean isHidden(ModuleRenderContext context) {
        return !this.option.getValue().isEnabled();
    }

}