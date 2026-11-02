package xyz.iwolfking.woldsvaults.client.screens.armory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.VaultAbilityKeyBinding;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.client.data.ClientAbilityData;
import iskallia.vault.client.gui.component.ScrollableContainer;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.ButtonElement;
import iskallia.vault.client.gui.framework.element.NineSliceElement;
import iskallia.vault.client.gui.helper.FontHelper;
import iskallia.vault.client.gui.helper.UIHelper;
import iskallia.vault.client.gui.overlay.VaultBarOverlay;
import iskallia.vault.client.gui.screen.player.AbilitiesElementContainerScreen;
import iskallia.vault.client.gui.screen.player.legacy.SplitTabContent;
import iskallia.vault.client.gui.screen.player.legacy.TabContent;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.dialog.AbilityDialog;
import iskallia.vault.client.gui.screen.player.legacy.tab.split.pan.AbilityPanRegion;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityNodeTextures;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityWidget;
import iskallia.vault.client.gui.screen.player.legacy.widget.AbilityWidgetSelectable;
import iskallia.vault.container.NBTElementContainer;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModTextureAtlases;
import iskallia.vault.skill.ability.component.AbilityDescriptionFactory;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.base.SpecializedSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.util.MiscUtils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.iwolfking.woldsvaults.WoldsVaults;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Read-only Abilities tab of an armory page: VH's ability screen fed the saved tree, with no learn, unlearn or
 * specialization buttons and no quick-ability keybind assignment.
 */
@OnlyIn(Dist.CLIENT)
public class ArmoryAbilitiesScreen extends AbilitiesElementContainerScreen {
    public ArmoryAbilitiesScreen(NBTElementContainer<AbilityTree> container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.view().installChrome(this.elementStore, this::removeElement, this::addElement, this::getTabContentSpatial,
                element -> element instanceof NineSliceElement
                        || element instanceof ButtonElement<?> && element.getClass().getEnclosingClass() == AbilitiesElementContainerScreen.class);
    }

    private ArmoryTreeView view() {
        return ArmoryTreeView.of(this.menu);
    }

    @Override
    public MutableComponent getTabTitle() {
        return this.view().title();
    }

    @Override
    public TabContent getTabContent() {
        ArmoryTreeView view = this.view();
        Dialog dialog = new Dialog(this.getAbilityTree(), this, view);
        return new SplitTabContent(this, dialog, new PanRegion(dialog, this));
    }

    @Override
    protected void renderPointOverlay(PoseStack poseStack) {
        this.view().renderPointOverlay(poseStack, this.width, "skill point");
    }

    /**
     * Renders, then clears the hover state VH's ability widgets set for quick-ability keybind assignment, so key
     * presses on this screen never rebind an ability.
     */
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        VaultAbilityKeyBinding.abilityHovering = false;
        VaultAbilityKeyBinding.skill = null;
        VaultAbilityKeyBinding.keyMapping = null;
    }

    /** The ability group holding specialization {@code id} in {@code tree}, or null. */
    @Nullable
    static SpecializedSkill group(AbilityTree tree, String id) {
        Skill skill = tree.getForId(id).orElse(null);
        return skill != null && skill.getParent() instanceof SpecializedSkill group ? group : null;
    }

    /** Tier of the group's current specialization, 0 when the ability is not learned. */
    static int groupTier(@Nullable SpecializedSkill group) {
        if (group == null || !group.isUnlocked()) {
            return 0;
        }
        return group.getSpecialization() instanceof TieredSkill tiered ? tiered.getUnmodifiedTier() : 1;
    }

    /** Tier of specialization {@code id}: the group's tier when it is the current one, else 0. */
    static int specTier(@Nullable SpecializedSkill group, String id) {
        return group != null && group.getSpecialization().getId().equals(id) ? groupTier(group) : 0;
    }

    static boolean isBase(SpecializedSkill group, String id) {
        return group.indexOf(id) == 0;
    }

    /** "Base" for a group's first specialization, else the part of its name after the last ": ". */
    static String shortName(SpecializedSkill group) {
        LearnableSkill spec = group.getSpecialization();
        if (group.getIndex() == 0) {
            return "Base";
        }
        String name = spec.getName() == null ? spec.getId() : spec.getName();
        int colon = name.lastIndexOf(": ");
        return colon >= 0 ? name.substring(colon + 2) : name;
    }

    /** "Berserker 8", or "not learned". */
    static String state(@Nullable SpecializedSkill group) {
        int tier = groupTier(group);
        return tier > 0 ? shortName(group) + " " + tier : "not learned";
    }

    /** AbilityDialog with no buttons, the armory heading ("Saved: Berserker 8") and the Load line. */
    static class Dialog extends AbilityDialog {
        private final AbilityTree tree;
        private final ArmoryTreeView view;
        private String selected;
        private AbilityWidget heading;
        private MutableComponent description;

        Dialog(AbilityTree tree, ArmoryAbilitiesScreen screen, ArmoryTreeView view) {
            super(tree, screen);
            this.tree = tree;
            this.view = view;
        }

        @Override
        public void setAbilityWidget(String abilityName) {
            this.selected = abilityName;
            super.setAbilityWidget(abilityName);
        }

        @Override
        public void update() {
            super.update();
            if (this.learnButton != null) {
                this.learnButton.active = false;
                this.learnButton.visible = false;
            }
            this.regretButton = null;
            this.heading = null;
            if (this.selected == null) {
                return;
            }
            Skill skill = this.tree.getForId(this.selected).orElse(null);
            SpecializedSkill group = group(this.tree, this.selected);
            if (!(skill instanceof TieredSkill) || group == null || !(group.getSpecialization() instanceof TieredSkill current)) {
                WoldsVaults.LOGGER.warn("Armory: ability {} is not a tiered specialization in the saved tree; no dialog shown", this.selected);
                return;
            }
            boolean specialization = !isBase(group, this.selected);
            this.heading = new AbilityWidget(this.selected, this.tree, 0, 0,
                    specialization ? AbilityNodeTextures.SECONDARY_NODE : AbilityNodeTextures.PRIMARY_NODE,
                    TextureAtlasRegion.of(ModTextureAtlases.ABILITIES, ModConfigs.ABILITIES_GUI.getIcon(this.selected)));
            SpecializedSkill target = this.heading.makeAbilityNode();
            int tier = current.getActualTier();
            int max = Math.max(current.getMaxLearnableTier(), tier);
            if (target.getSpecialization() instanceof TieredSkill targetSpec) {
                this.description = AbilityDescriptionFactory.create(targetSpec, tier, max, VaultBarOverlay.vaultLevel);
            } else {
                WoldsVaults.LOGGER.warn("Armory: ability {} has no tiered specialization to describe", this.selected);
                this.description = null;
            }
            this.descriptionComponent = new ScrollableContainer(this::renderArmoryDescription);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public Rectangle getDescriptionsBounds() {
            return ArmoryTreeView.descriptionBounds(this.getHeadingBounds(), this.bounds);
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            if (this.bounds == null) {
                return;
            }
            this.renderBackground(poseStack, mouseX, mouseY, partialTicks);
            if (this.selected == null || this.heading == null || this.descriptionComponent == null) {
                return;
            }
            poseStack.pushPose();
            poseStack.translate(this.bounds.x + 5, this.bounds.y + 5, 0.0);
            this.renderArmoryHeading(poseStack, mouseX, mouseY, partialTicks);
            this.descriptionComponent.setBounds(this.getDescriptionsBounds());
            this.descriptionComponent.render(poseStack, mouseX, mouseY, partialTicks);
            poseStack.popPose();
        }

        private void renderArmoryHeading(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            SpecializedSkill group = group(this.tree, this.selected);
            if (group == null) {
                return;
            }
            boolean learned = isBase(group, this.selected) ? groupTier(group) > 0 : specTier(group, this.selected) > 0;
            String name = this.tree.getForId(this.selected).map(Skill::getName).orElse(this.selected);
            String subText = this.view.saved() ? "Saved: " + state(group) : "Nothing saved";
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, ScreenTextures.UI_RESOURCE);
            int width = this.heading.getWidth();
            UIHelper.renderContainerBorder(this, poseStack, this.getHeadingBounds(), 14, 44, 2, 2, 2, 2, -7631989);
            poseStack.pushPose();
            poseStack.translate(10.0, 0.0, 0.0);
            poseStack.pushPose();
            poseStack.translate(width + 5, 13.0, 0.0);
            FontHelper.drawTextComponent(poseStack, ArmoryTreeView.colored(name,
                    learned ? ArmoryTreeView.NAME_LEARNED : ArmoryTreeView.NAME_UNLEARNED), false);
            poseStack.translate(0.0, 10.0, 0.0);
            FontHelper.drawTextComponent(poseStack, ArmoryTreeView.colored(subText,
                    learned ? ArmoryTreeView.NAME_LEARNED : ArmoryTreeView.SUB_UNLEARNED), false);
            poseStack.popPose();
            poseStack.translate(width / 2.0F, 23.0, 0.0);
            this.heading.render(poseStack, mouseX, mouseY, partialTicks);
            poseStack.popPose();
        }

        private void renderArmoryDescription(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            ArmoryTreeView.renderDescription(poseStack, this.descriptionComponent, this.loadLine(), this.description);
        }

        /** "Tier 4 ▶ 8" within one specialization, "Base 4 ▶ Berserker 8" across specializations. */
        private Component loadLine() {
            SpecializedSkill saved = group(this.tree, this.selected);
            SpecializedSkill live = group(ClientAbilityData.getTree(), this.selected);
            int savedTier = groupTier(saved);
            int liveTier = groupTier(live);
            boolean sameSpec = saved != null && live != null && saved.getIndex() == live.getIndex();
            boolean changes = sameSpec ? savedTier != liveTier : savedTier > 0 || liveTier > 0;
            String change = sameSpec && savedTier > 0 && liveTier > 0
                    ? "Tier " + liveTier + ArmoryTreeView.ARROW + savedTier
                    : state(live) + ArmoryTreeView.ARROW + state(saved);
            return this.view.loadLine(changes, change);
        }
    }

    /**
     * AbilityPanRegion that also draws the +N / -N tags: on an ability's base node the change in the ability's
     * level, on a specialization node the change in that specialization's level.
     */
    static class PanRegion extends AbilityPanRegion {
        PanRegion(Dialog dialog, ArmoryAbilitiesScreen screen) {
            super(dialog, screen);
        }

        @Override
        public void renderTabForeground(PoseStack poseStack, Rectangle containerBounds, int mouseX, int mouseY, float partialTicks,
                                        List<Runnable> postContainerRender) {
            super.renderTabForeground(poseStack, containerBounds, mouseX, mouseY, partialTicks, postContainerRender);
            AbilityTree savedTree = this.parentScreen.getAbilityTree();
            AbilityTree liveTree = ClientAbilityData.getTree();
            Point2D.Float midpoint = MiscUtils.getMidpoint(this.getBounds());
            poseStack.pushPose();
            poseStack.translate(midpoint.x, midpoint.y, 0.0);
            poseStack.scale(this.viewportScale, this.viewportScale, 1.0F);
            poseStack.translate(this.viewportTranslation.x, this.viewportTranslation.y, 0.0);
            for (AbstractWidget widget : this.getWidgets()) {
                if (!(widget instanceof AbilityWidgetSelectable ability)) {
                    continue;
                }
                String id = ability.getAbilityName();
                SpecializedSkill saved = group(savedTree, id);
                if (saved == null) {
                    continue;
                }
                SpecializedSkill live = group(liveTree, id);
                int delta = isBase(saved, id)
                        ? groupTier(saved) - groupTier(live)
                        : specTier(saved, id) - specTier(live, id);
                ArmoryTreeView.renderTag(poseStack, widget.x + widget.getWidth() / 2.0F, widget.y - widget.getHeight() / 2.0F, delta);
            }
            poseStack.popPose();
        }
    }
}
