package xyz.iwolfking.woldsvaults.client.spellcraft.bar;

import iskallia.vault.client.VaultAbilityKeyBinding;
import iskallia.vault.client.gui.screen.player.AbilitiesElementContainerScreen;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.tree.AbilityTree;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * The skill menu's spell bar under the Abilities tab. It holds this frame's default and effective views and the ability
 * selected in the tree, which a slot click assigns to the default bar in slot mode.
 */
public final class SkillMenuSpellBar implements SpellBarStrip.Host {
    @Nullable
    private static SkillMenuSpellBar current;
    private static boolean showEffective = true;

    private final AbilitiesElementContainerScreen screen;
    private final Supplier<String> selectedSpec;
    private final SpellBarStrip strip;
    private SpellBarView defaultView;
    @Nullable
    private SpellBarView effectiveView;
    private boolean renderingTree;

    private SkillMenuSpellBar(AbilitiesElementContainerScreen screen, Supplier<String> selectedSpec) {
        this.screen = screen;
        this.selectedSpec = selectedSpec;
        this.strip = new SpellBarStrip(this, showEffective);
    }

    /** Returns the menu of this Abilities screen, replacing a cached menu that belongs to another screen. */
    public static SkillMenuSpellBar of(AbilitiesElementContainerScreen screen, Supplier<String> selectedSpec) {
        if (current == null || current.screen != screen) {
            current = new SkillMenuSpellBar(screen, selectedSpec);
        }
        return current;
    }

    public static void forgetClosed() {
        if (current != null && Minecraft.getInstance().screen != current.screen) {
            current = null;
        }
    }

    public static void reset() {
        current = null;
    }

    @Nullable
    public static SkillMenuSpellBar current() {
        return current != null && Minecraft.getInstance().screen == current.screen ? current : null;
    }

    /** True while Vault Hunters draws the open skill menu's ability tree, so its nodes get slot tags. */
    public static boolean isRenderingTree() {
        SkillMenuSpellBar menu = current();
        return menu != null && menu.renderingTree;
    }

    /**
     * Rebuilds this frame's views and lays the strip out in the band the pan region and dialog leave free. Vault Hunters'
     * hover-to-bind (hover an ability, press a key to make it the quick-fire key) is on in classic mode only.
     */
    public void beginFrame() {
        VaultAbilityKeyBinding.isEnabled = SpellBarStrip.classic();
        AbilityTree tree = tree();
        this.defaultView = SpellBarView.ofDefaults(tree);
        this.effectiveView = SpellBarView.ofSynced(tree);
        int h = this.screen.height;
        this.strip.layout(21, h - 21 - SpellBarStrip.HEIGHT, this.screen.width - 21);
        this.strip.refreshView();
        showEffective = this.strip.prefersEffective();
        this.renderingTree = true;
    }

    public void endTree() {
        this.renderingTree = false;
    }

    public SpellBarStrip strip() {
        return this.strip;
    }

    public SpellBarView shown() {
        return this.strip.refreshView();
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
    public SpellBarView effectiveView() {
        return this.effectiveView;
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
}
