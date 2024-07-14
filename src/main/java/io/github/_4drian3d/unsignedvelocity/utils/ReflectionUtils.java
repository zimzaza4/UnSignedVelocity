package io.github._4drian3d.unsignedvelocity.utils;

import com.velocitypowered.proxy.protocol.packet.chat.LastSeenMessages;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommandPacket;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static Field LAST_SEEN_MESSAGES_FIELD;

    public static Class<?> SESSION_COMMAND_PACKET;

    static {
        try {
            SESSION_COMMAND_PACKET = Class.forName("com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommandPacket");
            LAST_SEEN_MESSAGES_FIELD = SESSION_COMMAND_PACKET.getDeclaredField("lastSeenMessages");
            LAST_SEEN_MESSAGES_FIELD.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
        }

    }
    public static LastSeenMessages getLastSeenMessages(SessionPlayerCommandPacket packet) {
        try {
            return (LastSeenMessages) LAST_SEEN_MESSAGES_FIELD.get(packet);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
