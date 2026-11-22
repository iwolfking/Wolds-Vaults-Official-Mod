package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom.crystal_workbench;

import iskallia.vault.block.entity.CrystalWorkbenchWorkspace;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.AugmentItem;
import iskallia.vault.item.SigilItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import xyz.iwolfking.woldsvaults.items.lib.IVaultCrystalModifier;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = CrystalWorkbenchWorkspace.Socket.class, remap = false)
public class MixinCrystalWorkbenchWorkspace$Socket {
    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("target")
    private static CrystalWorkbenchWorkspace.Socket[] $VALUES;

    @Final
    @Shadow
    public static CrystalWorkbenchWorkspace.Socket SEAL;
    @Final
    @Shadow
    public static CrystalWorkbenchWorkspace.Socket AUGMENT;

    @Final
    @Shadow
    public static CrystalWorkbenchWorkspace.Socket OBJECTIVE_MODIFIER;

    @Final
    @Shadow
    public static CrystalWorkbenchWorkspace.Socket CAPSTONE;

    @Final
    @Shadow
    public static CrystalWorkbenchWorkspace.Socket SIGIL;

    @Unique
    private static final CrystalWorkbenchWorkspace.Socket MISC = enumExpansion$addVariant("MISC", "Other Modifiers", "???");

    @Invoker("<init>")
    public static CrystalWorkbenchWorkspace.Socket enumExpansion$invokeInit(String internalName, int internalId, String displayName, String sets) {
        throw new AssertionError();
    }

    @Unique
    private static CrystalWorkbenchWorkspace.Socket enumExpansion$addVariant(String internalName, String displayName, String sets) {
        ArrayList<CrystalWorkbenchWorkspace.Socket> variants = new ArrayList<>(Arrays.asList(MixinCrystalWorkbenchWorkspace$Socket.$VALUES));
        CrystalWorkbenchWorkspace.Socket type = enumExpansion$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, displayName, sets);
        variants.add(type);
        MixinCrystalWorkbenchWorkspace$Socket.$VALUES = variants.toArray(new CrystalWorkbenchWorkspace.Socket[0]);
        return type;
    }

    /**
     * @author iwolfking
     * @reason Replace accepts check to handle newly added slot handling
     */
    @Overwrite
    public boolean accepts(ItemStack stack) {
        boolean accepts;
        CrystalWorkbenchWorkspace.Socket socket = (CrystalWorkbenchWorkspace.Socket) (Object)this;
        if (socket.equals(SEAL)) {
            accepts = stack.is(ModItems.CRYSTAL_SEALS_TAG);
        } else if (socket.equals(AUGMENT)) {
            accepts = stack.getItem() instanceof AugmentItem && !AugmentItem.isLegacy(stack);
        } else if (socket.equals(OBJECTIVE_MODIFIER)) {
            accepts = stack.is(ModItems.CRYSTAL_OBJECTIVE_MODIFIERS_TAG);
        } else if (socket.equals(CAPSTONE)) {
            accepts = stack.is(ModItems.CRYSTAL_CAPSTONES_TAG);
        } else if (socket.equals(SIGIL)) {
            accepts = stack.is(ModItems.SIGIL) && SigilItem.isPopulated(stack);
        }
        else if (socket.equals(MISC)) {
            accepts = stack.getItem() instanceof IVaultCrystalModifier;
        }
        else {
            throw new IncompatibleClassChangeError();
        }

        return accepts;
    }
}