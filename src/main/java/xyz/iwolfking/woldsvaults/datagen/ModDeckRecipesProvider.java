package xyz.iwolfking.woldsvaults.datagen;

import iskallia.vault.VaultMod;
import iskallia.vault.core.vault.player.Completion;
import iskallia.vault.core.world.data.entity.*;
import iskallia.vault.core.world.data.tile.PartialBlockState;
import iskallia.vault.core.world.data.tile.PartialTile;
import iskallia.vault.core.world.data.tile.TilePredicate;
import iskallia.vault.core.world.roll.FloatRoll;
import iskallia.vault.core.world.roll.IntRoll;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import iskallia.vault.task.*;
import iskallia.vault.task.counter.TargetTaskCounter;
import iskallia.vault.task.counter.TaskCounterPredicate;
import iskallia.vault.task.renderer.NamedTaskRenderer;
import iskallia.vault.task.util.DamagePhase;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import xyz.iwolfking.vhapi.api.datagen.AbstractTooltipProvider;
import xyz.iwolfking.vhapi.api.datagen.recipes.AbstractDeckRecipesProvider;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.init.ModEntities;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ModDeckRecipesProvider extends AbstractDeckRecipesProvider {
    protected ModDeckRecipesProvider(DataGenerator generator) {
        super(generator, WoldsVaults.MOD_ID);
    }

    @Override
    public void registerConfigs() {
        LootChestTask fairyTask = new LootChestTask(new LootChestTask.Config(PartialBlockState.of(ModBlocks.LIVING_STRONGBOX)), TargetTaskCounter.ofTargetInt(IntRoll.ofConstant(500), TaskCounterPredicate.GREATER_OR_EQUAL_TO));
        KillEntityTask woldTask = new KillEntityTask(new KillEntityTask.Config(PartialEntityGroup.of(VaultMod.id("elite"), PartialCompoundNbt.empty())), TargetTaskCounter.ofTargetInt(IntRoll.ofConstant(150), TaskCounterPredicate.GREATER_OR_EQUAL_TO));
        FindVaultRoomTask snakeTask = new FindVaultRoomTask(new FindVaultRoomTask.Config(new ResourceLocation[]{VaultMod.id("vault/rooms/challenge/dragon1"), VaultMod.id("vault/rooms/challenge/dragon2")}), TargetTaskCounter.ofTargetInt(IntRoll.ofConstant(10), TaskCounterPredicate.GREATER_OR_EQUAL_TO));
        FinishVaultTask crateTask = new FinishVaultTask(new FinishVaultTask.Config(Set.of(Completion.COMPLETED), new LinkedHashSet<>(List.of("elixir", "bingo", "monolith", "scavenger", "rune_boss", "alchemy", "enchanted_elixir", "brutal_bosses", "haunted_braziers", "ballistic_bingo"))), TargetTaskCounter.ofTargetInt(IntRoll.ofConstant(100), TaskCounterPredicate.GREATER_OR_EQUAL_TO));
        TakeDamageTask wallTask = new TakeDamageTask(new TakeDamageTask.Config(PartialEntityGroup.of(VaultMod.id("horde"), PartialCompoundNbt.empty()), DamagePhase.PRE_MITIGATION), TargetTaskCounter.ofTargetFloat(FloatRoll.ofConstant(25000F), TaskCounterPredicate.GREATER_OR_EQUAL_TO));

        add("wolds_decks", builder -> {
            builder.addRecipe(WoldsVaults.id("fairy_deck"), "fairy", fairyTask.setRenderer(new NamedTaskRenderer(new TextComponent("Loot Living Strongboxes"))).setId("fairy_deck_task"), "advanced", list -> {
                list.add(new ItemStack(ModItems.OMEGA_POG, 1));
                list.add(new ItemStack(ModItems.EXTRAORDINARY_BLACK_OPAL, 16));
                list.add(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PRISMATIC_FIBER, 1));
                list.add(new ItemStack(ModBlocks.VAULT_PLATINUM, 16));
            });

            builder.addRecipe(WoldsVaults.id("snake_deck"), "snake", snakeTask.setRenderer(new NamedTaskRenderer(new TextComponent("Discover Dragon Rooms"))).setId("snake_deck_task"), "advanced", list -> {
                list.add(new ItemStack(ModItems.OMEGA_POG, 1));
                list.add(new ItemStack(ModItems.EXTRAORDINARY_BLACK_OPAL, 16));
                list.add(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PRISMATIC_FIBER, 1));
                list.add(new ItemStack(ModBlocks.VAULT_PLATINUM, 16));
            });

            builder.addRecipe(WoldsVaults.id("crate_deck"), "crate", crateTask.setRenderer(new NamedTaskRenderer(new TextComponent("Complete Vaults"))).setId("crate_deck_task"), "advanced", list -> {
                list.add(new ItemStack(ModItems.OMEGA_POG, 1));
                list.add(new ItemStack(ModItems.EXTRAORDINARY_BLACK_OPAL, 16));
                list.add(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PRISMATIC_FIBER, 1));
                list.add(new ItemStack(ModBlocks.VAULT_PLATINUM, 16));
            });

            builder.addRecipe(WoldsVaults.id("wall_deck"), "wall", wallTask.setRenderer(new NamedTaskRenderer(new TextComponent("Take Damage From Horde Mobs"))).setId("wall_deck_task"), "advanced", list -> {
                list.add(new ItemStack(ModItems.OMEGA_POG, 1));
                list.add(new ItemStack(ModItems.EXTRAORDINARY_BLACK_OPAL, 16));
                list.add(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PRISMATIC_FIBER, 1));
                list.add(new ItemStack(ModBlocks.VAULT_PLATINUM, 16));
            });


            builder.addRecipe(WoldsVaults.id("wold_deck"), "wold", woldTask.setRenderer(new NamedTaskRenderer(new TextComponent("Kill Elite Mobs"))).setId("wold_deck_task"), "advanced", list -> {
                list.add(new ItemStack(ModItems.ECHO_POG, 1));
                list.add(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.WOLD_STAR_CHUNK, 1));
                list.add(new ItemStack(xyz.iwolfking.woldsvaults.init.ModItems.PRISMATIC_FIBER, 8));
                list.add(new ItemStack(ModBlocks.VAULT_PLATINUM, 32));
            });
        });
    }
}
