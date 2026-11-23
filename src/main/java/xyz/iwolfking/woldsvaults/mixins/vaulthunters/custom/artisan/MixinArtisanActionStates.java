package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.artisan;

import iskallia.vault.container.VaultArtisanStationContainer;
import iskallia.vault.gear.modification.ArtisanActions;
import iskallia.vault.gear.modification.GearModification;
import iskallia.vault.gear.modification.GearModificationAction;
import iskallia.vault.stash.StationStash;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.iwolfking.woldsvaults.init.ModGearModifications;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Map;

@Mixin(targets = "iskallia.vault.client.gui.screen.block.artisan.ArtisanActionStates", remap = false)
public class MixinArtisanActionStates {
    @Shadow
    @Final
    private VaultArtisanStationContainer menu;

    @Shadow
    @Final
    private Map<GearModification, Object> focusStates;

    @Shadow
    private ItemStack gear;

    @Unique
    private static final MethodHandle woldsVaults$STATE_CTOR = woldsVaults$findStateConstructor();


    @Inject(method = "refresh", at = @At("TAIL"))
    private void woldsVaults$ensureUnusualModifierState(ItemStack shownGear, long now, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ModGearModifications.getWoldGearModifications().forEach(gearModification -> {
            if (!this.focusStates.containsKey(gearModification)) {
                ItemStack material = woldsVaults$resolveMaterial(gearModification);
                GearModificationAction.Stock stockView = this.menu.stockView();
                this.focusStates.put(
                        gearModification,
                        woldsVaults$buildFallbackState(player, gearModification, material, stockView)
                );
            }
        });

    }

    @Inject(method = "focus", at = @At("HEAD"), cancellable = true)
    private void woldsVaults$fallbackMissingFocus(
            GearModification modification, CallbackInfoReturnable<Object> cir
    ) {
        Object existing = this.focusStates.get(modification);
        if (existing != null) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack material = woldsVaults$resolveMaterial(modification);
            GearModificationAction.Stock stockView = this.menu.stockView();
            Object fallback = woldsVaults$buildFallbackState(player, modification, material, stockView);
            this.focusStates.put(modification, fallback);
            cir.setReturnValue(fallback);
        }
    }

    @Unique
    private static ItemStack woldsVaults$resolveMaterial(GearModification modification) {
        for (ArtisanActions.Action action : ArtisanActions.buttons()) {
            if (action.modification().equals(modification)) {
                return action.material();
            }
        }
        ArtisanActions.Action faceted = ArtisanActions.faceted();
        if (faceted != null && faceted.modification().equals(modification)) {
            return faceted.material();
        }
        return ItemStack.EMPTY;
    }

    @Unique
    private Object woldsVaults$buildFallbackState(
            Player player,
            GearModification modification,
            ItemStack material,
            GearModificationAction.Stock stockView
    ) {
        if (woldsVaults$STATE_CTOR == null) {
            return null;
        }

        GearModificationAction.Check check = GearModificationAction.check(player, this.gear, material, stockView);
        long stash = material.isEmpty() ? 0L : StationStash.available(player, material);
        Component error = check.error();

        try {
            return woldsVaults$STATE_CTOR.invoke(
                    modification.getRegistryName() != null ? modification.getRegistryName().toString() : modification.toString(),
                    modification,
                    material,
                    null,
                    check.focusHave(),
                    stash,
                    check.usable(),
                    error,
                    check.blockedByOperation() || this.gear.isEmpty(),
                    check.potentialUse(),
                    check.refillsPotential(),
                    check.cost().costPlating(),
                    check.cost().costBronze(),
                    check.enoughPotential(),
                    check.enoughPlating(),
                    check.enoughBronze(),
                    modification.getDescription(material)
            );
        } catch (Throwable t) {
            return null;
        }
    }

    @Unique
    private static MethodHandle woldsVaults$findStateConstructor() {
        try {
            Class<?> stateClass = Class.forName("iskallia.vault.client.gui.screen.block.artisan.ArtisanActionState");
            for (Constructor<?> ctor : stateClass.getDeclaredConstructors()) {
                if (ctor.getParameterCount() == 17) {
                    ctor.setAccessible(true);
                    return MethodHandles.lookup().unreflectConstructor(ctor).asSpreader(Object[].class, 0);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
