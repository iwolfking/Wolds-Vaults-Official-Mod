package xyz.iwolfking.woldsvaults.client.screens.armory;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.IElement;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.screen.player.element.SkillTabContainerElement;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.armory.ArmoryGodSnapshot;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.armory.menu.ArmoryGodContainer;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodScreenTab;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeDialog;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreePanRegion;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeScreen;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeTheme;
import xyz.iwolfking.woldsvaults.gods.container.GodTreeContainer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The armory's Gods tab: the gods screen fed a saved page's {@link ArmoryGodSnapshot}, read-only. The armory top
 * strip replaces the player menu's tabs, the build tabs sit on the dialog's right edge, and nothing it shows can
 * unlock, refund or move a star.
 */
@OnlyIn(Dist.CLIENT)
public class ArmoryGodTreeScreen extends GodTreeScreen {
    private static final int BUILD_TABS_Y = 44;
    private static final int NOTHING_COLOR = 0xFFAAAAAA;

    private static GodScreenTab lastTab = GodScreenTab.IDONA;
    private static VaultGod lastGod = VaultGod.IDONA;

    public ArmoryGodTreeScreen(GodTreeContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, snapshotOf(container));
        List<IElement> vaultTabs = new ArrayList<>();
        for (IElement element : this.elementRefreshRates.keySet()) {
            if (element instanceof SkillTabContainerElement) {
                vaultTabs.add(element);
            }
        }
        vaultTabs.forEach(this::removeElement);
        ArmoryViewInfo info = infoOf(container);
        if (info != null) {
            this.addElement(ArmoryTabs.topStrip(Spatials.positionXYZ(15, -28, 1), info)
                    .layout((screen, gui, parent, world) -> world.translateXY(this.getTabContentSpatial())));
            this.addElement(ArmoryTabs.buildTabs(Spatials.positionXYZ(0, 0, 1), info)
                    .layout((screen, gui, parent, world) -> world.translateXY(screen.width() - 21, BUILD_TABS_Y)));
        }
    }

    private static ArmoryGodSnapshot snapshotOf(GodTreeContainer container) {
        if (container instanceof ArmoryGodContainer armory) {
            return armory.snapshot();
        }
        WoldsVaults.LOGGER.error("Armory: the Gods view opened on a {} instead of an armory menu; showing an empty build",
                container.getClass().getSimpleName());
        return ArmoryGodSnapshot.empty();
    }

    @Nullable
    private static ArmoryViewInfo infoOf(GodTreeContainer container) {
        if (container instanceof ArmoryGodContainer armory) {
            return armory.info();
        }
        WoldsVaults.LOGGER.error("Armory: the Gods view has no armory header ({}); its tabs are left out",
                container.getClass().getSimpleName());
        return null;
    }

    private ArmoryGodSnapshot snapshot() {
        return (ArmoryGodSnapshot) this.view;
    }

    @Override
    public MutableComponent getTabTitle() {
        ArmoryViewInfo info = this.menu instanceof ArmoryGodContainer armory ? armory.info() : null;
        return new TextComponent(info == null ? "Gods" : "Gods - Build " + (info.page() + 1));
    }

    @Override
    protected GodTreeDialog createDialog(VaultGod god) {
        return new ArmoryGodTreeDialog(this, this.view, god, infoOf(this.getMenu()));
    }

    @Override
    protected GodTreePanRegion createPanRegion(GodTreeDialog dialog, VaultGod god) {
        return new ArmoryGodPanRegion(this, this.view, dialog, god);
    }

    @Override
    protected GodScreenTab initialTab() {
        return lastTab;
    }

    @Override
    protected VaultGod initialGod() {
        return lastGod;
    }

    @Override
    protected void rememberTab(GodScreenTab tab, VaultGod god) {
        lastTab = tab;
        lastGod = god;
    }

    /** "N stars saved" for the viewed god, in the live screen's unspent-points spot. */
    @Override
    protected void renderPointsOverlay(PoseStack matrixStack) {
        String text;
        int color;
        if (!this.snapshot().saved()) {
            text = "Nothing saved";
            color = NOTHING_COLOR;
        } else {
            int stars = this.snapshot().getPurchasedTreeNodes(this.viewedGod).size();
            text = stars + (stars == 1 ? " star saved" : " stars saved");
            color = stars > 0 ? GodTreeTheme.pointsColor(this.viewedGod) : NOTHING_COLOR;
        }
        FormattedCharSequence line = new TextComponent(text)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color & 0xFFFFFF))).getVisualOrderText();
        int rightEdge = this.getContentBounds().width;
        this.font.drawShadow(matrixStack, line, rightEdge - this.font.width(line), 12.0F, 0xFFFFFFFF);
    }
}
