package xyz.iwolfking.woldsvaults.armory.menu;

import com.google.gson.JsonObject;
import iskallia.vault.container.NBTElementContainer;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.tree.AbilityTree;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.skill.tree.PrestigeTree;
import iskallia.vault.skill.tree.SkillTree;
import iskallia.vault.skill.tree.TalentTree;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.NetworkHooks;
import xyz.iwolfking.woldsvaults.armory.ArmoryTab;
import xyz.iwolfking.woldsvaults.armory.ArmoryViewInfo;
import xyz.iwolfking.woldsvaults.armory.SavedBuild;
import xyz.iwolfking.woldsvaults.armory.block.ArmoryBlockEntity;

import javax.annotation.Nullable;
import xyz.iwolfking.woldsvaults.init.ModContainers;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;

/** Read-only tree view menus (Abilities, Talents, Expertises, Prestige, Gods) for a saved armory page. */
public final class ArmoryTreeMenus {
    private ArmoryTreeMenus() {
    }

    /**
     * Opens the tree tab named by {@code info.tab()} for the page {@code info.page()}: the page's saved tree rebuilt
     * on a fresh config copy, or an empty copy when the page has no save.
     */
    public static void open(ServerPlayer player, ArmoryBlockEntity armory, ArmoryViewInfo info) {
        if (info.tab() == ArmoryTab.GODS) {
            ArmoryGodMenus.open(player, armory, info);
            return;
        }
        SavedBuild saved = armory.page(info.page()).build();
        SavedBuild shown = saved != null ? saved : new SavedBuild(new JsonObject(), new CompoundTag(), "", 0L);
        try {
            switch (info.tab()) {
                case ABILITIES -> {
                    AbilityTree abilities = shown.displayAbilities();
                    if (abilities == null) {
                        unavailable(player, info, "the ability config is not loaded");
                        return;
                    }
                    openTree(player, info, ModContainers.ARMORY_ABILITIES_CONTAINER, abilities, abilities.getSpentLearnPoints() + spent(shown.displayTalents()));
                }
                case TALENTS -> {
                    TalentTree talents = shown.displayTalents();
                    if (talents == null) {
                        unavailable(player, info, "the talent config is not loaded");
                        return;
                    }
                    openTree(player, info, ModContainers.ARMORY_TALENTS_CONTAINER, talents, talents.getSpentLearnPoints() + spent(shown.displayAbilities()));
                }
                case EXPERTISES -> {
                    ExpertiseTree expertises = shown.displayExpertises();
                    openTree(player, info, ModContainers.ARMORY_EXPERTISES_CONTAINER, expertises, expertises.getSpentLearnPoints());
                }
                case PRESTIGE -> {
                    PrestigeTree prestige = shown.displayPrestige();
                    if (prestige == null) {
                        unavailable(player, info, "the prestige power config is not loaded");
                        return;
                    }
                    openTree(player, info, ModContainers.ARMORY_PRESTIGE_CONTAINER, prestige, learnedTiers(prestige));
                }
                default -> unavailable(player, info, "it is not a tree tab");
            }
        } catch (RuntimeException e) {
            WoldLogger.error("Armory: building the {} view of page {} failed", info.tab(), info.page() + 1, e);
            player.displayClientMessage(new TextComponent("This armory view could not be opened."), true);
        }
    }

    private static <T extends SkillTree> void openTree(ServerPlayer player, ArmoryViewInfo info, MenuType<NBTElementContainer<T>> type,
                                                       T tree, int savedPoints) {
        NetworkHooks.openGui(player, new SimpleMenuProvider((windowId, inventory, p) ->
                        new ArmoryTreeContainer<>(() -> type, windowId, p, tree, info, savedPoints), new TextComponent("Armory")),
                buf -> ArmoryTreeContainer.write(buf, info, savedPoints, tree));
    }

    private static int spent(@Nullable SkillTree tree) {
        if (tree == null) {
            WoldLogger.error("Armory: a tree config is not loaded; its points are left out of the saved count");
            return 0;
        }
        return tree.getSpentLearnPoints();
    }

    /** Learned prestige power tiers. Prestige powers cost knowledge, not learn points, so spent points read 0. */
    private static int learnedTiers(PrestigeTree tree) {
        int[] count = {0};
        tree.iterate(TieredSkill.class, tiered -> count[0] += tiered.getUnmodifiedTier());
        return count[0];
    }

    private static void unavailable(ServerPlayer player, ArmoryViewInfo info, String reason) {
        WoldLogger.warn("Armory: the {} view of page {} cannot open: {}", info.tab(), info.page() + 1, reason);
        player.displayClientMessage(new TextComponent("This armory view is not available."), true);
    }
}
