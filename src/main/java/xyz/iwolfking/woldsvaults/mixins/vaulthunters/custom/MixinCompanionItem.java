package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.recipe.anvil.AnvilContext;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.core.WoldLogger;
import xyz.iwolfking.woldsvaults.api.util.CrystalDataUtils;
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;
import xyz.iwolfking.woldsvaults.modifiers.vault.PlayerNoTemporalShardModifier;

import java.util.Collection;
import java.util.List;

@Mixin(value = CompanionItem.class, remap = false)
public class MixinCompanionItem implements IVaultCrystalModifier {
    @Inject(method = "activateTemporalModifier", at = @At("HEAD"), cancellable = true)
    private static void preventActivation(ServerPlayer player, ItemStack stack, Vault vault, CallbackInfo ci) {
        if(vault.get(Vault.MODIFIERS).getModifiers().stream().anyMatch(vaultModifier -> vaultModifier instanceof PlayerNoTemporalShardModifier)) {
            player.displayClientMessage(new TextComponent("The Companion does not respond."), true);
            ci.cancel();
        }
    }

    @Override
    public boolean applyCrystalRecipe(AnvilContext context, CrystalData data, ItemStack ingredient, ItemStack output) {
        if(ingredient.getItem() instanceof CompanionItem) {
            if(context.getPlayer().isEmpty()) {
                return false;
            }

            if(CompanionItem.isOnCooldown(ingredient) || !CompanionItem.isOwner(ingredient, context.getPlayer().get()) || CompanionItem.getCompanionHearts(ingredient) <= 0) {
                return false;
            }

            Collection<Pair<Integer, List<ResourceLocation>>> relics = CompanionItem.getAllRelics(ingredient).values();
            WoldLogger.info(String.valueOf(relics.size()));
            relics.forEach(integerListPair -> {
                List<ResourceLocation> modifierIds = integerListPair.getSecond();
                modifierIds.forEach(modifierId -> {
                    VaultModifier<?> modifier = VaultModifierRegistry.get(modifierId);
                    if(modifier == null) {
                        return;
                    }

                    VaultModifierStack stack = new VaultModifierStack(modifier, 1);
                    if(data.addModifierByCrafting(stack, true, true)) {
                        data.addModifierByCrafting(stack, true, false);
                    }
                });
            });

            VaultModifierStack stack = new VaultModifierStack(VaultModifierRegistry.get(VaultMod.id("no_companion")), 1);

            if(data.addModifierByCrafting(stack, true, true)) {
                data.addModifierByCrafting(stack, true, false);
            }

            data.write(output);
            context.setOutput(output);

            context.onTake(context.getTake().append(() -> {
                context.getInput()[0].shrink(1);
                CompanionItem.setCompanionCooldown(context.getInput()[1], 5);
            }));
            return true;
        }

        return false;
    }

    @Override
    public int getCapacityConsumption(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean hasApplied(ItemStack crystalStack) {
        CrystalData data = CrystalData.read(crystalStack);
        return CrystalDataUtils.hasCountOfModifiers(VaultMod.id("no_companion"), data, 1);
    }
}
