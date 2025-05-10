package net.lopymine.patpat.plugin.entity;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.lopymine.patpat.plugin.config.Version;
import net.lopymine.patpat.plugin.extension.PlayerExtension;
import net.lopymine.patpat.plugin.packet.handler.IPatPacket;
import net.lopymine.patpat.plugin.packet.handler.PatPacketHandler;

import java.util.*;
import org.jetbrains.annotations.NotNull;

@Getter
@ExtensionMethod(PlayerExtension.class)
public class PatPlayer {

	private static final Version DEFAULT_VERSION = new Version(1, 0, 0);
	private static final Map<Player, PatPlayer> PAT_PLAYERS = new HashMap<>();

	private final Player player;
	private Version version = DEFAULT_VERSION;
	private IPatPacket patPacketHandler;

	public PatPlayer(Player player) {
		this(player, DEFAULT_VERSION);
	}

	public PatPlayer(Player player, Version version) {
		this.player = player;
		this.setVersion(version);
	}


	public static PatPlayer of(@NotNull Player player) {
		return PAT_PLAYERS.computeIfAbsent(player, PatPlayer::new);
	}

	@CanIgnoreReturnValue
	public static PatPlayer register(@NotNull Player player) {
		PatPlayer patPlayer = new PatPlayer(player, DEFAULT_VERSION);
		PAT_PLAYERS.put(player, patPlayer);
		return patPlayer;
	}

	public static void unregister(@NotNull Player player) {
		PAT_PLAYERS.remove(player);
	}

	public void setVersion(Version version) {
		this.version = version;
		updatePatPacketHandler();
	}

	private void updatePatPacketHandler() {
		this.patPacketHandler = PatPacketHandler.getPacketHandler(this);
	}

	public String getName() {
		return player.getName();
	}

	public void sendPluginMessage(@NotNull Plugin source, @NotNull String channel, byte @NotNull [] message) {
		player.sendPluginMessage(source, channel, message);
	}

	public UUID getUniqueId() {
		return player.getUniqueId();
	}

	public World getWorld() {
		return player.getWorld();
	}

	public void sendPatPatMessage(String message, Object... args) {
		this.player.sendPatPatMessage(message, args);
	}

	public void sendPatPatMessage(ComponentLike message) {
		this.player.sendPatPatMessage(message);
	}
}
