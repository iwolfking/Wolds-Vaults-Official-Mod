package xyz.iwolfking.woldsvaults.api.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Predicate;

public class GearSpecificModifierReader<T> extends VaultGearModifierReader<T> {
    public GearSpecificModifierReader(Predicate<ItemStack> itemStackPredicate, VaultGearModifierReader<T> original, VaultGearModifierReader<T> alternate) {
        super(null,0);
        this.itemStackPredicate = itemStackPredicate;
        this.original = original;
        this.alternate = alternate;
    }

    private final Predicate<ItemStack> itemStackPredicate;
    private final VaultGearModifierReader<T> original;
    private final VaultGearModifierReader<T> alternate;

    @Override
    public @Nullable MutableComponent getValueDisplay(T t) {
        return original.getValueDisplay(t);
    }

    @Nullable
    public MutableComponent getValueDisplay(T t, ItemStack stack) {
        if(itemStackPredicate.test(stack)) {
            return alternate.getValueDisplay(t);
        }

        return original.getValueDisplay(t);
    }

    @Override
    public String getModifierName() {
        return original.getModifierName();
    }

    @Override
    public int getRgbColor() {
        return original.getRgbColor();
    }

    @Override
    public Style getColoredTextStyle() {
        return original.getColoredTextStyle();
    }
    protected void serializeTextElements(JsonArray out, VaultGearAttributeInstance<T> instance, VaultGearModifier.AffixType type) {}

    @Nullable
    public MutableComponent getDisplay(VaultGearAttributeInstance<T> vaultGearAttributeInstance, VaultGearModifier.AffixType affixType) {
        return original.getDisplay(vaultGearAttributeInstance, affixType);
    }

    @Nullable
    public MutableComponent getDisplay(VaultGearAttributeInstance<T> vaultGearAttributeInstance, VaultGearModifier.AffixType affixType, ItemStack stack) {
        if(itemStackPredicate.test(stack)) {
            return alternate.getDisplay(vaultGearAttributeInstance, affixType);
        }

        return original.getDisplay(vaultGearAttributeInstance, affixType);
    }

    @NotNull
    @Override
    public JsonObject serializeDisplay(VaultGearAttributeInstance<T> instance, VaultGearModifier.AffixType type) {
        return original.serializeDisplay(instance, type);
    }

    @Override
    public MutableComponent formatConfigDisplay(LogicalSide side, Component configRange) {
        return original.formatConfigDisplay(side, configRange);
    }

    public MutableComponent getDisplay(VaultGearAttributeInstance<T> instance, VaultGearData data, VaultGearModifier.AffixType type, ItemStack stack) {
        return this.getDisplay(instance, type, stack);
    }

    public MutableComponent getDisplay(VaultGearAttributeInstance<T> instance, VaultGearData data, VaultGearModifier.AffixType type, ItemStack stack, boolean displayDetail) {
        MutableComponent component = this.getDisplay(instance, type, stack);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.modifyColourOnClient(component));
        return component;
    }
}
