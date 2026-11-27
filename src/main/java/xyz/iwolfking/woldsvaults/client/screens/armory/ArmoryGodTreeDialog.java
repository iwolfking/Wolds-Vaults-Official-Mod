package xyz.iwolfking.woldsvaults.client.screens.armory;

import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.armory.ArmoryGodSnapshot;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeDialog;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeScreen;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeTheme;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeView;
import xyz.iwolfking.woldsvaults.client.screens.gods.LiveGodTreeView;
import xyz.iwolfking.woldsvaults.client.screens.greed.GreedTheme;

import javax.annotation.Nullable;
import java.awt.Rectangle;

/**
 * The armory's god dialog: no action button, a saved-state heading line, and a "Load:" line comparing the saved
 * star with the viewer's live tree ahead of the usual description, which reaches down into the empty button band.
 */
@OnlyIn(Dist.CLIENT)
public class ArmoryGodTreeDialog extends GodTreeDialog {
    public static final int LOAD_CHANGES = 0x0F5A1C;
    public static final int LOAD_BLOCKED = 0x7E0000;
    public static final int LOAD_NONE = 0x404040;

    @Nullable
    private final ArmoryViewInfo info;

    public ArmoryGodTreeDialog(GodTreeScreen parentScreen, GodTreeView view, VaultGod god, @Nullable ArmoryViewInfo info) {
        super(parentScreen, view, god);
        this.info = info;
    }

    @Override
    public void update() {
        super.update();
        this.learnButton = null;
        this.regretButton = null;
    }

    @Override
    public Rectangle getDescriptionsBounds() {
        Rectangle base = super.getDescriptionsBounds();
        return new Rectangle(base.x, base.y, base.width, this.bounds.height - 15 - base.y);
    }

    @Override
    protected MutableComponent buildNodeDescription(boolean unlocked) {
        MutableComponent description = new TextComponent("");
        description.append(this.loadLine());
        description.append(new TextComponent("\n\n"));
        description.append(super.buildNodeDescription(unlocked));
        return description;
    }

    /** What Load would do to the selected star, against the viewer's live tree. */
    private Component loadLine() {
        boolean pageSaved = this.view instanceof ArmoryGodSnapshot snapshot && snapshot.saved();
        if (!pageSaved) {
            return colored("Load: nothing saved", LOAD_NONE);
        }
        if (this.info == null || !this.info.godsUpgrade()) {
            return colored("Load: no upgrade", LOAD_BLOCKED);
        }
        boolean saved = this.view.isTreeNodePurchased(this.god, this.selectedNodeId);
        boolean live = LiveGodTreeView.INSTANCE.isTreeNodePurchased(this.god, this.selectedNodeId);
        if (saved && !live) {
            return colored("Load: learns this star", LOAD_CHANGES);
        }
        if (!saved && live) {
            return colored("Load: removes this star", LOAD_CHANGES);
        }
        return colored("Load: no change", LOAD_NONE);
    }

    @Override
    protected Component headingSubText(String text, int color) {
        if (this.selectedNode == null || !this.selectedNode.enabled()) {
            return super.headingSubText(text, color);
        }
        if (!this.view.isTreeNodePurchased(this.god, this.selectedNodeId)) {
            boolean pageSaved = this.view instanceof ArmoryGodSnapshot snapshot && snapshot.saved();
            return super.headingSubText(pageSaved ? "Not saved" : "Nothing saved", COLOR_MUTED);
        }
        int slot = this.transferSlotOf(this.selectedNode);
        String saved = slot >= 0 ? "Saved - Slot " + GreedTheme.roman(slot + 1) : "Saved";
        return super.headingSubText(saved, GodTreeTheme.accent(this.god) & 0xFFFFFF);
    }

    private static Component colored(String text, int color) {
        return new TextComponent(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
    }
}
