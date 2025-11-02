package xyz.iwolfking.woldsvaults.gui.server;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xyz.iwolfking.woldsvaults.api.util.MessageFunctions;

import javax.annotation.Nullable;

public class ClaimBlockCategoryGui extends SimpleGui {
    private int ticker = 0;
    ClaimBlockCategoryGui.DisplayElement EMPTY = ClaimBlockCategoryGui.DisplayElement.of(new GuiElement(ItemStack.EMPTY, GuiElementInterface.EMPTY_CALLBACK));
    ClaimBlockCategoryGui.DisplayElement CLAIM_BLOCK_PACKAGE_1 = ClaimBlockCategoryGui.DisplayElement.of(new GuiElementBuilder(Items.COAL_BLOCK)
            .setName(new TextComponent("1 Additional Chunk").withStyle(ChatFormatting.WHITE))
            .addLoreLine(new TextComponent("Grants you an additional chunk claim!").withStyle(ChatFormatting.YELLOW))
            .addLoreLine(new TextComponent("Price: 2 Platinum").withStyle(ChatFormatting.GOLD))
            .addLoreLine(new TextComponent("Have the money in your wallet to pay.").withStyle(ChatFormatting.GREEN))
            .hideFlags()
            .setCallback((x,y,z) -> {
                playClickSound(this.player);
                try {
                    tryBuyClaim(this.player, 1, CoinValue.fromNumber(1458L));
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }));

    ClaimBlockCategoryGui.DisplayElement CLAIM_BLOCK_PACKAGE_2 = ClaimBlockCategoryGui.DisplayElement.of(new GuiElementBuilder(Items.IRON_BLOCK)
            .setName(new TextComponent("5 Additional Chunks").withStyle(ChatFormatting.GREEN))
            .addLoreLine(new TextComponent("Grants you 5 additional chunk claims!").withStyle(ChatFormatting.YELLOW))
            .addLoreLine(new TextComponent("Price: 10 Platinum").withStyle(ChatFormatting.GOLD))
            .addLoreLine(new TextComponent("Have the money in your wallet to pay.").withStyle(ChatFormatting.GREEN))
            .hideFlags()
            .setCallback((x,y,z) -> {
                playClickSound(this.player);
                try {
                    tryBuyClaim(this.player, 5, CoinValue.fromNumber(7290L));
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }));

    ClaimBlockCategoryGui.DisplayElement CLAIM_BLOCK_PACKAGE_3 = ClaimBlockCategoryGui.DisplayElement.of(new GuiElementBuilder(Items.GOLD_BLOCK)
            .setName(new TextComponent("10 Additional Chunks").withStyle(ChatFormatting.GREEN))
            .addLoreLine(new TextComponent("Grants you 10 additional chunk claims!").withStyle(ChatFormatting.YELLOW))
            .addLoreLine(new TextComponent("Price: 20 Platinum").withStyle(ChatFormatting.GOLD))
            .addLoreLine(new TextComponent("Have the money in your wallet to pay.").withStyle(ChatFormatting.GREEN))
            .hideFlags()
            .setCallback((x,y,z) -> {
                playClickSound(this.player);
                try {
                    tryBuyClaim(this.player, 10, CoinValue.fromNumber(14580L));
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }));
    ClaimBlockCategoryGui.DisplayElement CLAIM_BLOCK_PACKAGE_4 = ClaimBlockCategoryGui.DisplayElement.of(new GuiElementBuilder(Items.DIAMOND_BLOCK)
            .setName(new TextComponent("25 Additional Chunks").withStyle(ChatFormatting.GREEN))
            .addLoreLine(new TextComponent("Grants you 25 additional chunk claims!").withStyle(ChatFormatting.YELLOW))
            .addLoreLine(new TextComponent("Price: 50 Platinum").withStyle(ChatFormatting.GOLD))
            .addLoreLine(new TextComponent("Have the money in your wallet to pay.").withStyle(ChatFormatting.GREEN))
            .hideFlags()
            .setCallback((x,y,z) -> {
                playClickSound(this.player);
                try {
                    tryBuyClaim(this.player, 25, CoinValue.fromNumber(36450L));
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }));

    ClaimBlockCategoryGui.DisplayElement CLAIM_BLOCK_PACKAGE_5 = ClaimBlockCategoryGui.DisplayElement.of(new GuiElementBuilder(Items.NETHERITE_BLOCK)
            .setName(new TextComponent("50 Additional Chunks").withStyle(ChatFormatting.GREEN))
            .addLoreLine(new TextComponent("Grants you 50 additional chunk claims!").withStyle(ChatFormatting.YELLOW))
            .addLoreLine(new TextComponent("Price: 1 Iridium").withStyle(ChatFormatting.GOLD))
            .addLoreLine(new TextComponent("Have the money in your wallet to pay.").withStyle(ChatFormatting.GREEN))
            .hideFlags()
            .setCallback((x,y,z) -> {
                playClickSound(this.player);
                try {
                    tryBuyClaim(this.player, 50, CoinValue.fromNumber(59049L));
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }));

    public ClaimBlockCategoryGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x1, player, false);
    }

    private static void playClickSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 1, 1);
    }
    public void updateDisplay() {
        this.setSlot(0,  DisplayElement.FILLER.element());
        this.setSlot(1,  DisplayElement.FILLER.element());
        this.setSlot(2, CLAIM_BLOCK_PACKAGE_1.element);
        this.setSlot(3, CLAIM_BLOCK_PACKAGE_2.element);
        this.setSlot(4, CLAIM_BLOCK_PACKAGE_3.element);
        this.setSlot(5, CLAIM_BLOCK_PACKAGE_4.element);
        this.setSlot(6, CLAIM_BLOCK_PACKAGE_5.element);
        this.setSlot(7,  DisplayElement.FILLER.element());
        this.setSlot(8,  DisplayElement.FILLER.element());

    }


    private static void tryBuyClaim(ServerPlayer player, int count, CoinValue price) throws CommandSyntaxException {
            if (!MoneyUtil.ProcessPayment(null, player, price)) {
                MessageFunctions.sendMessage(player, new TextComponent("You don't have enough money to buy that!").withStyle(ChatFormatting.RED));
            }
            else {
                IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
                        serverData = ServerData.from(player.getServer());
                IPlayerConfigManager
                        configManager = serverData.getPlayerConfigs();
                IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>
                        claimsManager = serverData.getServerClaimsManager();
                GameProfile profile = player.getGameProfile();
                IPlayerConfig config = configManager.getLoadedConfig(profile.getId());
                int bonusChunks = config.getEffective(PlayerConfigOptions.BONUS_CHUNK_CLAIMS);
                config.tryToSet(PlayerConfigOptions.BONUS_CHUNK_CLAIMS, bonusChunks + count);
            }
        }




    @Override
    public void onTick() {
        ticker++;
        if (ticker >= 20) {
            ticker = 0;
            updateDisplay();
        }

        super.onTick();
    }

    public record DisplayElement(@Nullable GuiElementInterface element, @Nullable Slot slot) {
        private static final ClaimBlockCategoryGui.DisplayElement EMPTY = ClaimBlockCategoryGui.DisplayElement.of(new GuiElement(ItemStack.EMPTY, GuiElementInterface.EMPTY_CALLBACK));
        private static final ClaimBlockCategoryGui.DisplayElement FILLER = ClaimBlockCategoryGui.DisplayElement.of(
                new GuiElementBuilder(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setName(new TextComponent(""))
                        .hideFlags()
        );

        public static ClaimBlockCategoryGui.DisplayElement of(GuiElementInterface element) {
            return new ClaimBlockCategoryGui.DisplayElement(element, null);
        }

        public static ClaimBlockCategoryGui.DisplayElement of(GuiElementBuilderInterface<?> element) {
            return new ClaimBlockCategoryGui.DisplayElement(element.build(), null);
        }


        public static ClaimBlockCategoryGui.DisplayElement filler() {
            return FILLER;
        }

        public static ClaimBlockCategoryGui.DisplayElement empty() {
            return EMPTY;
        }
    }
}
