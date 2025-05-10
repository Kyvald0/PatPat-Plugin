package net.lopymine.patpat.plugin.packet.handler;

import com.google.common.io.*;
import lombok.experimental.ExtensionMethod;

import net.lopymine.patpat.plugin.PatLogger;
import net.lopymine.patpat.plugin.PatPatPlugin;
import net.lopymine.patpat.plugin.config.Version;
import net.lopymine.patpat.plugin.entity.PatPlayer;
import net.lopymine.patpat.plugin.extension.ByteArrayDataExtension;
import net.lopymine.patpat.plugin.util.StringUtils;


@ExtensionMethod(ByteArrayDataExtension.class)
public class HelloPacketHandler implements IPacketHandler {

	public static final String HELLO_PATPAT_SERVER_C2S_PACKET = StringUtils.modId("hello_patpat_server_c2s_packet");
	public static final String HELLO_PATPAT_PLAYER_S2C_PACKET = StringUtils.modId("hello_patpat_player_s2c_packet");

	@Override
	public void handle(PatPlayer sender, ByteArrayDataInput buf) {
		PatLogger.debug("Received hello packet from %s", sender.getName());
		readVersion(sender, buf);
		sendServerVersion(sender);
	}

	private void readVersion(PatPlayer sender, ByteArrayDataInput buf) {
		try {
			int major = buf.readUnsignedByte();
			int minor = buf.readUnsignedByte();
			int patch = buf.readUnsignedByte();
			Version senderVersion = new Version(major, minor, patch);
			sender.setVersion(senderVersion);
			PatLogger.debug("Player PatPat version: %s", senderVersion);
		} catch (Exception e) {
			PatLogger.error("Failed to read packet version from hello packet!", e);
		}
	}

	private void sendServerVersion(PatPlayer patPlayer) {
		Version serverVersion = Version.PLUGIN_VERSION;
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		output.writeByte(serverVersion.major());
		output.writeByte(serverVersion.minor());
		output.writeByte(serverVersion.patch());
		patPlayer.sendPluginMessage(PatPatPlugin.getInstance(), this.getOutgoingPacketId(), output.toByteArray());
	}

	@Override
	public String getIncomingPacketId() {
		return HELLO_PATPAT_SERVER_C2S_PACKET;
	}

	@Override
	public String getOutgoingPacketId() {
		return HELLO_PATPAT_PLAYER_S2C_PACKET;
	}
}
