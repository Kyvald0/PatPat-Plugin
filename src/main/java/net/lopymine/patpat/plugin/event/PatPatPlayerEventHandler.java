package net.lopymine.patpat.plugin.event;

import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.lopymine.patpat.plugin.*;
import net.lopymine.patpat.plugin.config.Version;
import net.lopymine.patpat.plugin.extension.PlayerExtension;
import net.lopymine.patpat.plugin.packet.PatPatPacketManager;

@ExtensionMethod(PlayerExtension.class)
public class PatPatPlayerEventHandler implements Listener {

	public static void register() {
		PatPatPlugin plugin = PatPatPlugin.getInstance();
		plugin.getServer().getPluginManager().registerEvents(new PatPatPlayerEventHandler(), plugin);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PatPatPacketManager.PLAYER_PROTOCOLS.put(player.getUniqueId(), Version.PACKET_V1_VERSION);
		PatLogger.debug("Player %s joined", player.getName());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		PatPatPacketManager.PLAYER_PROTOCOLS.remove(player.getUniqueId());
		PatLogger.debug("Player %s quit", player.getName());
	}

}
