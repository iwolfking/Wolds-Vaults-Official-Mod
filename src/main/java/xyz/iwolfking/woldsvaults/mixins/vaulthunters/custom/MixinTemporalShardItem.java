package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.IdentifiableItem;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.gear.TemporalShardItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.modifiers.vault.PlayerNoTemporalShardModifier;

import javax.annotation.Nullable;

@Mixin(value = TemporalShardItem.class, remap = false)
public abstract class MixinTemporalShardItem implements IdentifiableItem{

    @Inject(method = "lambda$use$4", at = @At("HEAD"), cancellable = true)
    private static void preventUse(ItemStack stack, Player player, Level level, Vault vault, CallbackInfo ci) {
        if(vault.get(Vault.MODIFIERS).getModifiers().stream().anyMatch(vaultModifier -> vaultModifier instanceof PlayerNoTemporalShardModifier)) {
            player.displayClientMessage(new TextComponent("The relic does not respond."), true);
            ci.cancel();
        }
    }

    @Inject(method = "isIdentified", at = @At("HEAD"), cancellable = true)
    private static void identificationCheck(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if (!stack.isEmpty() && stack.getItem() instanceof TemporalShardItem) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("identified", Tag.TAG_ANY_NUMERIC)) {
                cir.setReturnValue(tag.getBoolean("identified"));
            }
        }
    }

    /**
     * @author
     * @reason identify without attributegeardata, this makes identified temporal shards stackable
     */
    @Overwrite
    public void tickFinishRoll(ItemStack stack, @Nullable Player player, boolean identify) {
        if (!stack.isEmpty() && stack.getItem() instanceof TemporalShardItem) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean("identified", true);
            tag.remove("vaultGearData");
            tag.remove("clientCache");
        }
    }

    @Override
    public VaultGearState getState(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof TemporalShardItem) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("identified", Tag.TAG_ANY_NUMERIC)) {
                return VaultGearState.IDENTIFIED;
            }
        }

        // original
        AttributeGearData data = AttributeGearData.read(stack);
        if (data instanceof VaultGearData gearData) {
            return gearData.getState();
        }
        return data.getFirstValue(ModGearAttributes.STATE).orElse(VaultGearState.UNIDENTIFIED);
    }
}
