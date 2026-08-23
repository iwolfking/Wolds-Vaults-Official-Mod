package xyz.iwolfking.woldsvaults.mixins.vaulthunters.gods;

import iskallia.vault.client.gui.screen.summary.VaultEndScreen;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.VaultSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.gods.ClientVaultGodXp;

import java.util.UUID;

@Mixin(value = VaultEndScreen.class, remap = false)
public abstract class MixinVaultEndScreenGodXp {
    @Shadow
    @Final
    private VaultSnapshot snapshot;
    @Shadow
    @Final
    private boolean isHistory;
    @Shadow
    @Final
    private boolean fromLink;
    @Shadow
    @Final
    private UUID asPlayer;

    /**
     * @author PoorMansPhysicist
     * @reason append the god experience a mapped vault paid out to the vault end screen's experience
     * label, colored by the map's god. The local's declared type in the shipped class is
     * {@code Component}, which the handler must match for the STORE discriminator to bind.
     */
    @ModifyVariable(method = "<init>(Liskallia/vault/core/vault/stat/VaultSnapshot;Lnet/minecraft/network/chat/Component;Ljava/util/UUID;ZZ)V",
            at = @At("STORE"), name = "xpGainedComponent")
    private Component woldsVaults$appendGodXp(Component xpGainedComponent) {
        try {
            if (!(xpGainedComponent instanceof MutableComponent mutable) || this.isHistory || this.fromLink) {
                return xpGainedComponent;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || !minecraft.player.getUUID().equals(this.asPlayer)) {
                return xpGainedComponent;
            }
            Vault vault = this.snapshot.getEnd();
            if (vault == null || !vault.has(Vault.ID)) {
                return xpGainedComponent;
            }
            ClientVaultGodXp.peek(vault.get(Vault.ID)).ifPresent(award ->
                    mutable.append(new TextComponent(String.format("  +%,d xp", award.amount()))
                            .setStyle(Style.EMPTY.withColor(award.god().getChatColor()))));
        } catch (Exception e) {
            WoldsVaults.LOGGER.error("Failed to append god xp to the vault end screen; showing vanilla xp only.", e);
        }
        return xpGainedComponent;
    }
}
