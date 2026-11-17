package xyz.iwolfking.woldsvaults.integration.curios;

import iskallia.vault.integration.IntegrationCurios;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import xyz.iwolfking.woldsvaults.armory.SlotKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The armory's view of a player's curio slots, addressed by armory slot key. */
public final class ArmoryCurioSlots {
    private ArmoryCurioSlots() {
    }

    /**
     * The key of every visible curio slot {@code player} has, every index, in the order the H screen's curio column
     * lists them. Slots in {@link SlotKeys#NEVER_TOUCHED_CURIOS} are left out.
     */
    public static List<String> keys(Player player) {
        List<String> keys = new ArrayList<>();
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                ICurioStacksHandler stacks = entry.getValue();
                if (!stacks.isVisible() || SlotKeys.NEVER_TOUCHED_CURIOS.contains(entry.getKey())) {
                    continue;
                }
                for (int i = 0; i < stacks.getStacks().getSlots(); i++) {
                    keys.add(SlotKeys.curio(entry.getKey(), i));
                }
            }
        });
        return keys;
    }

    public static boolean has(Player player, String key) {
        int index = SlotKeys.curioIndex(key);
        return CuriosApi.getCuriosHelper().getCuriosHandler(player)
                .resolve()
                .flatMap(handler -> handler.getStacksHandler(SlotKeys.curioId(key)))
                .map(stacks -> index >= 0 && index < stacks.getStacks().getSlots())
                .orElse(false);
    }

    public static ItemStack get(Player player, String key) {
        if (!has(player, key)) {
            return ItemStack.EMPTY;
        }
        return IntegrationCurios.getCurioItemStack(player, SlotKeys.curioId(key), SlotKeys.curioIndex(key));
    }

    /** Sets the slot; returns false when the player has no such slot or the slot refused the item. */
    public static boolean set(Player player, String key, ItemStack stack) {
        if (!has(player, key)) {
            return false;
        }
        return IntegrationCurios.setCurioItemStack(player, stack, SlotKeys.curioId(key), SlotKeys.curioIndex(key));
    }

    /** Whether the curio's own equip rule (deck level minimum, etc.) lets {@code player} wear {@code stack}. */
    public static boolean canEquip(Player player, String key, ItemStack stack) {
        SlotContext context = new SlotContext(SlotKeys.curioId(key), player, SlotKeys.curioIndex(key), false, true);
        return CuriosApi.getCuriosHelper().getCurio(stack).map(curio -> curio.canEquip(context)).orElse(true);
    }

    /** The empty-slot icon Curios draws for the slot type of {@code key}. Client only. */
    public static ResourceLocation icon(String key) {
        return CuriosApi.getIconHelper().getIcon(SlotKeys.curioId(key));
    }

    /** The slot type's display name, as the Curios screen shows it. */
    public static Component name(String key) {
        return new TranslatableComponent("curios.identifier." + SlotKeys.curioId(key));
    }

    /** Whether {@code stack} is tagged for the curio slot type of {@code key}. */
    public static boolean fits(String key, ItemStack stack) {
        return CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).contains(SlotKeys.curioId(key));
    }
}
