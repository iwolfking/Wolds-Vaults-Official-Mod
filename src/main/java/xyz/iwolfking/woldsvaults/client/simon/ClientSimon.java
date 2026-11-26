package xyz.iwolfking.woldsvaults.client.simon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import xyz.iwolfking.woldsvaults.entities.simon.SimonCrystalEntity;
import xyz.iwolfking.woldsvaults.init.ModNetwork;
import xyz.iwolfking.woldsvaults.network.message.SimonActionMessage;


/**
 * The client side of Simon's Set. A right click that nothing else used (no block opened, no entity touched, an item
 * in the main hand with no use of its own) shoots a prism seed, or with sneak held resonates the crystals; either
 * way it is used up so the off hand doesn't act on it too. While a sequence waits for hits, a left click aimed at
 * one of its crystals (out to the set's click range, not through blocks) is taken before any attack runs and sent
 * as a hit on that crystal, so it is the crosshair that picks the crystal, never a sweeping swing.
 */
public final class ClientSimon {
    private static boolean canShoot;
    private static int cooldownTicks;
    private static boolean canResonate;
    private static float clickRange;
    private static long readyTick;
    private static int[] waiting = new int[0];

    private ClientSimon() {
    }

    public static void set(boolean canShoot, int cooldownTicks, boolean canResonate, float clickRange) {
        ClientSimon.canShoot = canShoot;
        ClientSimon.cooldownTicks = cooldownTicks;
        ClientSimon.canResonate = canResonate;
        ClientSimon.clickRange = clickRange;
    }

    public static void waitFor(int[] crystalIds) {
        waiting = crystalIds;
    }

    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getPlayer();
        if (!event.getWorld().isClientSide || event.getHand() != InteractionHand.MAIN_HAND || player != Minecraft.getInstance().player || player.isSpectator()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.getUseAnimation() != UseAnim.NONE || stack.getItem() instanceof BlockItem) {
            return;
        }
        long now = player.level.getGameTime();
        if (player.isShiftKeyDown() && canResonate) {
            ModNetwork.sendToServer(new SimonActionMessage(SimonActionMessage.Action.RESONATE, 0));
        } else if (!player.isShiftKeyDown() && canShoot && now >= readyTick) {
            ModNetwork.sendToServer(new SimonActionMessage(SimonActionMessage.Action.SHOOT, 0));
            readyTick = now + cooldownTicks;
        } else {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    /**
     * At the start of the tick, takes a left click aimed at a waiting crystal before any attack sees it and sends it as
     * a hit on that crystal.
     */
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (event.phase != TickEvent.Phase.START || waiting.length == 0 || player == null || mc.screen != null || player.isSpectator()) {
            return;
        }
        SimonCrystalEntity aimed = aimedCrystal(player);
        if (aimed == null || !mc.options.keyAttack.consumeClick()) {
            return;
        }
        while (mc.options.keyAttack.consumeClick()) {
        }
        player.swing(InteractionHand.MAIN_HAND);
        ModNetwork.sendToServer(new SimonActionMessage(SimonActionMessage.Action.HIT, aimed.getId()));
    }

    /** The nearest waiting crystal under the crosshair, nearer than any block in the way. */
    private static SimonCrystalEntity aimedCrystal(LocalPlayer player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(clickRange));
        BlockHitResult block = player.level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double best = block.getType() == HitResult.Type.MISS ? clickRange : block.getLocation().distanceTo(eye);
        SimonCrystalEntity aimed = null;
        for (int id : waiting) {
            Entity entity = player.level.getEntity(id);
            if (!(entity instanceof SimonCrystalEntity crystal) || crystal.isRemoved()) {
                continue;
            }
            AABB box = crystal.getBoundingBox().inflate(0.2);
            double distance = box.contains(eye) ? 0.0 : box.clip(eye, end).map(hit -> hit.distanceTo(eye)).orElse(Double.MAX_VALUE);
            if (distance < best) {
                best = distance;
                aimed = crystal;
            }
        }
        return aimed;
    }

    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        set(false, 0, false, 0.0F);
        waiting = new int[0];
        readyTick = 0;
    }
}
