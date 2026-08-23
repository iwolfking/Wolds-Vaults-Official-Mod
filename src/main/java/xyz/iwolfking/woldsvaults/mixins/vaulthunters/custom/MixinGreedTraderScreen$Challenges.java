package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.VaultMod;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.data.ClientGreedTreeData;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ItemStackDisplayElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.NineSliceButtonElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.screen.GreedTraderScreen;
import iskallia.vault.client.gui.screen.bounty.element.HeaderElement;
import iskallia.vault.config.greed.GreedChallengeEntry;
import iskallia.vault.container.GreedTraderContainer;
import iskallia.vault.greed.GreedChallengeSlot;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.init.ModTextureAtlases;
import iskallia.vault.network.message.ServerboundGreedChallengeActionMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.milestones.GreedChallengeOffers;
import xyz.iwolfking.woldsvaults.milestones.MilestoneDefinition;
import xyz.iwolfking.woldsvaults.milestones.MilestoneRegistry;
import xyz.iwolfking.woldsvaults.milestones.client.ClientMilestoneData;

import java.util.List;

/**
 * Turns Mr. Greedy's challenge tab into a menu of everything the player's rank has unlocked.
 *
 * <p>Base offered exactly one rolled challenge at a time, hid the accept button while any challenge
 * was in flight, and put an abandon button next to it as the only way to see a different one. The
 * server now files one slot per unlocked, uncompleted crystal, so both halves of this tab are
 * rebuilt against that list: every challenge is a row, any row can be taken, and abandoning is
 * gone. The elements, textures and geometry are base's own - this is the same tab, filled
 * differently.</p>
 *
 * <p>The reward line reads the crystal's milestone instead of the retired
 * {@code getChallengeReputationReward} config formula, so the number on the offer is exactly the
 * number the achievements tab pays out for it. It is drawn only on the detail panel a row opens,
 * base's own placement for reward text: a row is barely wider than a challenge name, and printing
 * the reputation there too left the two colliding. Both reimplementations are HEAD-cancels rather
 * than {@code @Overwrite}s so base keeps its own selection, rebuild-on-sync and layout plumbing;
 * the mixin extends {@code GreedTraderScreen} - the target's own superclass - to reach the
 * protected element helpers without shadowing each one.</p>
 */
@Mixin(value = GreedTraderScreen.Challenges.class, remap = false)
public abstract class MixinGreedTraderScreen$Challenges extends GreedTraderScreen {
    private static final int LIST_X = 7;
    private static final int LIST_Y = 17;
    private static final int LIST_W = 133;
    private static final int LIST_H = 165;
    private static final int ROW_W = 124;
    private static final int ROW_H = 19;
    private static final int DETAIL_X = 152;
    private static final int DETAIL_W = 139;
    private static final int ACTION_H = 20;
    private static final int NAME_COLOUR = 0x544C3B;
    private static final int ACTIVE_COLOUR = 0xFFC44D;
    private static final int REWARD_COLOUR = 0xFFDD00;
    private static final int COMPLETE_COLOUR = 0xA1FFA7;

    @Shadow
    private int selectedChallenge;

    protected MixinGreedTraderScreen$Challenges(GreedTraderContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Inject(method = "buildContent", at = @At("HEAD"), cancellable = true)
    private void listEveryUnlockedChallenge(CallbackInfo ci) {
        ci.cancel();
        this.woldsvaults$buildList();
        this.woldsvaults$buildDetail();
    }

    @Inject(method = "updateDetail", at = @At("HEAD"), cancellable = true)
    private void showMilestoneReward(CallbackInfo ci) {
        ci.cancel();
        this.woldsvaults$buildDetail();
    }

    private void woldsvaults$buildList() {
        NineSliceElement<?> plate = new NineSliceElement<>(Spatials.positionXY(LIST_X, LIST_Y).size(LIST_W, LIST_H),
                ScreenTextures.INSET_GREY_BACKGROUND);
        plate.layout(this.translateWorldSpatial());
        this.addContentEl(plate);
        ScrollList list = new ScrollList(Spatials.positionXY(LIST_X, LIST_Y).size(LIST_W, LIST_H));
        list.layout(this.translateWorldSpatial());
        this.addContentEl(list);
        if (ModConfigs.GREED_TRADER == null) {
            return;
        }
        List<GreedChallengeSlot> slots = ClientGreedTreeData.getChallengeSlots();
        int rowY = 0;
        for (int i = 0; i < slots.size(); i++) {
            GreedChallengeSlot slot = slots.get(i);
            GreedChallengeEntry entry = ModConfigs.GREED_TRADER.getChallengeEntryById(slot.getChallengeId());
            if (entry == null) {
                continue;
            }
            int index = i;
            ClickableElement<?> row = new ClickableElement<>(Spatials.positionXY(1, rowY).size(ROW_W, ROW_H),
                    () -> this.woldsvaults$select(index));
            if (slot.isAvailable() || slot.isAttempted()) {
                row.addElement(new NineSliceButtonElement<>(Spatials.positionXY(0, 0).size(ROW_W, ROW_H),
                        ScreenTextures.RAISE_ENCHANTING_TABLE_BUTTON_TEXTURES, () -> {
                }));
            } else {
                row.addElement(new NineSliceElement<>(Spatials.positionXY(0, 0).size(ROW_W, ROW_H),
                        ScreenTextures.BUTTON_EMPTY_DARK_GRAY));
            }
            int reserved = 4;
            if (slot.isComplete()) {
                row.addElement(new TextureAtlasElement<>(Spatials.positionXY(ROW_W - 4 - 15, 1),
                        TextureAtlasRegion.of(ModTextureAtlases.QUESTS, VaultMod.id("gui/quests/check"))));
                reserved += 15;
            }
            row.addElement(new LabelElement<>(Spatials.positionXY(4, 5), Spatials.size(ROW_W - 4 - reserved, 9),
                    woldsvaults$coloured(entry.getDisplayName(), slot.isAttempted() ? ACTIVE_COLOUR : NAME_COLOUR),
                    LabelTextStyle.defaultStyle()));
            list.addElement(row);
            rowY += ROW_H;
        }
        if (rowY == 0) {
            LabelElement<?> empty = new LabelElement<>(Spatials.positionXY(LIST_X + 4, LIST_Y + 6),
                    Spatials.size(LIST_W - 8, 9),
                    new TextComponent("No challenges unlocked yet").withStyle(ChatFormatting.DARK_GRAY),
                    LabelTextStyle.defaultStyle());
            empty.layout(this.translateWorldSpatial());
            this.addContentEl(empty);
        }
    }

    private void woldsvaults$select(int slotIndex) {
        this.selectedChallenge = slotIndex;
        this.clearDetail();
        this.woldsvaults$buildDetail();
        this.requestLayout();
    }

    private void woldsvaults$buildDetail() {
        if (ModConfigs.GREED_TRADER == null) {
            return;
        }
        List<GreedChallengeSlot> slots = ClientGreedTreeData.getChallengeSlots();
        if (this.selectedChallenge < 0 || this.selectedChallenge >= slots.size()) {
            return;
        }
        GreedChallengeSlot slot = slots.get(this.selectedChallenge);
        GreedChallengeEntry entry = ModConfigs.GREED_TRADER.getChallengeEntryById(slot.getChallengeId());
        if (entry == null) {
            return;
        }
        HeaderElement header = new HeaderElement(Spatials.positionXY(DETAIL_X, 4).width(DETAIL_W).height(ACTION_H),
                new TextComponent(entry.getDisplayName()).withStyle(ChatFormatting.WHITE));
        header.layout(this.translateWorldSpatial());
        this.addDetailEl(header);

        int detailY = 28;
        ScrollList detail = new ScrollList(Spatials.positionXY(DETAIL_X, detailY)
                .size(DETAIL_W - 1, 189 - detailY - ACTION_H - 9));
        detail.layout(this.translateWorldSpatial());
        this.addDetailEl(detail);

        int innerW = DETAIL_W - 15;
        int yOff = 4;
        if (entry.getDescription() != null && !entry.getDescription().isEmpty()) {
            detail.addElement(new LabelElement<>(Spatials.positionXY(4, yOff), Spatials.size(innerW, 40),
                    new TextComponent(entry.getDescription()).withStyle(ChatFormatting.DARK_GRAY),
                    LabelTextStyle.defaultStyle().wrap()));
            yOff += 44;
        }
        detail.addElement(new LabelElement<>(Spatials.positionXYZ(4, yOff, 1),
                new TextComponent("Rewards").withStyle(ChatFormatting.WHITE),
                LabelTextStyle.defaultStyle().shadowFromTextColor()));
        yOff += 12;
        int reputation = woldsvaults$reputation(slot.getChallengeId());
        Component rewardLine = reputation > 0
                ? woldsvaults$coloured(" +" + reputation + " Reputation, collected from the Achievements tab", REWARD_COLOUR)
                : new TextComponent(" No reputation").withStyle(ChatFormatting.DARK_GRAY);
        detail.addElement(new LabelElement<>(Spatials.positionXYZ(4, yOff, 1), Spatials.size(innerW, 30),
                rewardLine, LabelTextStyle.defaultStyle().wrap()));
        yOff += 34;
        detail.addElement(new LabelElement<>(Spatials.positionXYZ(4, yOff, 1),
                new TextComponent("Cost").withStyle(ChatFormatting.WHITE),
                LabelTextStyle.defaultStyle().shadowFromTextColor()));
        yOff += 12;
        int rebuyCost = ModConfigs.GREED_TRADER.getChallengeRebuyCoinCost();
        Component costLine = slot.isAvailable()
                ? new TextComponent(" Free").withStyle(ChatFormatting.DARK_GRAY)
                : new TextComponent(" " + rebuyCost + " Greed Coins for a replacement crystal")
                .withStyle(ChatFormatting.DARK_GRAY);
        detail.addElement(new LabelElement<>(Spatials.positionXYZ(4, yOff, 1), Spatials.size(innerW, 20),
                costLine, LabelTextStyle.defaultStyle().wrap()));

        this.woldsvaults$buildAction(slot, rebuyCost);
    }

    private void woldsvaults$buildAction(GreedChallengeSlot slot, int rebuyCost) {
        int buttonY = 186 - ACTION_H - 2;
        int index = this.selectedChallenge;
        if (slot.isAvailable()) {
            NineSliceButtonElement<?> accept = new NineSliceButtonElement<>(
                    Spatials.positionXY(DETAIL_X + 1, buttonY).size(DETAIL_W - 2, ACTION_H),
                    ScreenTextures.BUTTON_EMPTY_TEXTURES, () -> {
                ModNetwork.sendToServer(new ServerboundGreedChallengeActionMessage(
                        ServerboundGreedChallengeActionMessage.Action.ACCEPT, index));
                this.playClick();
            });
            accept.label(() -> new TextComponent("Take Challenge"), LabelTextStyle.shadowFromTextColor().center());
            accept.layout(this.translateWorldSpatial());
            this.addDetailEl(accept);
            return;
        }
        if (slot.isAttempted()) {
            NineSliceButtonElement<?> rebuy = new NineSliceButtonElement<>(
                    Spatials.positionXY(DETAIL_X + 1, buttonY).size(DETAIL_W - 2, ACTION_H),
                    ScreenTextures.BUTTON_EMPTY_TEXTURES, () -> {
                ModNetwork.sendToServer(new ServerboundGreedChallengeActionMessage(
                        ServerboundGreedChallengeActionMessage.Action.REBUY, index));
                this.playClick();
            });
            rebuy.setDisabled(() -> this.getMenu().getPlayerCoinCount() < rebuyCost);
            rebuy.layout(this.translateWorldSpatial());
            this.addDetailEl(rebuy);

            String rebuyText = "Another Crystal x" + rebuyCost;
            int textWidth = Minecraft.getInstance().font.width(rebuyText);
            int startX = DETAIL_X + 1 + (DETAIL_W - 2 - (textWidth + 11)) / 2;
            LabelElement<?> rebuyLabel = new LabelElement<>(
                    Spatials.positionXYZ(startX, buttonY + 1 + (ACTION_H - 9) / 2, 2),
                    woldsvaults$coloured(rebuyText, 0xFFFFFF), LabelTextStyle.shadowFromTextColor());
            rebuyLabel.layout(this.translateWorldSpatial());
            this.addDetailEl(rebuyLabel);

            ItemStackDisplayElement<?> coin = new ItemStackDisplayElement<>(
                    Spatials.positionXYZ(startX + textWidth + 2, buttonY + (ACTION_H - 9) / 2, 10),
                    new ItemStack(ModItems.GREED_COIN));
            coin.setScale(9.0F / 16.0F);
            coin.layout(this.translateWorldSpatial());
            this.addDetailEl(coin);
            return;
        }
        NineSliceElement<?> plate = new NineSliceElement<>(Spatials.positionXY(DETAIL_X + 1, buttonY)
                .size(DETAIL_W - 2, ACTION_H), ScreenTextures.BUTTON_EMPTY_DARK_GRAY);
        plate.layout(this.translateWorldSpatial());
        this.addDetailEl(plate);
        Component text = slot.isComplete()
                ? woldsvaults$coloured("Completed", COMPLETE_COLOUR)
                : new TextComponent("Abandoned").withStyle(ChatFormatting.RED);
        LabelElement<?> label = new LabelElement<>(Spatials.positionXY(DETAIL_X, buttonY + (ACTION_H - 9) / 2 + 1),
                Spatials.size(DETAIL_W, 9), text, LabelTextStyle.defaultStyle().center());
        label.layout(this.translateWorldSpatial());
        this.addDetailEl(label);
    }

    private static Component woldsvaults$coloured(String text, int colour) {
        return new TextComponent(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colour)));
    }

    /**
     * What finishing this crystal is worth: the reputation of the next uncollected tier of the
     * milestone it completes, read from the client's own milestone mirror so the offer and the
     * achievements row can never disagree. Crystals with no milestone are worth nothing.
     */
    private static int woldsvaults$reputation(String challengeCrystalId) {
        MilestoneDefinition definition = MilestoneRegistry.getByChallengeCrystal(challengeCrystalId);
        if (definition == null) {
            return 0;
        }
        return GreedChallengeOffers.getReputation(challengeCrystalId,
                ClientMilestoneData.getClaimedTiers(definition.getId()),
                ClientMilestoneData.getCompletedTiers(definition.getId()));
    }
}
