package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.layout.ArchitectCrystalLayout;
import iskallia.vault.item.crystal.properties.CapacityCrystalProperties;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.loadingzone.PocketData;
import xyz.iwolfking.woldsvaults.loadingzone.PocketTemplates;
import xyz.iwolfking.woldsvaults.loadingzone.Pockets;

import java.util.List;
import java.util.Optional;

public class PocketTestCommand {
    private static final String TEST_ROOM = WoldsVaults.id("vault/rooms/test/pocket_hub").toString();
    private static final String MYTHIC_ROOM_PREFIX = "vault/rooms/mythic/";
    private static final int MYTHIC_FILLER_ROOMS = 60;
    private static final List<String> MYTHIC_ROOMS = List.of("magical_forest", "fortress", "babel", "dragon_den", "inverted_pyramid",
            "inverted_pyramid_ruins", "grand_canyon", "hellscape");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wvpockets")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("crystal")
                        .executes(context -> giveCrystal(context, 100))
                        .then(Commands.argument("rooms", IntegerArgumentType.integer(1, 500))
                                .executes(context -> giveCrystal(context, IntegerArgumentType.getInteger(context, "rooms")))))
                .then(Commands.literal("mythic")
                        .then(Commands.argument("room", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(MYTHIC_ROOMS, builder))
                                .executes(PocketTestCommand::giveMythicCrystal)))
                .then(Commands.literal("info").executes(PocketTestCommand::info)));
    }

    private static int giveCrystal(CommandContext<CommandSourceStack> context, int rooms) throws CommandSyntaxException {
        ListTag entries = new ListTag();
        entries.add(layoutEntry(TEST_ROOM, rooms, 0x55FFFF));
        give(context.getSource().getPlayerOrException(), entries, "Pocket Test Crystal (" + rooms + " rooms)");
        context.getSource().sendSuccess(new TextComponent("Gave a crystal for a vault of " + rooms + " pocket test rooms"), false);
        return 1;
    }

    /** A crystal whose vault holds one copy of the named mythic room among ordinary test rooms it can fit between. */
    private static int giveMythicCrystal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String room = StringArgumentType.getString(context, "room");
        if (!MYTHIC_ROOMS.contains(room)) {
            context.getSource().sendFailure(new TextComponent("Unknown mythic room " + room + "; one of " + String.join(", ", MYTHIC_ROOMS)));
            return 0;
        }
        ListTag entries = new ListTag();
        entries.add(layoutEntry(WoldsVaults.id(MYTHIC_ROOM_PREFIX + room).toString(), 1, 0xFFAA00));
        entries.add(layoutEntry(TEST_ROOM, MYTHIC_FILLER_ROOMS, 0x55FFFF));
        give(context.getSource().getPlayerOrException(), entries, "Mythic Room Test Crystal (" + room + ")");
        context.getSource().sendSuccess(new TextComponent("Gave a crystal for a vault with the mythic room " + room), false);
        return 1;
    }

    private static CompoundTag layoutEntry(String pool, int count, int color) {
        CompoundTag entry = new CompoundTag();
        entry.putString("pool", pool);
        entry.putInt("count", count);
        entry.putInt("color", color);
        return entry;
    }

    /** Gives an architect crystal listing exactly these rooms; completion 1 keeps every block of them. */
    private static void give(ServerPlayer player, ListTag entries, String name) {
        CompoundTag layoutTag = new CompoundTag();
        layoutTag.putInt("tunnel_span", 1);
        layoutTag.put("entries", entries);
        layoutTag.putFloat("completion", 1.0F);
        ArchitectCrystalLayout layout = new ArchitectCrystalLayout();
        layout.readNbt(layoutTag);
        ItemStack crystal = VaultCrystalItem.create(data -> {
            data.setLayout(layout);
            data.setProperties(new CapacityCrystalProperties());
        });
        crystal.setHoverName(new TextComponent(name));
        if (!player.getInventory().add(crystal)) {
            player.drop(crystal, false);
        }
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PocketData data = PocketData.get(player.server);
        int open = data.vaults().values().stream().mapToInt(pockets -> pockets.pockets().size()).sum();
        Optional<Vault> vault = ServerVaults.get(player.getLevel());
        String here = vault.map(current -> {
            PocketData.VaultPockets pockets = data.vaults().get(current.get(Vault.ID));
            if (pockets == null) {
                return "This vault has no pockets open yet";
            }
            long scanned = pockets.pockets().stream().filter(pocket -> pocket.scan() != null).count();
            return "This vault: " + pockets.pockets().size() + " pockets open (" + scanned + " scanned), " + pockets.returns() + " return points, clock "
                    + (Pockets.holdsClock(current) ? "held" : "running");
        }).orElse("Not in a vault");
        context.getSource().sendSuccess(new TextComponent(here), false);
        context.getSource().sendSuccess(new TextComponent("All vaults: " + open + " pockets open in " + data.vaults().size() + " vaults, " + PocketTemplates.builtCount() + " builds in memory"), false);
        return 1;
    }
}
