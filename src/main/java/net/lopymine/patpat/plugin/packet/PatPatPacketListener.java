package net.lopymine.patpat.plugin.packet;

import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.*;

import net.lopymine.patpat.plugin.*;
import net.lopymine.patpat.plugin.packet.handler.*;

import java.util.*;
import org.jetbrains.annotations.NotNull;

public class PatPatPacketListener implements PluginMessageListener {

	private final Map<String, IPacketHandler> handlers = new HashMap<>();

	@Override
	public void onPluginMessageReceived(@NotNull String s, @NotNull Player sender, byte[] bytes) {
		IPacketHandler packetHandler = this.handlers.get(s);
		PatLogger.debug("Received packet with id %s from %s with data %s".formatted(s, sender.getName(), Arrays.toString(bytes)));
		if (packetHandler == null) {
			return;
		}
		PatLogger.debug("Handling packet with " + packetHandler.getClass() + " from packet id" + packetHandler.getIncomingPacketId());
		packetHandler.handle(sender, ByteStreams.newDataInput(bytes));
	}

	public void registerPacket(IPacketHandler handler, Messenger messenger) {
		this.handlers.put(handler.getIncomingPacketId(), handler);
		if (handler instanceof PatPacketHandler patPacketHandler) {
			PatPacketHandler.registerHandler(patPacketHandler);
		}

		messenger.registerIncomingPluginChannel(PatPatPlugin.getInstance(), handler.getIncomingPacketId(), this);
		messenger.registerOutgoingPluginChannel(PatPatPlugin.getInstance(), handler.getOutgoingPacketId());
	}
}
