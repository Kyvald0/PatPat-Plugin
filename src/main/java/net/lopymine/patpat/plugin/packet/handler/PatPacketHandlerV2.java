package net.lopymine.patpat.plugin.packet.handler;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.lopymine.patpat.plugin.*;
import net.lopymine.patpat.plugin.config.Version;
import net.lopymine.patpat.plugin.extension.ByteArrayDataExtension;
import net.lopymine.patpat.plugin.packet.PatPatPacketManager;

import java.io.IOException;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(ByteArrayDataExtension.class)
public class PatPacketHandlerV2 extends PatPacketHandler {

	@Override
	public String getIncomingPacketId() {
		return PatPatPacketManager.PATPAT_C2S_PACKET_ID_V2;
	}

	@Override
	public String getOutgoingPacketId() {
		return PatPatPacketManager.PATPAT_S2C_PACKET_ID_V2;
	}

	@Override
	public Version getPacketVersion() {
		return Version.PACKET_V2_VERSION;
	}

	@Override
	@Nullable
	protected Entity getPattedEntity(PatPatPlugin plugin, Player sender, ByteArrayDataInput buf) {
		try {
			int entityId = buf.readVarInt();
			for (Entity entity : sender.getWorld().getEntities()) { // TODO Should we do smthg with this????
				if (entity.getEntityId() != entityId) {
					continue;
				}
				return entity;
			}
		} catch (Exception e) {
			PatLogger.warn("Failed to parse entityId from incoming packet from player %s[%s]! Ignoring packet.".formatted(sender.getName(), sender.getUniqueId()), e);
		}
		return null;
	}

	@Override
	public byte[] getOutgoingPacketBytes(Entity pattedEntity, Entity whoPattedEntity, ByteArrayDataOutput output) {
		output.writeVarInt(pattedEntity.getEntityId());
		output.writeVarInt(whoPattedEntity.getEntityId());
		return output.toByteArray();
	}
}
