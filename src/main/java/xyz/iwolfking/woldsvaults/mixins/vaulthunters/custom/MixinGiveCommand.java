package xyz.iwolfking.woldsvaults.mixins.vaulthunters.custom;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.command.give.GiveCommand;
import iskallia.vault.init.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.iwolfking.woldsvaults.api.util.AncientUniqueHelper;
import xyz.iwolfking.woldsvaults.items.UnidentifiedAncientUniqueItem;

import javax.annotation.Nullable;

@Mixin(value = GiveCommand.class, remap = false)
public class MixinGiveCommand {

    /**
     * Hangs an optional {@code ancient} flag off the tail of the existing unique-giving command, so
     * {@code /the_vault give unique <name> <level> ancient} rolls a guaranteed ancient.
     *
     * <p>Brigadier merges nodes by name when they are added, so re-declaring the {@code unique} ->
     * {@code uniqueName} -> {@code level} chain here does not duplicate it: the existing nodes are
     * kept along with their suggestion providers and executor, and only the new {@code ancient} leaf
     * is grafted on. That keeps the plain command untouched.</p>
     */
    @Inject(method = "buildGearGiving", at = @At("TAIL"))
    private void woldsvaults$addAncientUniqueGiving(LiteralArgumentBuilder<CommandSourceStack> builder, CallbackInfo ci) {
        builder.then(Commands.literal("unique")
                .then(Commands.argument("uniqueName", (ArgumentType<?>) StringArgumentType.string())
                        .then(Commands.argument("level", (ArgumentType<?>) IntegerArgumentType.integer(0, 100))
                                .then(Commands.literal("ancient")
                                        .executes(MixinGiveCommand::woldsvaults$giveAncientUnique)))));
    }

    @Unique
    private static int woldsvaults$giveAncientUnique(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String uniqueName = StringArgumentType.getString(ctx, "uniqueName");
        int level = IntegerArgumentType.getInteger(ctx, "level");

        ResourceLocation uniqueKey = woldsvaults$resolveUniqueKey(uniqueName);
        if (uniqueKey == null) {
            player.sendMessage(new TextComponent("Unknown unique item: " + uniqueName).withStyle(ChatFormatting.RED), Util.NIL_UUID);
            return 0;
        }
        String ancientName = AncientUniqueHelper.getAncientName(uniqueKey).orElse(null);
        if (ancientName == null) {
            player.sendMessage(new TextComponent(uniqueKey + " has no ancient variant.").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            return 0;
        }

        ItemStack stack = UnidentifiedAncientUniqueItem.createAncientUnique(uniqueKey, player, level);
        if (stack.isEmpty()) {
            player.sendMessage(new TextComponent("Failed to create an ancient " + uniqueKey + "; see the log for why.").withStyle(ChatFormatting.RED), Util.NIL_UUID);
            return 0;
        }
        player.getInventory().add(stack);
        player.sendMessage(new TextComponent("Gave " + ancientName + " (Level " + level + ")").withStyle(ChatFormatting.GOLD), Util.NIL_UUID);
        return 1;
    }

    /**
     * Resolves a bare unique name against the unique registry rather than assuming the_vault
     * namespace. The stock command hardcodes {@code VaultMod.id(name)}, which cannot reach any of the
     * addon's own uniques even though its suggestion list offers them — several ancients that need
     * testing, Plague Steppers and Mineral Greatsword among them, live under woldsvaults.
     */
    @Unique
    @Nullable
    private static ResourceLocation woldsvaults$resolveUniqueKey(String uniqueName) {
        ResourceLocation exact = ResourceLocation.tryParse(uniqueName);
        if (exact != null && ModConfigs.UNIQUE_GEAR.getRegistry().containsKey(exact)) {
            return exact;
        }
        for (ResourceLocation key : ModConfigs.UNIQUE_GEAR.getRegistry().keySet()) {
            if (key.getPath().equals(uniqueName)) {
                return key;
            }
        }
        return null;
    }
}
