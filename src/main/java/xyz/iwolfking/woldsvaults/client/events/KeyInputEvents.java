package xyz.iwolfking.woldsvaults.client.events;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrcrayfish.configured.api.util.ConfigScreenHelper;
import iskallia.vault.client.gui.screen.accessibility.InventoryHudEditScreen;
import iskallia.vault.item.gear.TrinketItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.integration.bettercombat.BetterCombatToggleHelper;
import xyz.iwolfking.woldsvaults.client.init.ModKeybinds;
import xyz.iwolfking.woldsvaults.client.screens.SpeedCapConfigScreen;
import xyz.iwolfking.woldsvaults.effect.trinkets.SpeedLimitTrinketEffect;
import xyz.iwolfking.woldsvaults.client.screens.PouchScreen;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.items.trinket_pouch.menu.PouchMenu;
import xyz.iwolfking.woldsvaults.network.packets.ServerboundOpenTrinketPouchPacket;
import top.theillusivec4.curios.common.inventory.CurioSlot;

import java.util.OptionalInt;

@Mod.EventBusSubscriber(modid = WoldsVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyInputEvents {
    public static boolean isFeatherFixEnabled = true;

    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/block/stone");

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (ModKeybinds.isFeatherFixed.consumeClick()) {
            isFeatherFixEnabled = !isFeatherFixEnabled;

            String state = isFeatherFixEnabled ? "ON" : "OFF";
            var player = Minecraft.getInstance().player;
            if (player != null){
                player.displayClientMessage(new TextComponent("Toggled Feather Fix: " + state).withStyle(ChatFormatting.YELLOW), true);
            }
        }

        if(ModKeybinds.openWoldsVaultsConfig.consumeClick()) {
            ModContainer woldsModContainer =  ModList.get().getModContainerById(WoldsVaults.MOD_ID).orElse(null);
            if(woldsModContainer != null) {
                Screen configScreen = ConfigScreenHelper.createForgeConfigSelectionScreen(new TextComponent("Wold's Vaults Configs"), ModList.get().getModContainerById(WoldsVaults.MOD_ID).orElse(null), BACKGROUND);
                Minecraft.getInstance().setScreen(configScreen);
            }
        }

        if(ModKeybinds.toggleBetterCombat.consumeClick()) {
            BetterCombatToggleHelper.toggleBetterCombat();
        }

        if(ModKeybinds.openInventoryHUD.consumeClick()) {
            Minecraft.getInstance().setScreen(new InventoryHudEditScreen(Minecraft.getInstance().screen));
        }

        if (ModKeybinds.openTrinketPouch.consumeClick()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && Minecraft.getInstance().screen == null) {
                ModNetwork.sendToServer(new ServerboundOpenTrinketPouchPacket(player.containerMenu.containerId, PouchMenu.EQUIPPED));
            }
        }
    }

    @SubscribeEvent
    public static void onGuiMouseClicked(ScreenEvent.MouseClickedEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getButton() != 1 || Screen.hasShiftDown() || minecraft.player == null
                || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)
                || screen instanceof PouchScreen || !screen.getMenu().getCarried().isEmpty()) {
            return;
        }
        Slot slot = slotAt(screen, event.getMouseX(), event.getMouseY());
        if (slot == null || !PouchHelper.isPouch(slot.getItem())) {
            return;
        }
        OptionalInt pouchSlot = openablePouchSlot(slot, minecraft.player);
        if (pouchSlot.isEmpty()) {
            return;
        }
        event.setCanceled(true);
        ModNetwork.sendToServer(new ServerboundOpenTrinketPouchPacket(minecraft.player.containerMenu.containerId, pouchSlot.getAsInt()));
    }

    private static Slot slotAt(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        for (Slot slot : screen.getMenu().slots) {
            int left = screen.getGuiLeft() + slot.x;
            int top = screen.getGuiTop() + slot.y;
            if (slot.isActive() && mouseX >= left - 1 && mouseX < left + 17 && mouseY >= top - 1 && mouseY < top + 17) {
                return slot;
            }
        }
        return null;
    }

    private static OptionalInt openablePouchSlot(Slot slot, LocalPlayer player) {
        if (slot instanceof CurioSlot curioSlot && curioSlot.getIdentifier().equals("trinket_pouch") && curioSlot.getSlotIndex() == 0) {
            return OptionalInt.of(PouchMenu.EQUIPPED);
        }
        if (slot.container == player.getInventory() && slot.mayPickup(player)) {
            return OptionalInt.of(slot.getSlotIndex());
        }
        return OptionalInt.empty();
    }

    @SubscribeEvent
    public static void onGuiKeyPressed(ScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }
        InputConstants.Key pressedKey = InputConstants.getKey(event.getKeyCode(), event.getScanCode());
        if (!ModKeybinds.configureTrinket.isActiveAndMatches(pressedKey)) {
            return;
        }
        Slot hovered = containerScreen instanceof PouchScreen pouchScreen
                ? pouchScreen.trinketConfigurationSlot() : containerScreen.getSlotUnderMouse();
        if (hovered == null || !hovered.hasItem()) {
            return;
        }
        ItemStack stack = hovered.getItem();
        if (!SpeedLimitTrinketEffect.hasBootsEffect(stack) || !TrinketItem.isIdentified(stack)) {
            return;
        }
        boolean creative = containerScreen instanceof CreativeModeInventoryScreen;
        if (creative && !(hovered.container instanceof Inventory)) {
            WoldsVaults.LOGGER.warn("Weighted Boots: hovered creative slot is not backed by the player inventory, cannot open the speed config screen.");
            return;
        }
        int slotIndex = creative ? hovered.getSlotIndex() : hovered.index;
        minecraft.setScreen(new SpeedCapConfigScreen(containerScreen, creative, slotIndex, SpeedLimitTrinketEffect.getCapPercent(stack)));
        event.setCanceled(true);
    }
}
