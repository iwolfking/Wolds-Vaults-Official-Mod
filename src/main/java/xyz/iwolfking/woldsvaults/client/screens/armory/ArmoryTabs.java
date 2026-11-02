package xyz.iwolfking.woldsvaults.client.screens.armory;

import iskallia.vault.VaultMod;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ElasticContainerElement;
import iskallia.vault.client.gui.framework.element.ItemStackDisplayElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.framework.element.TabElement;
import iskallia.vault.client.gui.framework.element.TextureAtlasElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.init.ModTextureAtlases;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.armory.ArmoryTab;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.armory.network.ServerboundArmoryNavMessage;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeTheme;
import xyz.iwolfking.woldsvaults.init.ModNetwork;

/**
 * The armory's shared tab chrome, used by every armory screen: the top strip (the player menu's
 * {@code SkillTabContainerElement} geometry with the armory's eight tabs) and the eight build tabs on a window's
 * right edge. Clicking either asks the server to reopen the armory on that tab or page.
 */
public final class ArmoryTabs {
    public static final TextureAtlasRegion TAB_ICON_EXCESS =
            TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/treasure_chest"));
    public static final int BUILD_TAB_SIZE = 20;
    public static final int BUILD_TAB_PITCH = 22;

    private ArmoryTabs() {
    }

    public static TextureAtlasRegion icon(ArmoryTab tab) {
        return switch (tab) {
            case GEAR -> ScreenTextures.TAB_ICON_WARDROBE_GEAR;
            case INVENTORY -> ScreenTextures.TAB_ICON_WARDROBE_HOTBAR;
            case ABILITIES -> ScreenTextures.TAB_ICON_ABILITIES;
            case TALENTS -> ScreenTextures.TAB_ICON_TALENTS;
            case EXPERTISES -> ScreenTextures.TAB_ICON_EXPERTISES;
            case PRESTIGE -> ScreenTextures.TAB_ICON_PRESTIGE_POWERS;
            case GODS -> GodTreeTheme.TAB_ICON_GODS;
            case EXCESS -> TAB_ICON_EXCESS;
        };
    }

    public static String title(ArmoryTab tab) {
        return switch (tab) {
            case GEAR -> "Gear";
            case INVENTORY -> "Inventory";
            case ABILITIES -> "Abilities";
            case TALENTS -> "Talents";
            case EXPERTISES -> "Expertises";
            case PRESTIGE -> "Prestige";
            case GODS -> "Gods";
            case EXCESS -> "Excess";
        };
    }

    /** Asks the server to open {@code tab} of {@code page}; a no-op when that is what is open. */
    public static void open(ArmoryViewInfo info, int page, ArmoryTab tab) {
        if (page != info.page() || tab != info.tab()) {
            ModNetwork.sendToServer(new ServerboundArmoryNavMessage(info.pos(), page, tab));
        }
    }

    /** The top tab strip. Place it as the base screens place {@code SkillTabContainerElement}. */
    public static TopStrip topStrip(IPosition position, ArmoryViewInfo info) {
        return new TopStrip(position, info);
    }

    /** The eight build tabs, stacked downward from {@code position}. */
    public static BuildTabs buildTabs(IPosition position, ArmoryViewInfo info) {
        return new BuildTabs(position, info);
    }

    public static class TopStrip extends ElasticContainerElement<TopStrip> {
        TopStrip(IPosition position, ArmoryViewInfo info) {
            super(Spatials.positionXYZ(position));
            for (ArmoryTab tab : ArmoryTab.values()) {
                boolean selected = tab == info.tab();
                TextureAtlasElement<?> background = selected
                        ? new TextureAtlasElement<>(ScreenTextures.TAB_BACKGROUND_TOP_SELECTED)
                        : new TextureAtlasElement<>(Spatials.positionY(4), ScreenTextures.TAB_BACKGROUND_TOP);
                this.addElement(new TabElement<>(Spatials.positionX(31 * tab.ordinal() - 10), background,
                        new TextureAtlasElement<>(Spatials.positionXYZ(6, 9, 1), icon(tab)),
                        () -> open(info, info.page(), tab)).tooltip(() -> new TextComponent(title(tab))));
            }
        }
    }

    public static class BuildTabs extends ElasticContainerElement<BuildTabs> {
        BuildTabs(IPosition position, ArmoryViewInfo info) {
            super(Spatials.positionXYZ(position));
            for (int i = 0; i < info.pageIcons().size(); i++) {
                int page = i;
                boolean selected = page == info.page();
                NineSliceElement<?> background = new NineSliceElement<>(Spatials.size(BUILD_TAB_SIZE + (selected ? 3 : 0), BUILD_TAB_SIZE),
                        selected ? ScreenTextures.TAB_BACKGROUND_RIGHT_9 : ScreenTextures.TAB_BACKGROUND_RIGHT_9_DISABLED);
                ItemStack icon = info.pageIcons().get(i);
                ItemStackDisplayElement<?> display = new ItemStackDisplayElement<>(Spatials.positionXYZ(selected ? 3 : 2, 2, 10), icon);
                String title = info.pageTitles().get(i) + (info.pageSaved().get(i) ? "" : " (empty)");
                this.addElement(new TabElement<>(Spatials.positionY(BUILD_TAB_PITCH * i).positionX(selected ? -3 : 0), background,
                        display, () -> open(info, page, info.tab())).tooltip(() -> new TextComponent(title)));
            }
        }
    }
}
