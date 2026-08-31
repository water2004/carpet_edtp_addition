package org.edtp.carpet_edtp_addition.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.permissions.PermissionSet;
import org.edtp.carpet_edtp_addition.CarpetEdtpAdditionSettings;

public class TickCommandForAllGameTests {
    @GameTest
    public void nonOpCanUseTickCommandOnlyWhenRuleIsEnabled(GameTestHelper helper) {
        CommandDispatcher<CommandSourceStack> dispatcher = helper.getLevel().getServer().getCommands().getDispatcher();
        CommandNode<CommandSourceStack> tickCommand = dispatcher.getRoot().getChild("tick");
        CommandSourceStack nonOpSource = helper.getLevel().getServer().createCommandSourceStack()
            .withPermission(PermissionSet.NO_PERMISSIONS);

        helper.assertTrue(tickCommand != null, "Vanilla tick command is not registered");

        try {
            CarpetEdtpAdditionSettings.tickCommandForAll.set(null, false);
            helper.assertFalse(tickCommand.canUse(nonOpSource), "Non-op could use /tick while the rule was disabled");
            helper.assertTrue(commandIsRejected(dispatcher, nonOpSource), "/tick query executed while the rule was disabled");

            CarpetEdtpAdditionSettings.tickCommandForAll.set(null, true);
            helper.assertTrue(tickCommand.canUse(nonOpSource), "Non-op could not use /tick while the rule was enabled");
            helper.assertFalse(commandIsRejected(dispatcher, nonOpSource), "/tick query was rejected while the rule was enabled");
            helper.succeed();
        } finally {
            CarpetEdtpAdditionSettings.tickCommandForAll.set(null, false);
        }
    }

    @GameTest
    public void ruleChangeRefreshesConnectedPlayersCommandTree(GameTestHelper helper) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
            new GameProfile(UUID.randomUUID(), "tick-command-test"),
            false
        );
        ServerPlayer player = new ServerPlayer(
            helper.getLevel().getServer(),
            helper.getLevel(),
            cookie.gameProfile(),
            cookie.clientInformation()
        );
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);

        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        drainOutbound(channel);

        try {
            CarpetEdtpAdditionSettings.tickCommandForAll.set(player.createCommandSourceStack(), true);
            helper.assertTrue(
                drainOutbound(channel),
                "Enabling tickCommandForAll did not resend the command tree to a connected non-op"
            );

            CarpetEdtpAdditionSettings.tickCommandForAll.set(player.createCommandSourceStack(), false);
            helper.assertTrue(
                drainOutbound(channel),
                "Disabling tickCommandForAll did not resend the command tree to a connected non-op"
            );
            helper.succeed();
        } finally {
            CarpetEdtpAdditionSettings.tickCommandForAll.set(null, false);
            helper.getLevel().getServer().getPlayerList().remove(player);
            channel.close();
        }
    }

    private static boolean commandIsRejected(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandSourceStack source
    ) {
        try {
            dispatcher.execute("tick query", source);
            return false;
        } catch (CommandSyntaxException ignored) {
            return true;
        }
    }

    private static boolean drainOutbound(EmbeddedChannel channel) {
        boolean foundCommandTree = false;
        Object message;
        while ((message = channel.readOutbound()) != null) {
            foundCommandTree |= message instanceof ClientboundCommandsPacket;
        }
        return foundCommandTree;
    }
}
