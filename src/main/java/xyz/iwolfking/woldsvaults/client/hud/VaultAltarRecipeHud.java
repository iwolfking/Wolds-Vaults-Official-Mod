package xyz.iwolfking.woldsvaults.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.VaultMod;
import iskallia.vault.bounty.Bounty;
import iskallia.vault.bounty.BountyList;
import iskallia.vault.bounty.client.ClientBountyData;
import iskallia.vault.bounty.task.*;
import iskallia.vault.bounty.task.properties.*;
import iskallia.vault.client.gui.screen.bounty.BountyScreen;
import iskallia.vault.client.gui.screen.bounty.element.BountyElement;
import iskallia.vault.init.ModKeybinds;
import iskallia.vault.util.EntityGroupsUtils;
import iskallia.vault.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;
import java.util.*;

public class VaultAltarRecipeHud implements IIngameOverlay {

    protected static final ResourceLocation BOUNTY_PROGRESS_LOCATION = VaultMod.id("textures/gui/bounty_progress.png");
    private HashMap<UUID, Set<String>> killEntityTargets = new HashMap();
    long time = 1000L;
    long deltaTime;
    HashMap<UUID, Integer> currentIndex = new HashMap();

    public VaultAltarRecipeHud() {
    }

    public void render(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.hideGui) {
            if (!ClientBountyData.INSTANCE.getBounties().isEmpty()) {
                this.updateTime();
                if (ModKeybinds.bountyStatusKey.isDown()) {
                    this.renderBounties(gui, poseStack, partialTick, width, height);
                }

            }
        }
    }

    private void renderBounties(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        ProfilerFiller profiler = mc.getProfiler();
        Player player = mc.player;
        if (player != null) {
            profiler.push("BountyProgressOverlay");
            int midX = mc.getWindow().getGuiScaledWidth() / 2;
            int midY = mc.getWindow().getGuiScaledHeight() / 2;
            int bountyHeight = 50;
            BountyList bounties = ClientBountyData.INSTANCE.getBounties();
            this.clearCompletedTargets(bounties);
            int startY = midY - bounties.size() * bountyHeight / 2;
            int y = startY;

            for(Iterator var15 = bounties.iterator(); var15.hasNext(); y += bountyHeight + 5) {
                Bounty bounty = (Bounty)var15.next();
                RenderSystem.setShaderTexture(0, BOUNTY_PROGRESS_LOCATION);
                this.renderBounty(bounty, gui, poseStack, midX, y);
            }

            if (this.time <= 0L) {
                this.time = 1000L;
            }

            profiler.pop();
        }
    }

    private void clearCompletedTargets(BountyList bounties) {
        Set<UUID> completedTargets = new HashSet();
        Iterator var3 = this.killEntityTargets.keySet().iterator();

        while(var3.hasNext()) {
            UUID uuid = (UUID)var3.next();
            if (!bounties.contains(uuid)) {
                completedTargets.add(uuid);
            }
        }

        HashMap var10001 = this.killEntityTargets;
        Objects.requireNonNull(var10001);
        completedTargets.forEach(var10001::remove);
    }

    private void updateTime() {
        long currentTime = System.currentTimeMillis();
        if (this.deltaTime == 0L) {
            this.deltaTime = currentTime;
        }

        if (currentTime > this.deltaTime) {
            this.time -= currentTime - this.deltaTime;
        }

        this.deltaTime = currentTime;
    }

    private void updateIndex(UUID bountyId) {
        Set<String> targets = (Set)this.killEntityTargets.getOrDefault(bountyId, Set.of());
        if (targets.isEmpty()) {
            this.currentIndex.remove(bountyId);
        } else {
            int current = this.currentIndex.get(bountyId) == null ? 0 : (Integer)this.currentIndex.get(bountyId);
            if (current + 1 >= targets.size()) {
                current = 0;
            } else {
                ++current;
            }

            this.currentIndex.put(bountyId, current);
        }
    }

    private void renderBounty(Bounty bounty, ForgeIngameGui gui, PoseStack poseStack, int midX, int startY) {
        Task<?> task = bounty.getTask();
        int width = 130;
        int iconY = 62;
        int iconWidth = 14;
        int iconHeight = 16;
        BountyElement.Status status = task.getProperties().getRewardPool().equalsIgnoreCase("legendary") ? BountyElement.Status.LEGENDARY : BountyElement.Status.ACTIVE;
        ChatFormatting color = status == BountyElement.Status.LEGENDARY ? ChatFormatting.YELLOW : ChatFormatting.GREEN;
        TextComponent target;
        byte iconX;
        if (task instanceof KillEntityTask killEntityTask) {
            this.killEntityTargets.computeIfAbsent(bounty.getId(), (id) -> {
                return EntityGroupsUtils.getDescriptions(((KillEntityProperties)killEntityTask.getProperties()).getFilter());
            });
            if (this.time <= 0L) {
                this.updateIndex(bounty.getId());
            }

            List<String> components = List.copyOf((Collection)this.killEntityTargets.get(bounty.getId()));
            target = new TextComponent((String)components.get((Integer)this.currentIndex.get(bounty.getId())));
            iconX = 0;
        } else if (task instanceof CompletionTask completionTask) {
            target = (TextComponent) BountyScreen.OBJECTIVE_NAME.getOrDefault(((CompletionProperties)completionTask.getProperties()).getId(), new TextComponent("Empty - Report to Dev"));
            iconX = 14;
        } else {
            Item item;
            if (task instanceof ItemSubmissionTask itemSubmissionTask) {
                item = (Item) ForgeRegistries.ITEMS.getValue(((ItemSubmissionProperties)itemSubmissionTask.getProperties()).getItemId());
                target = item == null ? TextUtil.formatLocationPathAsProperNoun(((ItemSubmissionProperties)itemSubmissionTask.getProperties()).getItemId()) : new TextComponent(item.getDescription().getString());
                iconX = 28;
            } else if (task instanceof ItemDiscoveryTask itemDiscoveryTask) {
                item = (Item)ForgeRegistries.ITEMS.getValue(((ItemDiscoveryProperties)itemDiscoveryTask.getProperties()).getItemId());
                target = item == null ? TextUtil.formatLocationPathAsProperNoun(((ItemDiscoveryProperties)itemDiscoveryTask.getProperties()).getItemId()) : new TextComponent(item.getDescription().getString());
                iconX = 42;
            } else {
                if (!(task instanceof MiningTask)) {
                    return;
                }

                MiningTask miningTask = (MiningTask)task;
                Block block = (Block)ForgeRegistries.BLOCKS.getValue(((MiningProperties)miningTask.getProperties()).getBlockId());
                target = block == null ? TextUtil.formatLocationPathAsProperNoun(((MiningProperties)miningTask.getProperties()).getBlockId()) : new TextComponent(block.getName().getString());
                iconX = 56;
            }
        }

        target.getStyle().applyFormat(color);
        int headerX = 0;
        int headerY = 14;
        int headerHeight = 22;
        int x = 1;
        gui.blit(poseStack, x + 3, startY + 23, 20, 36, 124, 15);
        gui.blit(poseStack, x, startY + 2, headerX, headerY, width, headerHeight);
        int iconBoxX = 0;
        int iconBoxY = 36;
        int iconBoxWidth = 20;
        int iconBoxHeight = 26;
        gui.blit(poseStack, x + 4, startY, iconBoxX, iconBoxY, iconBoxWidth, iconBoxHeight);
        gui.blit(poseStack, x + 7, startY + 5, iconX, iconY, iconWidth, iconHeight);
        gui.blit(poseStack, x + 5, startY + 28, 0, 0, 120, 7);
        float progress = (float)(task.getAmountObtained() / task.getProperties().getAmount());
        gui.blit(poseStack, x + 5, startY + 28, 0, 7, (int)(120.0F * progress), 7);
        DecimalFormat df = new DecimalFormat("0");
        String var10002 = df.format(task.getAmountObtained());
        TextComponent progressComponent = new TextComponent(var10002 + "/" + df.format(task.getProperties().getAmount()));
        int progressX = x + width / 2 - Minecraft.getInstance().font.width(progressComponent) / 2;
        GuiComponent.drawString(poseStack, Minecraft.getInstance().font, target, x + 25, startY + 9, color.getColor());
        GuiComponent.drawString(poseStack, Minecraft.getInstance().font, progressComponent, progressX, startY + 27, 16777215);
    }
}