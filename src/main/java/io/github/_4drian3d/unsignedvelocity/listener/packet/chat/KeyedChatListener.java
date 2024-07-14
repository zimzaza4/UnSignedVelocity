package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.chat.ChatQueue;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerChatPacket;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.EventListener;
import io.github._4drian3d.vpacketevents.api.event.PacketReceiveEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class KeyedChatListener implements EventListener {
    @Inject
    private EventManager eventManager;
    @Inject
    private UnSignedVelocity plugin;
    @Inject
    private Configuration configuration;

    @Override
    public void register() {
        eventManager.register(plugin, PacketReceiveEvent.class, this::onChat);
    }

    private void onChat(final PacketReceiveEvent event) {
        // Packet sent by players with version 1.19 and 1.19.1
        if (!(event.getPacket() instanceof final KeyedPlayerChatPacket packet)) {
            return;
        }

        final ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();
        if (!checkConnection(player)) return;

        ChatQueue chatQueue = player.getChatQueue();
        event.setResult(ResultedEvent.GenericResult.denied());

        PlayerChatEvent toSend = new PlayerChatEvent(player, packet.getMessage());
        CompletableFuture<PlayerChatEvent> future = eventManager.fire(toSend);

        CompletableFuture<MinecraftPacket> chatFuture;

        chatFuture = future.thenApply(pme -> {
            PlayerChatEvent.ChatResult chatResult = pme.getResult();
            if (!chatResult.isAllowed()) {
                return null;
            }

            return player.getChatBuilderFactory().builder()
                    .message(chatResult.getMessage().orElse(packet.getMessage()))
                    .setTimestamp(packet.getExpiry()).toServer();
        });

        chatQueue.queuePacket(
                newLastSeen -> chatFuture,
                packet.getExpiry(),
                null
        );
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.applyChatMessages();
    }
}
