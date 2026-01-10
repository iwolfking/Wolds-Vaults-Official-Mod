package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.block.PlaceholderBlock;
import iskallia.vault.block.VaultGlobeBlock;
import iskallia.vault.command.GiveLootCommand;
import iskallia.vault.config.CatalystConfig;
import iskallia.vault.config.InscriptionConfig;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.random.RandomSource;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.core.vault.influence.VaultGod;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.BoosterPackItem;
import iskallia.vault.item.CardDeckItem;
import iskallia.vault.item.InfusedCatalystItem;
import iskallia.vault.item.InscriptionItem;
import iskallia.vault.item.data.InscriptionData;
import iskallia.vault.util.SidedHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.vhapi.mixin.accessors.CatalystConfigEntryAccessor;
import xyz.iwolfking.vhapi.mixin.accessors.CatalystConfigPoolAccessor;
import xyz.iwolfking.vhapi.mixin.accessors.InscriptionConfigEntryAccessor;
import xyz.iwolfking.vhapi.mixin.accessors.InscriptionConfigPoolAccessor;
import xyz.iwolfking.woldsvaults.items.*;

import java.util.Arrays;

@Mixin(value = GiveLootCommand.class, remap = false)
public class MixinGiveLootCommand {
    @Inject(method = "build", at = @At("TAIL"))
    private void wv$addExtraLootCommands(
            LiteralArgumentBuilder<CommandSourceStack> builder,
            CallbackInfo ci
    ) {
        builder.then(
                Commands.literal("booster_pack")
                        .then(
                                Commands.argument("id", ResourceLocationArgument.id())
                                        .suggests((ctx, sb) -> {
                                            ModConfigs.BOOSTER_PACK.getIds().forEach(sb::suggest);
                                            return sb.buildFuture();
                                        })
                                        .executes(this::woldsVaults$giveBoosterPack)
                        )
        );

        builder.then(
                Commands.literal("card_deck")
                        .then(
                                Commands.argument("id", StringArgumentType.word())
                                        .suggests((ctx, sb) -> {
                                            ModConfigs.CARD_DECK.getIds().forEach(sb::suggest);
                                            return sb.buildFuture();
                                        })
                                        .executes(this::woldsVaults$giveCardDeck)
                        )
        );

        builder.then(
                Commands.literal("trinket_pouch")
                        .then(
                                Commands.argument("id", ResourceLocationArgument.id())
                                        .suggests((ctx, sb) -> {
                                            xyz.iwolfking.woldsvaults.init.ModConfigs.TRINKET_POUCH
                                                    .TRINKET_POUCH_CONFIGS
                                                    .keySet()
                                                    .forEach(id -> sb.suggest(id.toString()));
                                            return sb.buildFuture();
                                        })
                                        .executes(this::woldsVaults$giveTrinketPouch)
                        )
        );

        builder.then(
                Commands.literal("recipe_blueprint")
                        .then(
                                Commands.argument("id", ResourceLocationArgument.id())
                                        .suggests((ctx, sb) -> {
                                            xyz.iwolfking.woldsvaults.init.ModConfigs.RECIPE_UNLOCKS
                                                    .RECIPE_UNLOCKS
                                                    .keySet()
                                                    .forEach(id -> sb.suggest(id.toString()));
                                            return sb.buildFuture();
                                        })
                                        .executes(this::woldsVaults$giveRecipeBlueprint)
                        )
        );

        builder.then(
                Commands.literal("research_token")
                        .then(
                                Commands.argument("research", StringArgumentType.string())
                                        .suggests((ctx, sb) -> {
                                            ModConfigs.RESEARCHES.getAll().forEach(research -> sb.suggest(research.getName()));
                                            return sb.buildFuture();
                                        })
                                        .executes(this::woldsVaults$giveResearchToken)
                        )
        );

        builder.then(
                Commands.literal("vault_globe")
                        .then(
                                Commands.argument("type", StringArgumentType.string())
                                        .suggests((ctx, sb) -> {
                                            Arrays.stream(VaultGlobeBlock.VaultGlobeType.values()).forEach(type -> sb.suggest(type.name()));
                                            return sb.buildFuture();
                                        })
                                        .executes(this::woldsVaults$giveVaultGlobe)
                        )
        );

        builder.then(
                Commands.literal("placeholder")
                        .then(
                                Commands.argument("type", StringArgumentType.string())
                                        .suggests((ctx, sb) -> {
                                            Arrays.stream(PlaceholderBlock.Type.values()).forEach(type -> sb.suggest(type.name()));
                                            return sb.buildFuture();
                                        })
                                        .executes(this::woldsVaults$givePlaceholder)
                        )
        );


        builder.then(
                Commands.literal("god_offering")
                        .then(
                                Commands.argument("god", StringArgumentType.string())
                                        .suggests((ctx, sb) -> {
                                            Arrays.stream(VaultGod.values()).forEach(type -> sb.suggest(type.name()));
                                            return sb.buildFuture();
                                        })
                                        .executes(this::woldsVaults$giveGodOffering)
                        )
        );

        builder.then(
                Commands.literal("chiseling_focus")
                        .then(
                                Commands.argument("modifier", StringArgumentType.word())
                                        .suggests((ctx, sb) -> {
                                            ToolModifierNullifyingItem.CHISELING_MODIFIER_TYPES.forEach(sb::suggest);
                                            return sb.buildFuture();
                                        })
                                        .executes(this::woldsVaults$giveChiselingFocus)
                        )
        );

        builder.then(
                Commands.literal("catalyst")
                        .then(
                                Commands.argument("pool", ResourceLocationArgument.id())
                                        .suggests((ctx, sb) -> {
                                            ModConfigs.CATALYST.pools.forEach((id, pools) ->
                                                    sb.suggest(id.toString())
                                            );
                                            return sb.buildFuture();
                                        })
                                        .then(
                                                Commands.argument("size", IntegerArgumentType.integer())
                                                        .executes(this::woldsVaults$giveCatalyst)
                                        )
                        )
        );

        builder.then(
                Commands.literal("inscription")
                        .then(
                                Commands.argument("pool", ResourceLocationArgument.id())
                                        .suggests((ctx, sb) -> {
                                            ModConfigs.INSCRIPTION.pools.forEach((id, pools) ->
                                                    sb.suggest(id.toString())
                                            );
                                            return sb.buildFuture();
                                        })
                                        .then(
                                                Commands.argument("size", IntegerArgumentType.integer())
                                                        .executes(this::woldsVaults$giveInscription)
                                        )
                        )
        );

    }


    @Unique
    private int woldsVaults$giveBoosterPack(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ItemStack boosterPack = new ItemStack(ModItems.BOOSTER_PACK);
        BoosterPackItem.setId(boosterPack, ResourceLocationArgument.getId(context, "id").toString());
        woldsvaults$giveStack(player, boosterPack);
        return 1;
    }

    @Unique
    private int woldsVaults$giveCardDeck(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ItemStack cardDeck = new ItemStack(ModItems.CARD_DECK);
        CardDeckItem.setId(cardDeck, StringArgumentType.getString(context, "id"));
        woldsvaults$giveStack(player, cardDeck);
        return 1;
    }

    @Unique
    private int woldsVaults$giveTrinketPouch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ItemStack trinketPouch =  TrinketPouchItem.create(ResourceLocationArgument.getId(context, "id"));
        woldsvaults$giveStack(player, trinketPouch);
        return 1;
    }

    @Unique
    private int woldsVaults$giveRecipeBlueprint(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ItemStack blueprint =  RecipeBlueprintItem.create(ResourceLocationArgument.getId(context, "id").toString());
        woldsvaults$giveStack(player, blueprint);
        return 1;
    }


    @Unique
    private int woldsVaults$giveResearchToken(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ItemStack researchToken =  ResearchTokenItem.create(StringArgumentType.getString(context, "research"));
        woldsvaults$giveStack(player, researchToken);
        return 1;
    }

    @Unique
    private int woldsVaults$giveVaultGlobe(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ItemStack vaultGlobe = new ItemStack(ModBlocks.VAULT_GLOBE_BLOCK);

        CompoundTag tag = vaultGlobe.getOrCreateTag();

        CompoundTag stateTag = new CompoundTag();
        tag.put("BlockStateTag", stateTag);

        stateTag.putString(
                "type",
                StringArgumentType.getString(context, "type").toLowerCase()
        );


        woldsvaults$giveStack(player, vaultGlobe);
        return 1;
    }

    @Unique
    private int woldsVaults$givePlaceholder(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ItemStack placeholder = new ItemStack(ModBlocks.PLACEHOLDER);
        CompoundTag tag = placeholder.getOrCreateTag();

        CompoundTag stateTag = new CompoundTag();
        tag.put("BlockStateTag", stateTag);

        stateTag.putString(
                "type",
                StringArgumentType.getString(context, "type").toLowerCase()
        );

        tag.putString("type", StringArgumentType.getString(context, "type").toLowerCase());

        woldsvaults$giveStack(player, placeholder);
        return 1;
    }

    @Unique
    private int woldsVaults$giveGodOffering(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ItemStack godOffering =  GodReputationItem.create(StringArgumentType.getString(context, "god"));
        woldsvaults$giveStack(player, godOffering);
        return 1;
    }

    @Unique
    private int woldsVaults$giveChiselingFocus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ItemStack chiselingFocus =  ToolModifierNullifyingItem.create(StringArgumentType.getString(context, "modifier"));
        woldsvaults$giveStack(player, chiselingFocus);
        return 1;
    }

    @Unique
    private int woldsVaults$giveCatalyst(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ResourceLocation pool = ResourceLocationArgument.getId(context, "pool");
        CatalystConfig.Pool poolEntry = ModConfigs.CATALYST.pools.get(pool).getForLevel(SidedHelper.getVaultLevel(player)).orElse(null);
        if(poolEntry == null) {
            return 0;
        }
        WeightedList<CatalystConfig.Entry> entryList = ((CatalystConfigPoolAccessor)poolEntry).getPool();
        CatalystConfig.Entry entry = entryList.getRandom().orElse(null);
        if(entry == null) {
            return 0;
        }

        ItemStack catalyst =  InfusedCatalystItem.create(IntegerArgumentType.getInteger(context, "size"), ((CatalystConfigEntryAccessor)entry).getModifiers());

        woldsvaults$giveStack(player, catalyst);
        return 1;
    }

    @Unique
    private int woldsVaults$giveInscription(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = ((CommandSourceStack)context.getSource()).getPlayerOrException();
        RandomSource random = JavaRandom.ofNanoTime();
        ResourceLocation pool = ResourceLocationArgument.getId(context, "pool");
        InscriptionConfig.Pool poolEntry = ModConfigs.INSCRIPTION.pools.get(pool).getForLevel(SidedHelper.getVaultLevel(player)).orElse(null);
        if(poolEntry == null) {
            return 0;
        }
        WeightedList<InscriptionConfig.Entry> entryList = ((InscriptionConfigPoolAccessor)poolEntry).getPool();
        InscriptionConfig.Entry entry = entryList.getRandom().orElse(null);
        if(entry == null) {
            return 0;
        }

        ItemStack inscription = new ItemStack(ModItems.INSCRIPTION);

        InscriptionData inscriptionData = InscriptionData.from(inscription);
        ((InscriptionConfigEntryAccessor)entry).getEntries().forEach(inscriptionData::add);
        inscriptionData.setSize(IntegerArgumentType.getInteger(context, "size"));
        inscriptionData.setColor(((InscriptionConfigEntryAccessor) entry).getColor());
        inscriptionData.setModel(((InscriptionConfigEntryAccessor) entry).getModel().getMin());
        inscriptionData.write(inscription);

        woldsvaults$giveStack(player, inscription);
        return 1;
    }




    @Unique
    private void woldsvaults$giveStack(Player player, ItemStack stack) {
        ItemHandlerHelper.giveItemToPlayer(player, stack);
        player.displayClientMessage(new TranslatableComponent("command.woldsvaults.give_loot", stack.getDisplayName()), false);
    }
}
