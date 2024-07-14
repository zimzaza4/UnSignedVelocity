package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.ChatQueue;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChatPacket;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.EventListener;
import io.github._4drian3d.vpacketevents.api.event.PacketReceiveEvent;

import java.util.concurrent.CompletableFuture;

public final class SessionChatListener implements EventListener {
    @Inject
    private UnSignedVelocity plugin;
    @Inject
    private EventManager eventManager;
    @Inject
    private Configuration configuration;


    @Override
    public void register() {
        eventManager.register(plugin, PacketReceiveEvent.class, this::onChat);
    }

    private void onChat(PacketReceiveEvent event) {
        // Packet sent by players with version 1.19.3 and up
        if (!(event.getPacket() instanceof final SessionPlayerChatPacket packet)) {
            return;
        }

        final ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();
        if (!checkConnection(player)) return;

        event.setResult(ResultedEvent.GenericResult.denied());

        ChatQueue chatQueue = player.getChatQueue();
        PlayerChatEvent toSend = new PlayerChatEvent(player, packet.getMessage());
        CompletableFuture<PlayerChatEvent> eventFuture = eventManager.fire(toSend);
        chatQueue.queuePacket(
                newLastSeenMessages -> eventFuture
                        .thenApply(pme -> {
                            PlayerChatEvent.ChatResult chatResult = pme.getResult();

                            if (!chatResult.isAllowed()) {
                                return null;
                            }

                            if (chatResult.getMessage().map(str -> !str.equals(packet.getMessage()))
                                    .orElse(false)) {
                                return player.getChatBuilderFactory().builder().message(chatResult.getMessage().orElseThrow())
                                        .setTimestamp(packet.getTimestamp())
                                        .setLastSeenMessages(newLastSeenMessages)
                                        .toServer();
                            }
                            return packet.withLastSeenMessages(newLastSeenMessages);
                        }),
                packet.getTimestamp(),
                packet.getLastSeenMessages()
        );
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.applyChatMessages();
    }
}
