package xyz.iwolfking.woldsvaults.client;

import com.mojang.blaze3d.platform.InputConstants;
import iskallia.vault.client.data.ClientPatreonGearStats;
import iskallia.vault.gear.VaultGearType;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.stats.PatreonGearStatHelper;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.www.patreon.PatreonManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

/**
 * Client-only parts of the gear tooltip overwrite. The mixin body cannot name client classes such as
 * {@code Minecraft}: a dedicated server fails to transform the mixin when it does.
 */
public final class GearTooltipClient {
    private GearTooltipClient() {
    }

    /** Whether either shift key is held in the game window. */
    public static boolean isShiftDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, 340) || InputConstants.isKeyDown(window, 344);
    }

    /** Adds the Patreon kill or damage-taken line for the local player's tracked gear. */
    public static void addPatreonStats(ItemStack stack, List<Component> tooltip) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !PatreonManager.getInstance().isUserPatreon(minecraft.player.getUUID())) {
            return;
        }
        if (!(stack.getItem() instanceof VaultGearItem gearItem)) {
            return;
        }
        VaultGearType type = gearItem.getGearType(stack);
        if (type != VaultGearType.SWORD && type != VaultGearType.AXE) {
            if (PatreonGearStatHelper.isTrackableArmor(stack)) {
                double damageTaken = ClientPatreonGearStats.getDamageTaken(stack);
                if (damageTaken > 0.0D) {
                    String formatted = String.format(Locale.US, "%.1f", damageTaken);
                    tooltip.add(new TextComponent("Damage Taken: ").withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("damage"))).append(new TextComponent(formatted).withStyle(ChatFormatting.WHITE)));
                }
            }
        } else {
            int kills = ClientPatreonGearStats.getKills(stack);
            if (kills > 0) {
                tooltip.add(new TextComponent("Killed ").withStyle(Style.EMPTY.withColor(ModConfigs.COLORS.getColor("damage"))).append(new TextComponent(String.valueOf(kills)).withStyle(ChatFormatting.WHITE)).append(" mobs"));
            }
        }
    }
}
