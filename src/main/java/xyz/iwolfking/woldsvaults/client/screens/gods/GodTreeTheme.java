package xyz.iwolfking.woldsvaults.client.screens.gods;

import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModTextureAtlases;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import xyz.iwolfking.woldsvaults.WoldsVaults;

/**
 * Palette and shared assets for the god constellation screens. Star frame sheets are 256x256 with 64px
 * cells: columns stat/minor/major/root, rows locked/available/unlocked/selected.
 */
public final class GodTreeTheme {
    public static final String LANG_ROOT = "screen.woldsvaults.gods.";

    public static final TextureAtlasRegion TAB_ICON_GODS =
            TextureAtlasRegion.of(ModTextureAtlases.SCREEN, WoldsVaults.id("gui/screen/tab_icon_gods"));

    public static final VaultGod[] DISPLAY_ORDER = {VaultGod.IDONA, VaultGod.VELARA, VaultGod.WENDARR, VaultGod.TENOS};

    public static final ResourceLocation GODS_MASTERY_ICON = WoldsVaults.id("textures/item/gods_mastery.png");

    public static final int OVERVIEW_ACCENT = 0xFFE6DCC0;
    public static final int PLATE_FILL = 0xC01A1420;
    public static final int PLATE_BORDER = 0xFF2E2430;
    public static final int PLATE_BORDER_HOVER = 0xFF8A8A8A;
    public static final int TEXT_MUTED = 0xFFC4C4C4;
    public static final int TEXT_DIM = 0xFF8A8A8A;
    public static final int STATUS_GOOD = 0xFF6EF08C;
    public static final int STATUS_BAD = 0xFFFF5A5A;
    public static final int SLOT_LOCKED_FILL = 0xFF1C1A22;
    public static final int SLOT_LOCKED_RING = 0xFF5E5C68;
    public static final int SLOT_LOCKED_CROSS = 0xD0FF5A5A;
    public static final int SLOT_EMPTY_FILL = 0xFF14101A;
    public static final int SLOT_EMPTY_RING = 0xFFA4A4AE;
    public static final int POPUP_FILL = 0xE0100A14;

    public static final int STAR_SHEET_SIZE = 256;
    public static final int STAR_CELL = 64;
    public static final int STAR_COLUMN_STAT = 0;
    public static final int STAR_COLUMN_MINOR = 1;
    public static final int STAR_COLUMN_MAJOR = 2;
    public static final int STAR_COLUMN_ROOT = 3;
    public static final int STAR_ROW_LOCKED = 0;
    public static final int STAR_ROW_AVAILABLE = 1;
    public static final int STAR_ROW_UNLOCKED = 2;
    public static final int STAR_ROW_SELECTED = 3;

    public static final int SPACE_BACKGROUND = 0xFF060309;

    private GodTreeTheme() {
    }

    public static MutableComponent lang(String suffix, Object... args) {
        return new TranslatableComponent(LANG_ROOT + suffix, args);
    }

    public static ResourceLocation starSheet(VaultGod god) {
        return WoldsVaults.id("textures/gui/gods/stars_" + god.getName().toLowerCase(java.util.Locale.ROOT) + ".png");
    }

    /** The grey star sheet, for a transfer-slot star dormant under the active god. */
    public static ResourceLocation starSheetNeutral() {
        return WoldsVaults.id("textures/gui/gods/stars_neutral.png");
    }

    public static TextureAtlasRegion godIcon(VaultGod god) {
        return switch (god) {
            case IDONA -> ScreenTextures.ICON_IDONA;
            case VELARA -> ScreenTextures.ICON_VELARA;
            case TENOS -> ScreenTextures.ICON_TENOS;
            case WENDARR -> ScreenTextures.ICON_WENDARR;
        };
    }

    /** The bright accent - constellation line cores, lit text, the selected sub-tab border. */
    public static int accent(VaultGod god) {
        return switch (god) {
            case IDONA -> 0xFFFF7A3D;
            case VELARA -> 0xFF6EF08C;
            case TENOS -> 0xFF5AD8FF;
            case WENDARR -> 0xFFFFC854;
        };
    }

    /** The dim body tone - unlit constellation lines and muted accents. */
    public static int accentDim(VaultGod god) {
        return switch (god) {
            case IDONA -> 0xFF802818;
            case VELARA -> 0xFF2B6B40;
            case TENOS -> 0xFF1F5E7A;
            case WENDARR -> 0xFF7A5A1E;
        };
    }

    /** The deep shade behind dim lines, just above the sky background. */
    public static int accentDeep(VaultGod god) {
        return switch (god) {
            case IDONA -> 0xFF3D1410;
            case VELARA -> 0xFF14321E;
            case TENOS -> 0xFF0E2C3A;
            case WENDARR -> 0xFF3A2C10;
        };
    }

    public static int pointsColor(VaultGod god) {
        return 0xFF000000 | god.getColor();
    }
}
