package net.lopymine.patpat.plugin.packet.manager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.messaging.Messenger;

import net.lopymine.patpat.plugin.PatPatPlugin;
import net.lopymine.patpat.plugin.packet.handler.*;
import net.lopymine.patpat.plugin.packet.listener.PatPatPacketListener;

public class PatPatPacketManager {

	private PatPatPacketManager() {
		throw new IllegalStateException("Manager class");
	}

	@SuppressWarnings("deprecation")
	public static void register() {
		// Create Main Listener for PatPat Packets
		PatPatPacketListener listener = new PatPatPacketListener();

		PatPatPlugin plugin = PatPatPlugin.getInstance();
		Messenger messenger = plugin.getServer().getMessenger();

		// Register Packet Handlers
		listener.registerPacket(new PatPacketHandler(), messenger);
		listener.registerPacket(new PatPacketOldHandler(), messenger);
		listener.registerPacket(new HelloPacketHandler(), messenger);

		// Send packet for online players (/reload confirm)
		Bukkit.getOnlinePlayers().forEach(HelloPacketHandler::sendHelloPacket);
	}

}
