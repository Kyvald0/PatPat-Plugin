package net.lopymine.patpat.plugin.packet;

import org.bukkit.plugin.messaging.Messenger;

import net.lopymine.patpat.plugin.PatPatPlugin;
import net.lopymine.patpat.plugin.packet.handler.*;

public class PatPatPacketManager {

	private PatPatPacketManager() {
		throw new IllegalStateException("Manager class");
	}

	public static void register() {
		// Create Main Listener for PatPat Packets
		PatPatPacketListener listener = new PatPatPacketListener();

		PatPatPlugin plugin = PatPatPlugin.getInstance();
		Messenger messenger = plugin.getServer().getMessenger();

		// Register Packet Handlers
		listener.registerPacket(new PatPacketHandler(), messenger);
		listener.registerPacket(new PatPacketOldHandler(), messenger);
		listener.registerPacket(new HelloPacketHandler(), messenger);
	}
}
