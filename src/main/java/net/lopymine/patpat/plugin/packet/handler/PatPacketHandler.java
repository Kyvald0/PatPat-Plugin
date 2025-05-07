package net.lopymine.patpat.plugin.packet.handler;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.experimental.ExtensionMethod;

import net.lopymine.patpat.plugin.*;
import net.lopymine.patpat.plugin.command.ratelimit.RateLimitManager;
import net.lopymine.patpat.plugin.config.*;
import net.lopymine.patpat.plugin.config.option.ListMode;
import net.lopymine.patpat.plugin.extension.ByteArrayDataExtension;
import net.lopymine.patpat.plugin.packet.PatPatPacketManager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.*;
import org.jetbrains.annotations.*;

@ExtensionMethod(ByteArrayDataExtension.class)
public class PatPacketHandler implements IPacketHandler {

	public static final Map<Version, PatPacketHandler> HANDLERS = new HashMap<>();
	private static boolean initialized = false;

	public static void registerHandler(PatPacketHandler handler) {
		if (PatPacketHandler.initialized) {
			throw new IllegalArgumentException("Cannot register handler after initialization the PatPacketHandler!");
		}
		PatPacketHandler.HANDLERS.put(handler.getPacketVersion(), handler);
	}

	public static String getOutgoingPacketIdByPlayerProtocol(UUID player) {
		Version version = PatPatPacketManager.PLAYER_PROTOCOLS.get(player);
		PatPacketHandler handler = PatPacketHandler.HANDLERS.get(version);
		if (handler == null) {
			return PatPatPacketManager.PATPAT_S2C_PACKET_ID;
		}
		return handler.getOutgoingPacketId();
	}

	public static byte[] getOutgoingPacketBytesByPlayerProtocol(UUID player, Entity pattedEntity, Entity whoPattedEntity, ByteArrayDataOutput output) {
		Version version = PatPatPacketManager.PLAYER_PROTOCOLS.get(player);
		PatPacketHandler handler = PatPacketHandler.HANDLERS.get(version);
		if (handler == null) {
			output.writeUuid(pattedEntity.getUniqueId());
			output.writeUuid(whoPattedEntity.getUniqueId());
			return output.toByteArray();
		}
		return handler.getOutgoingPacketBytes(pattedEntity, whoPattedEntity, output);
	}

	public static void init() {
		Map<Version, PatPacketHandler> collect = PatPacketHandler.HANDLERS.entrySet()
				.stream()
				.sorted(Entry.comparingByKey())
				.collect(Collectors.toMap(
						Entry::getKey,
						Entry::getValue,
						(one, two) -> one,
						LinkedHashMap::new)
				);

		PatPacketHandler.HANDLERS.clear();
		PatPacketHandler.HANDLERS.putAll(collect);
		PatPacketHandler.HANDLERS.keySet().forEach((k) -> PatLogger.debug(k.toString())); // TODO remove this line after check
		PatPacketHandler.initialized = true;
	}

	@Override
	public void handle(Player sender, ByteArrayDataInput buf) {
		PatPatPlugin plugin = PatPatPlugin.getInstance();
		if (!this.canHandle(sender)) {
			return;
		}

		Entity pattedEntity = this.getPattedEntity(plugin, sender, buf);
		if (!(pattedEntity instanceof LivingEntity livingEntity)) {
			return;
		}

		if (livingEntity.isInvisible()) {
			return;
		}

		double patVisibilityRadius = plugin.getServer().getViewDistance() * 16;

		List<Player> nearbyPlayers = new ArrayList<>(pattedEntity
				.getNearbyEntities(patVisibilityRadius, patVisibilityRadius, patVisibilityRadius)
				.stream()
				.flatMap(entity -> {
					if (entity instanceof Player player) {
						return Stream.of(player);
					}
					return Stream.empty();
				}).toList());

		if (pattedEntity instanceof Player player) {
			nearbyPlayers.add(player);
		}

		for (Player player : nearbyPlayers) {
			UUID senderUuid = sender.getUniqueId();
			UUID playerUuid = player.getUniqueId();
			if (playerUuid.equals(senderUuid)) {
				continue;
			}

			byte[] byteArray = getOutgoingPacketBytesByPlayerProtocol(playerUuid, pattedEntity, sender, ByteStreams.newDataOutput());
			String packetId = getOutgoingPacketIdByPlayerProtocol(playerUuid);
			PatLogger.debug("Sending out pat packet to %s with id %s and data %s", player.getName(), packetId, Arrays.toString(byteArray));
			player.sendPluginMessage(plugin, packetId, byteArray);
		}
	}

	@Nullable
	protected Entity getPattedEntity(PatPatPlugin plugin, Player player, ByteArrayDataInput buf) {
		return plugin.getServer().getEntity(buf.readUuid());
	}

	private boolean canHandle(Player sender) {
		if (!sender.hasPermission(PatPatConfig.getInstance().getRateLimit().getPermissionBypass()) && !RateLimitManager.canPat(sender.getUniqueId())) {
			return false;
		}

		Set<UUID> uuids = PlayerListConfig.getInstance().getUuids();
		ListMode listMode = PatPatConfig.getInstance().getListMode();

		return switch (listMode) {
			case DISABLED -> true;
			case WHITELIST -> uuids.contains(sender.getUniqueId());
			case BLACKLIST -> !uuids.contains(sender.getUniqueId());
		};
	}

	@Override
	public String getIncomingPacketId() {
		return PatPatPacketManager.PATPAT_C2S_PACKET_ID;
	}

	@Override
	public String getOutgoingPacketId() {
		return PatPatPacketManager.PATPAT_S2C_PACKET_ID;
	}

	public Version getPacketVersion() {
		return Version.PACKET_V1_VERSION;
	}

	public byte[] getOutgoingPacketBytes(Entity pattedEntity, Entity whoPattedEntity, ByteArrayDataOutput output) {
		output.writeUuid(pattedEntity.getUniqueId());
		output.writeUuid(whoPattedEntity.getUniqueId());
		return output.toByteArray();
	}
}
