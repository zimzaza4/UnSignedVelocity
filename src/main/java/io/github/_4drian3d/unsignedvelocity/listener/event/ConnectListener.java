package io.github._4drian3d.unsignedvelocity.listener.event;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.EventListener;

import java.lang.reflect.Field;

public class ConnectListener implements EventListener {
    private static final Field KEY;

    static {

        try {
            KEY = ConnectedPlayer.class.getDeclaredField( "playerKey");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Inject
    private EventManager eventManager;
    @Inject
    private UnSignedVelocity plugin;
    @Inject
    private Configuration configuration;

    @Subscribe
    void onJoin(PostLoginEvent event) throws Throwable {
        if (!configuration.removeSignedKey()) {
            return;
        }
        final Player player = event.getPlayer();
        if (player.getIdentifiedKey() != null) {
            KEY.setAccessible(true);
            KEY.set(player, null);
        }
    }

    @Override
    public void register() {
        eventManager.register(plugin, this);
    }

    @Override
    public boolean canBeLoaded() {
        return true;
    }
}
