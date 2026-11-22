package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.crystal_workbench;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import iskallia.vault.block.entity.CrystalWorkbenchWorkspace;
import iskallia.vault.container.crystal.CrystalWorkbenchPreview;
import iskallia.vault.recipe.anvil.AnvilExecutor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.api.lib.CrystalWorkbenchPreviewContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//TOOD: Make sure applying Maps with Capstones/other ingredients works as expected
@Mixin(value = CrystalWorkbenchPreview.class, remap = false)
public class MixinCrystalWorkbenchPreview {

    @Unique
    private static final ThreadLocal<CrystalWorkbenchPreviewContext> WOLDS_VAULTS$CONTEXT = new ThreadLocal<>();

    @Inject(method = "compute", at = @At("HEAD"))
    private static void woldsVaults$captureContext(
            Player player,
            CrystalWorkbenchWorkspace workspace,
            CallbackInfoReturnable<CrystalWorkbenchPreview.Outcome> cir
    ) {
        WOLDS_VAULTS$CONTEXT.set(new CrystalWorkbenchPreviewContext(player, workspace));
    }

    @WrapOperation(
            method = "compute",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;of([Ljava/lang/Object;)Ljava/util/List;"
            )
    )
    private static List<Object> woldsVaults$sanitizeSocketArray(Object[] elements, Operation<List<Object>> original) {
        if (elements instanceof CrystalWorkbenchPreview.SocketState[] states) {
            for (int i = 0; i < states.length; i++) {
                if (states[i] == null) {
                    states[i] = CrystalWorkbenchPreview.SocketState.EMPTY;
                }
            }
            CrystalWorkbenchPreviewContext ctx = WOLDS_VAULTS$CONTEXT.get();
            if (ctx != null) {
                CrystalWorkbenchWorkspace.Socket miscSocket = woldsVaults$miscSocket();
                if (miscSocket != null && miscSocket.ordinal() < states.length) {
                    ItemStack miscStack = ctx.workspace.socket(miscSocket);
                    if (!miscStack.isEmpty()) {
                        ItemStack baseCrystal = ctx.workspace.crystal().copy();
                        Optional<AnvilExecutor.Application> app = AnvilExecutor.applyOnce(ctx.player, baseCrystal, miscStack);
                        if (app.isPresent()) {
                            states[miscSocket.ordinal()] = new CrystalWorkbenchPreview.SocketState(true, true, "");
                            ctx.miscApplication = app.get();
                        } else {
                            states[miscSocket.ordinal()] = new CrystalWorkbenchPreview.SocketState(
                                    true,
                                    false,
                                    "This modifier doesn't fit this crystal"
                            );
                        }
                    }
                }
            }
        }
        return original.call(new Object[]{elements});
    }

    @ModifyReturnValue(method = "compute", at = @At("RETURN"))
    private static CrystalWorkbenchPreview.Outcome woldsVaults$applyMiscToOutcome(CrystalWorkbenchPreview.Outcome outcome) {
        CrystalWorkbenchPreviewContext ctx = WOLDS_VAULTS$CONTEXT.get();
        WOLDS_VAULTS$CONTEXT.remove();
        if (ctx == null || ctx.miscApplication == null) {
            return outcome;
        }

        AnvilExecutor.Application miscApp = ctx.miscApplication;
        CrystalWorkbenchPreview.Report oldReport = outcome.report();

        ItemStack startingCrystal = outcome.result().isEmpty() ? ctx.workspace.crystal().copy() : outcome.result();
        Optional<AnvilExecutor.Application> applied = AnvilExecutor.applyOnce(ctx.player, startingCrystal, ctx.workspace.socket(CrystalWorkbenchWorkspace.Socket.valueOf("MISC")));
        if (applied.isEmpty()) {
            return outcome;
        }

        List<ItemStack> mergedExtras = new ArrayList<>(outcome.extras());
        mergedExtras.addAll(applied.get().extras());

        CrystalWorkbenchPreview.Report updatedReport = new CrystalWorkbenchPreview.Report(
                oldReport.hasCrystal(),
                true,
                oldReport.sockets(),
                oldReport.rows(),
                oldReport.cursesAdded()
        );

        return new CrystalWorkbenchPreview.Outcome(applied.get().output(), mergedExtras, updatedReport);
    }

    @Inject(method = "socketRefusalReason", at = @At("HEAD"), cancellable = true)
    private static void woldsVaults$miscRefusalReason(
            CrystalWorkbenchWorkspace.Socket socket,
            ItemStack item,
            ItemStack crystal,
            CallbackInfoReturnable<String> cir
    ) {
        if (socket == woldsVaults$miscSocket()) {
            cir.setReturnValue("This modifier doesn't fit this crystal");
        }
    }

    @Unique
    private static CrystalWorkbenchWorkspace.Socket woldsVaults$miscSocket() {
        for (CrystalWorkbenchWorkspace.Socket socket : CrystalWorkbenchWorkspace.Socket.values()) {
            if ("MISC".equals(socket.name())) {
                return socket;
            }
        }
        return null;
    }
}