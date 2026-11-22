package xyz.iwolfking.woldsvaults.api.lib;

import iskallia.vault.block.entity.CrystalWorkbenchWorkspace;
import iskallia.vault.recipe.anvil.AnvilExecutor;
import net.minecraft.world.entity.player.Player;

public final class CrystalWorkbenchPreviewContext {
    public final Player player;
    public final CrystalWorkbenchWorkspace workspace;
    public AnvilExecutor.Application miscApplication;

    public CrystalWorkbenchPreviewContext(Player player, CrystalWorkbenchWorkspace workspace) {
        this.player = player;
        this.workspace = workspace;
    }
}
