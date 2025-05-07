package net.lopymine.patpat.plugin.packet.handler;

import com.google.common.io.*;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;

import net.lopymine.patpat.plugin.*;
import net.lopymine.patpat.plugin.config.Version;
import net.lopymine.patpat.plugin.extension.ByteArrayDataExtension;
import net.lopymine.patpat.plugin.packet.PatPatPacketManager;

import java.util.Map.Entry;

@ExtensionMethod(ByteArrayDataExtension.class)
public class HelloPacketHandler implements IPacketHandler {

	@Override
	public void handle(Player sender, ByteArrayDataInput buf) {
		PatLogger.info("Received hello packet from %s", sender.getName());

		// Reading sender version
		try {
			int major = buf.readVarInt();
			int minor = buf.readVarInt();
			int patch = buf.readVarInt();
			Version senderVersion = new Version(major, minor, patch);

			PatPacketHandler handler = PatPacketHandler.HANDLERS.get(senderVersion);
			if (handler == null) {
				for (Entry<Version, PatPacketHandler> entry : PatPacketHandler.HANDLERS.entrySet()) {
					Version packetVersion = entry.getKey();
					if (!packetVersion.isLessThan(senderVersion)) {
						continue;
					}
					PatPatPacketManager.PLAYER_PROTOCOLS.put(sender.getUniqueId(), packetVersion);
					PatLogger.warn("Parsed unknown packet version: %s, using %s !", senderVersion.toString(), packetVersion.toString());
					return;
				}
			}

			PatPatPacketManager.PLAYER_PROTOCOLS.put(sender.getUniqueId(), senderVersion);
		} catch (Exception e) {
			PatLogger.error("Failed to read packet version from hello packet!", e);
			PatPatPacketManager.PLAYER_PROTOCOLS.put(sender.getUniqueId(), Version.PACKET_V1_VERSION);
		}

		// Sending server version
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		Version pluginVersion = Version.PLUGIN_VERSION;
		output.writeVarInt(pluginVersion.major());
		output.writeVarInt(pluginVersion.minor());
		output.writeVarInt(pluginVersion.patch());

		sender.sendPluginMessage(PatPatPlugin.getInstance(), this.getOutgoingPacketId(), output.toByteArray());
	}

	@Override
	public String getIncomingPacketId() {
		return PatPatPacketManager.HELLO_PATPAT_SERVER_C2S_PACKET;
	}

	@Override
	public String getOutgoingPacketId() {
		return PatPatPacketManager.HELLO_PATPAT_PLAYER_S2C_PACKET;
	}
}
