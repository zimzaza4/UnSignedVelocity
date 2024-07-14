package io.github._4drian3d.unsignedvelocity.listener.packet.command;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.chat.CommandHandler;
import com.velocitypowered.proxy.protocol.packet.chat.LastSeenMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface SimpleCommandHandler {

    Logger logger = LogManager.getLogger(CommandHandler.class);

    default void queueCommandResult(VelocityServer server, ConnectedPlayer player,
                                    BiFunction<CommandExecuteEvent, LastSeenMessages, CompletableFuture<MinecraftPacket>> futurePacketCreator,
                                    String message, Instant timestamp, @Nullable LastSeenMessages lastSeenMessages) {
        CompletableFuture<CommandExecuteEvent> eventFuture = server.getCommandManager().callCommandEvent(player, message);
        player.getChatQueue().queuePacket(
                newLastSeenMessages -> eventFuture
                        .thenComposeAsync(event -> futurePacketCreator.apply(event, newLastSeenMessages))
                        .thenApply(pkt -> {
                            if (server.getConfiguration().isLogCommandExecutions()) {
                                logger.info("{} -> executed command /{}", player, message);
                            }
                            return pkt;
                        }).exceptionally(e -> {
                            logger.info("Exception occurred while running command for {}", player.getUsername(), e);
                            player.sendMessage(
                                    Component.translatable("velocity.command.generic-error", NamedTextColor.RED));
                            return null;
                        }), timestamp, lastSeenMessages);
    }

    default CompletableFuture<MinecraftPacket> runCommand(VelocityServer server,
                                                  ConnectedPlayer player, String command,
                                                  Function<Boolean, MinecraftPacket> hasRunPacketFunction) {
        return server.getCommandManager().executeImmediatelyAsync(player, command)
                .thenApply(hasRunPacketFunction);
    }
}
