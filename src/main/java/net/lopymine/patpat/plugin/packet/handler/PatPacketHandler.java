package net.lopymine.patpat.plugin.packet.handler;

import com.google.common.io.ByteArrayDataInput;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;

import net.lopymine.patpat.plugin.PatLogger;
import net.lopymine.patpat.plugin.PatPatPlugin;
import net.lopymine.patpat.plugin.command.ratelimit.RateLimitManager;
import net.lopymine.patpat.plugin.config.PatPatConfig;
import net.lopymine.patpat.plugin.config.PlayerListConfig;
import net.lopymine.patpat.plugin.config.option.ListMode;
import net.lopymine.patpat.plugin.entity.PatPlayer;
import net.lopymine.patpat.plugin.extension.ByteArrayDataExtension;
import net.lopymine.patpat.plugin.packet.*;
import net.lopymine.patpat.plugin.util.StringUtils;

import java.util.*;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(ByteArrayDataExtension.class)
public class PatPacketHandler implements IPacketHandler {

	private static final String PATPAT_C2S_PACKET_ID_V2 = StringUtils.modId("pat_entity_c2s_packet_v2");
	private static final String PATPAT_S2C_PACKET_ID_V2 = StringUtils.modId("pat_entity_s2c_packet_v2");

	private static final Set<IPatPacket> PAT_PACKET_HANDLERS = new LinkedHashSet<>();

	private final double patVisibilityRadius = Bukkit.getServer().getViewDistance() * 16D;

	public PatPacketHandler() {
		// MUST be in order from newer to older
		// Otherwise it will be handled in the wrong way, for example,
		// 1) 1.2.0(Client Version) >= 1.0.0(PatPacketV1)?
		// -> Use 1.0.0 packets (wrong)
		// 2) 1.2.0(Client Version) >= 1.2.0(PatPacketV2)?
		// -> Use 1.2.0 packets, you will say, but we are already using 1.0.0 packets because of the wrong order

		PAT_PACKET_HANDLERS.clear();
		PAT_PACKET_HANDLERS.add(new PatPacketV2());
		PAT_PACKET_HANDLERS.add(new PatPacketV1());
	}

	@Override
	public void handle(PatPlayer sender, ByteArrayDataInput buf) {
		PatPatPlugin plugin = PatPatPlugin.getInstance();
		if (!this.canHandle(sender.getPlayer())) {
			return;
		}
		IPatPacket senderPacketHandler = sender.getPatPacketHandler();
		if (senderPacketHandler == null) {
			PatLogger.debug("Not found packet handler for player with PatPat version: %s", sender.getVersion());
			return;
		}

		Entity pattedEntity = senderPacketHandler.getPattedEntity(sender, buf);
		if (!(pattedEntity instanceof LivingEntity livingEntity)) {
			return;
		}

		if (livingEntity.isInvisible()) {
			return;
		}

		List<PatPlayer> nearbyPlayers = new ArrayList<>(pattedEntity
				.getNearbyEntities(patVisibilityRadius, patVisibilityRadius, patVisibilityRadius)
				.stream()
				.map(entity -> {
					if (entity instanceof Player player) {
						return player;
					}
					return null;
				})
				.filter(Objects::nonNull)
				.map(PatPlayer::of)
				.toList()
		);

		if (pattedEntity instanceof Player player) {
			nearbyPlayers.add(PatPlayer.of(player));
		}

		UUID senderUuid = sender.getUniqueId();
		Map<String, PatPacket> packets = new HashMap<>();
		nearbyPlayers.forEach(player -> {
			UUID playerUuid = player.getUniqueId();
			if (playerUuid.equals(senderUuid)) {
				return;
			}
			IPatPacket packetHandler = player.getPatPacketHandler();
			if (packetHandler == null) {
				return;
			}
			PatPacket packet = packets.computeIfAbsent(
					packetHandler.getPacketHandlerId(),
					s -> packetHandler.getPacket(pattedEntity, sender.getPlayer())
			);
			PatLogger.debug("Sending out pat packet to %s with id %s and data %s", player.getName(), packet.channel(), Arrays.toString(packet.bytes()));
			player.sendPluginMessage(plugin, packet.channel(), packet.bytes());
		});
	}

	@Nullable
	public static IPatPacket getPacketHandler(PatPlayer player) {
		for (IPatPacket packetHandler : PAT_PACKET_HANDLERS) {
			if (packetHandler.canHandle(player)) {
				return packetHandler;
			}
		}
		return null;
	}

	private boolean canHandle(Player sender) {
		UUID senderUuid = sender.getUniqueId();
		if (!sender.hasPermission(PatPatConfig.getInstance().getRateLimit().getPermissionBypass()) && !RateLimitManager.canPat(senderUuid)) {
			return false;
		}

		Set<UUID> uuids = PlayerListConfig.getInstance().getUuids();
		ListMode listMode = PatPatConfig.getInstance().getListMode();

		return switch (listMode) {
			case DISABLED -> true;
			case WHITELIST -> uuids.contains(senderUuid);
			case BLACKLIST -> !uuids.contains(senderUuid);
		};
	}

	@Override
	public String getIncomingPacketId() {
		return PATPAT_C2S_PACKET_ID_V2;
	}

	@Override
	public String getOutgoingPacketId() {
		return PATPAT_S2C_PACKET_ID_V2;
	}
}
