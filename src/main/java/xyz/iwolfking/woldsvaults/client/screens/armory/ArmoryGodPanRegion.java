package xyz.iwolfking.woldsvaults.client.screens.armory;

import iskallia.vault.config.entry.SkillStyle;
import iskallia.vault.core.vault.influence.VaultGod;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.armory.ArmoryGodSnapshot;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodNodeWidget;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeDialog;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreePanRegion;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeScreen;
import xyz.iwolfking.woldsvaults.client.screens.gods.GodTreeView;
import xyz.iwolfking.woldsvaults.client.screens.gods.LiveGodTreeView;
import xyz.iwolfking.woldsvaults.gods.node.GodNode;

/**
 * The armory's star chart: saved stars drawn learned, the rest unlearned, each tagged with what Load would change
 * against the viewer's live tree. It opens centred on the saved stars and never touches the live screen's pan memory.
 */
@OnlyIn(Dist.CLIENT)
public class ArmoryGodPanRegion extends GodTreePanRegion {
    private static final float OPEN_SCALE = 0.45F;

    public ArmoryGodPanRegion(GodTreeScreen parentScreen, GodTreeView view, GodTreeDialog dialog, VaultGod god) {
        super(parentScreen, view, dialog, god);
    }

    @Override
    protected GodNodeWidget createWidget(GodNode node, SkillStyle style, boolean unlocked, boolean available) {
        int tag = 0;
        if (this.view instanceof ArmoryGodSnapshot snapshot && snapshot.saved()) {
            boolean live = LiveGodTreeView.INSTANCE.isTreeNodePurchased(this.god, node.id());
            if (unlocked != live) {
                tag = unlocked ? 1 : -1;
            }
        }
        return new ArmoryGodNodeWidget(node, style, this.god, unlocked, tag);
    }

    @Override
    protected void loadViewportTransforms() {
        this.viewportTranslation = this.savedCenter();
        this.viewportScale = this.clampViewportScale(OPEN_SCALE);
    }

    @Override
    protected void saveViewportTransforms() {
    }

    /** The centre of the saved stars' bounding box, or of the whole chart when the god has none saved. */
    private Vec2 savedCenter() {
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        boolean any = false;
        for (GodNodeWidget widget : this.nodeWidgets.values()) {
            if (!widget.isUnlocked()) {
                continue;
            }
            float x = widget.x + widget.getWidth() / 2.0F;
            float y = widget.y + widget.getHeight() / 2.0F;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            any = true;
        }
        if (!any) {
            return this.getCenter();
        }
        return new Vec2((minX + maxX) / -2.0F, (minY + maxY) / -2.0F);
    }
}
