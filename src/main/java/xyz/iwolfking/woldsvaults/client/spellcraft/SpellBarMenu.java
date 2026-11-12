package xyz.iwolfking.woldsvaults.client.spellcraft;

import iskallia.vault.client.gui.screen.player.AbilitiesElementContainerScreen;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * The skill menu's spell bar: the strip under the Abilities tab, its views for this frame (the default bar and, while
 * items lay spells over it, the effective bar as last synced), and the ability selected in the tree, which a default-bar click
 * assigns. The tree's slot tags and the dialog heading read the shown view from here.
 */
public final class SpellBarMenu implements SpellBarStrip.Host {
    @Nullable
    private static SpellBarMenu current;
    private static boolean showItem = true;

    private final AbilitiesElementContainerScreen screen;
    private final Supplier<String> selectedSpec;
    private final Supplier<Float> treeZoom;
    private final SpellBarStrip strip;
    private SpellBarView defaultView;
    @Nullable
    private SpellBarView itemView;
    private boolean renderingTree;

    private SpellBarMenu(AbilitiesElementContainerScreen screen, Supplier<String> selectedSpec, Supplier<Float> treeZoom) {
        this.screen = screen;
        this.selectedSpec = selectedSpec;
        this.treeZoom = treeZoom;
        this.strip = new SpellBarStrip(this, showItem);
    }

    /**
     * The menu for this Abilities screen, made on first use and dropped when another screen asks. {@code selectedSpec}
     * gives the specialization the dialog shows, {@code treeZoom} the tree pan's current zoom.
     */
    public static SpellBarMenu of(AbilitiesElementContainerScreen screen, Supplier<String> selectedSpec, Supplier<Float> treeZoom) {
        if (current == null || current.screen != screen) {
            current = new SpellBarMenu(screen, selectedSpec, treeZoom);
        }
        return current;
    }

    @Nullable
    public static SpellBarMenu current() {
        return current != null && Minecraft.getInstance().screen == current.screen ? current : null;
    }

    /** True while VH draws the Abilities tree of the open skill menu, so its nodes get slot tags. */
    public static boolean isRenderingTree() {
        SpellBarMenu menu = current();
        return menu != null && menu.renderingTree;
    }

    /** Rebuilds this frame's views and places the strip in the band the pan and dialog gave up. */
    public void beginFrame() {
        AbilityTree tree = tree();
        this.defaultView = SpellBarView.ofDefaults(tree);
        this.itemView = SpellBarView.ofSynced(tree);
        int h = this.screen.height;
        this.strip.layout(21, h - 21 - SpellBarStrip.HEIGHT, this.screen.width - 21);
        this.strip.view();
        showItem = this.strip.prefersItem();
        this.renderingTree = true;
    }

    public void endTree() {
        this.renderingTree = false;
    }

    /** The tree pan's zoom, so slot tags can undo it and keep a fixed size on screen. */
    public float treeZoom() {
        Float zoom = this.treeZoom.get();
        return zoom == null || zoom <= 0.0F ? 1.0F : zoom;
    }

    public SpellBarStrip strip() {
        return this.strip;
    }

    /** The bar the strip shows this frame. */
    public SpellBarView shown() {
        return this.strip.view();
    }

    @Override
    public AbilityTree tree() {
        return this.screen.getAbilityTree();
    }

    @Override
    public SpellBarView defaultView() {
        return this.defaultView == null ? SpellBarView.ofDefaults(tree()) : this.defaultView;
    }

    @Nullable
    @Override
    public SpellBarView itemView() {
        return this.itemView;
    }

    @Nullable
    @Override
    public String selectedGroup() {
        String spec = this.selectedSpec.get();
        if (spec == null) {
            return null;
        }
        Skill skill = tree().getForId(spec).orElse(null);
        Skill group = skill == null ? null : skill.getParent();
        return group == null ? null : group.getId();
    }


    @Override
    public void openKeys() {
        SpellKeysScreen.openFromSkillMenu();
    }
}
