package net.lopymine.patpat.plugin.packet.handler;

import com.google.common.io.ByteArrayDataInput;
import org.bukkit.entity.Player;

public interface IPacketHandler {

	void handle(Player sender, ByteArrayDataInput buf);

	String getIncomingPacketId();

	String getOutgoingPacketId();

}
