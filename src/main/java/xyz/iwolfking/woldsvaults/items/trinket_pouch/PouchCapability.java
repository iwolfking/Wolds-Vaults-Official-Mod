package xyz.iwolfking.woldsvaults.items.trinket_pouch;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.api.util.PouchHelper;

public final class PouchCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<PouchContents> TYPE = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation ID = WoldsVaults.id("trinket_pouch");
    private final PouchContents contents = new PouchContents();
    private final LazyOptional<PouchContents> optional = LazyOptional.of(() -> contents);

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PouchContents.class);
    }

    public static void attach(AttachCapabilitiesEvent<ItemStack> event) {
        if (PouchHelper.isPouch(event.getObject())) {
            PouchCapability provider = new PouchCapability();
            event.addCapability(ID, provider);
            event.addListener(provider.optional::invalidate);
        }
    }

    public static PouchContents get(ItemStack pouch) {
        return pouch.getCapability(TYPE).orElseThrow(() -> new IllegalStateException("Missing trinket pouch capability on " + pouch.getItem()));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction direction) {
        return capability == TYPE ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return contents.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        contents.deserializeNBT(tag);
    }
}
