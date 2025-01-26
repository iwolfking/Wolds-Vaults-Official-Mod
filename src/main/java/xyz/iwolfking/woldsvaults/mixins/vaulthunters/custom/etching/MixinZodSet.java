package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.etching;

import iskallia.vault.etching.EtchingSet;
import iskallia.vault.init.ModEtchings;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModEtchings.class,  remap = false)
public class MixinZodSet {

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V", ordinal = 11))
    private static void replaceZodSet(IForgeRegistry<EtchingSet<?>> instance, IForgeRegistryEntry<EtchingSet<?>> v){
        instance.register(xyz.iwolfking.woldsvaults.init.ModEtchings.ZOD);
    }
}
