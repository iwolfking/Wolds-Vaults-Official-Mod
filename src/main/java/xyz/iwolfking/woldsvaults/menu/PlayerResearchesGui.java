package xyz.iwolfking.woldsvaults.menu;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.world.data.PlayerResearchesData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.UsernameCache;
import xyz.iwolfking.woldsvaults.items.ResearchTokenItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerResearchesGui extends SimpleGui {
    private int ticker = 0;
    private final UUID target;
    private final String username;

    private int page = 0;
    private final int ITEMS_PER_PAGE = 45;

    public PlayerResearchesGui(ServerPlayer player, String target) {
        super(MenuType.GENERIC_9x6, player, false);
        UUID foundUuid = null;
        for (Map.Entry<UUID, String> entry : UsernameCache.getMap().entrySet()) {
            if (entry.getValue().equals(target)) {
                foundUuid = entry.getKey();
            }
        }
        this.username = target;
        this.target = foundUuid;
        updateDisplay();
    }

    private static void playClickSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 1, 1);
    }

    public void updateDisplay() {
        List<String> targetResearches = getResearches(target);
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, targetResearches.size());

        int slotIndex = 0;

        for (int i = start; i < end; i++) {
            String research = targetResearches.get(i);

            PlayerResearchesGui.DisplayElement researchElement = PlayerResearchesGui.DisplayElement.of(
                    GuiElementBuilder.from(ResearchTokenItem.create(research))
                            .setName(new TextComponent(research).withStyle(ChatFormatting.GREEN))
                            .addLoreLine(new TextComponent("Unlocked.").withStyle(ChatFormatting.GOLD))
                            .hideFlags()
                            .setCallback((x, y, z) -> playClickSound(this.player))
            );

            this.setSlot(slotIndex, researchElement.element);
            this.setTitle(new TextComponent(username + "'s Researches"));
            slotIndex++;
        }

        for (; slotIndex < ITEMS_PER_PAGE; slotIndex++) {
            this.setSlot(slotIndex, PlayerResearchesGui.DisplayElement.filler().element);
        }

        addNavigationButtons(targetResearches.size());
    }

    private void addNavigationButtons(int totalItems) {
        int totalPages = (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

        if (page > 0) {
            this.setSlot(48, new GuiElementBuilder(Items.ARROW)
                    .setName(new TextComponent("Previous Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((x, y, z) -> {
                        page--;
                        playClickSound(player);
                        updateDisplay();
                    })
                    .build());
        } else {
            this.setSlot(48, PlayerResearchesGui.DisplayElement.filler().element);
        }

        this.setSlot(49, new GuiElementBuilder(Items.PAPER)
                .setName(new TextComponent("Page " + (page + 1) + " / " + Math.max(1, totalPages)))
                .setCallback(GuiElementInterface.EMPTY_CALLBACK)
                .hideFlags()
                .build());

        if (page < totalPages - 1) {
            this.setSlot(50, new GuiElementBuilder(Items.ARROW)
                    .setName(new TextComponent("Next Page").withStyle(ChatFormatting.YELLOW))
                    .setCallback((x, y, z) -> {
                        page++;
                        playClickSound(player);
                        updateDisplay();
                    })
                    .build());
        } else {
            this.setSlot(50, PlayerResearchesGui.DisplayElement.filler().element);
        }
    }

    public List<String> getResearches(UUID uuid) {
        ResearchTree playerResearches = PlayerResearchesData.get((ServerLevel) this.player.level).getResearches(uuid);
        return playerResearches.getResearchesDone();
    }

    @Override
    public void onTick() {
//        ticker++;
//        if (ticker >= 20) {
//            ticker = 0;
//            updateDisplay();
//        }
//        super.onTick();
    }

    public record DisplayElement(@Nullable GuiElementInterface element, @Nullable Slot slot) {
        private static final PlayerResearchesGui.DisplayElement EMPTY = PlayerResearchesGui.DisplayElement.of(new GuiElement(ItemStack.EMPTY, GuiElementInterface.EMPTY_CALLBACK));
        private static final PlayerResearchesGui.DisplayElement FILLER = PlayerResearchesGui.DisplayElement.of(
                new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(new TextComponent(""))
                        .hideFlags()
        );

        public static PlayerResearchesGui.DisplayElement of(GuiElementInterface element) {
            return new PlayerResearchesGui.DisplayElement(element, null);
        }

        public static PlayerResearchesGui.DisplayElement of(GuiElementBuilderInterface<?> element) {
            return new PlayerResearchesGui.DisplayElement(element.build(), null);
        }

        public static PlayerResearchesGui.DisplayElement filler() {
            return FILLER;
        }

        public static PlayerResearchesGui.DisplayElement empty() {
            return EMPTY;
        }
    }
}
