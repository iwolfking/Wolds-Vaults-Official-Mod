package xyz.iwolfking.woldsvaults.menu;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.iwolfking.woldsvaults.api.core.competition.PlayerRewardStorage;
import xyz.iwolfking.woldsvaults.api.core.competition.lib.RewardBundle;

import java.util.List;

public class TimeTrialRewardsGui extends SimpleGui {

    private final ServerPlayer player;
    private final PlayerRewardStorage storage;

    public TimeTrialRewardsGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.player = player;
        this.storage = PlayerRewardStorage.get(player.getServer());

        this.setTitle(new TranslatableComponent("menu.woldsvaults.rewards_menu"));
        this.setLockPlayerInventory(true);

        update();
    }

    private void update() {
        for(int i = 0; i < this.size; i++) {
            this.setSlot(i, ItemStack.EMPTY);
        }

        List<RewardBundle> bundles = storage.getRewards(player.getUUID());
        int slot = 0;

        for (RewardBundle bundle : bundles) {
            for (ItemStack stack : bundle.getItems()) {
                if (slot >= 45) break;

                this.setSlot(slot++, new GuiElement(
                        stack.copy(),
                        (index, type, action, gui) -> {
                            if(storage.claimItem(player.getUUID(), bundle, stack)) {
                                giveItem(stack);
                            }
                            update();
                        }
                ));
            }
        }

        // Claim All Button
        this.setSlot(53, new GuiElementBuilder(Items.CHEST)
                .setName(new TextComponent("Claim All").withStyle(ChatFormatting.GREEN))
                .addLoreLine(new TextComponent("Claim all rewards").withStyle(ChatFormatting.GRAY))
                .setCallback((i, t, a, g) -> claimAll())
        );

        fillFiller();
    }

    private void giveItem(ItemStack stack) {
        if (!player.addItem(stack.copy())) {
            player.drop(stack.copy(), false);
        }

        playClick();
    }

    private void claimAll() {
        storage.getRewards(player.getUUID()).forEach(bundle ->
                bundle.getItems().forEach(this::giveItem)
        );

        storage.clearRewards(player.getUUID());
        update();
    }

    private void fillFiller() {
        GuiElement filler = new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(TextComponent.EMPTY)
                .hideFlags()
                .build();

        for (int i = 45; i < 53; i++) {
            if (this.getSlot(i) == null) {
                this.setSlot(i, filler);
            }
        }
    }

    private void playClick() {
        player.playNotifySound(
                SoundEvents.UI_BUTTON_CLICK,
                SoundSource.MASTER,
                0.7F,
                1.0F
        );
    }
}
