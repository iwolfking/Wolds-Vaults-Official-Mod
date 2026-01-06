package xyz.iwolfking.woldsvaults.api.vault.layout;

import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.WorldManager;
import iskallia.vault.core.world.generator.GridGenerator;

import iskallia.vault.item.crystal.layout.ClassicInfiniteCrystalLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;

public class ClassicStarCrystalLayout extends ClassicInfiniteCrystalLayout {

    private int arms;
    private int outerRadius;
    private int innerRadius;

    public ClassicStarCrystalLayout() {}

    public ClassicStarCrystalLayout(int tunnelSpan, int arms, int outerRadius, int innerRadius) {
        super(tunnelSpan);
        this.arms = arms;
        this.outerRadius = outerRadius;
        this.innerRadius = innerRadius;
    }

    @Override
    public void configure(Vault vault, RandomSource random, String sigil) {
        vault.getOptional(Vault.WORLD)
            .map(world -> world.get(WorldManager.GENERATOR))
            .ifPresent(generator -> {
                if (generator instanceof GridGenerator grid) {
                    grid.set(
                        GridGenerator.LAYOUT,
                        new ClassicStarLayout(tunnelSpan, arms, outerRadius, innerRadius)
                    );
                }
            });
    }

    @Override
    public void addText(List<Component> tooltip, int minIndex, TooltipFlag flag, float time, int level) {
        tooltip.add(
            new TextComponent("Layout: ")
                .append(new TextComponent("Star").withStyle(ChatFormatting.GOLD))
        );
    }

    @Override
    public Optional<CompoundTag> writeNbt() {
        return super.writeNbt().map(nbt -> {
            nbt.putInt("arms", arms);
            nbt.putInt("outer", outerRadius);
            nbt.putInt("inner", innerRadius);
            return nbt;
        });
    }

    @Override
    public void readNbt(CompoundTag nbt) {
        super.readNbt(nbt);
        arms = nbt.getInt("arms");
        outerRadius = nbt.getInt("outer");
        innerRadius = nbt.getInt("inner");
    }
}
