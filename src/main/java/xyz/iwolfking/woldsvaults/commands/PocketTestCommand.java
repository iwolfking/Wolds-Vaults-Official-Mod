package xyz.iwolfking.woldsvaults.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.crystal.VaultCrystalItem;
import iskallia.vault.item.crystal.layout.ArchitectCrystalLayout;
import iskallia.vault.item.crystal.properties.CapacityCrystalProperties;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.woldsvaults.WoldsVaults;
import xyz.iwolfking.woldsvaults.loadingzone.PocketData;
import xyz.iwolfking.woldsvaults.loadingzone.PocketTemplates;

import java.util.Optional;

// Op-only tools for testing vault pockets: a crystal whose vault is nothing but the pocket test room, and a count of the
// pockets the current vault has opened.
public class PocketTestCommand {
    private static final String TEST_ROOM = WoldsVaults.id("vault/rooms/test/pocket_hub").toString();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wvpockets")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("crystal")
                        .executes(context -> giveCrystal(context, 100))
                        .then(Commands.argument("rooms", IntegerArgumentType.integer(1, 500))
                                .executes(context -> giveCrystal(context, IntegerArgumentType.getInteger(context, "rooms")))))
                .then(Commands.literal("info").executes(PocketTestCommand::info)));
    }

    // an architect layout lists exactly the rooms a vault gets; completion 1 keeps every block of them
    private static int giveCrystal(CommandContext<CommandSourceStack> context, int rooms) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        CompoundTag entry = new CompoundTag();
        entry.putString("pool", TEST_ROOM);
        entry.putInt("count", rooms);
        entry.putInt("color", 0x55FFFF);
        ListTag entries = new ListTag();
        entries.add(entry);
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
        crystal.setHoverName(new TextComponent("Pocket Test Crystal (" + rooms + " rooms)"));
        if (!player.getInventory().add(crystal)) {
            player.drop(crystal, false);
        }
        context.getSource().sendSuccess(new TextComponent("Gave a crystal for a vault of " + rooms + " pocket test rooms"), false);
        return 1;
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
                    + (pockets.clock() == null ? "running" : "held");
        }).orElse("Not in a vault");
        context.getSource().sendSuccess(new TextComponent(here), false);
        context.getSource().sendSuccess(new TextComponent("All vaults: " + open + " pockets open in " + data.vaults().size() + " vaults, " + PocketTemplates.builtCount() + " builds in memory"), false);
        return 1;
    }
}
