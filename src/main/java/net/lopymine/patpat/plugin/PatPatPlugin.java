package net.lopymine.patpat.plugin;

import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.bukkit.plugin.java.JavaPlugin;

import net.lopymine.patpat.plugin.command.PatPatCommandManager;
import net.lopymine.patpat.plugin.config.PatPatConfig;
import net.lopymine.patpat.plugin.config.PlayerListConfig;
import net.lopymine.patpat.plugin.config.migrate.MigrateManager;
import net.lopymine.patpat.plugin.event.PatPatPlayerEventHandler;
import net.lopymine.patpat.plugin.packet.PatPatPacketManager;

@Getter
public class PatPatPlugin extends JavaPlugin {

	public static final String MOD_ID = "patpat-plugin";

	@Getter
	private static PatPatPlugin instance;

	@Getter
	private static BukkitAudiences adventure;

	private static Translator myTranslator;

	@Override
	@SuppressWarnings("java:S2696") // Plugins system
	public void onEnable() {
		instance  = this;
		adventure = BukkitAudiences.create(this);
		if (!this.getDataFolder().exists() && !this.getDataFolder().mkdirs()) {
			PatLogger.warn("Failed to create config folder for PatPat Plugin!");
		}
		MigrateManager.migrate();
		PatPatConfig.reload();
		PlayerListConfig.reload();
		MigrateManager.checkVersion();

		PatPatPacketManager.register();
		PatPatCommandManager.register();
		PatPatPlayerEventHandler.register();
		myTranslator = new PatTranslator();
		GlobalTranslator.translator().addSource(myTranslator);

		PatLogger.info("Plugin started");
	}

	@Override
	@SuppressWarnings("java:S2696") // Plugins system
	public void onDisable() {
		if (adventure != null) {
			adventure.close();
			adventure = null;
		}
		if (myTranslator != null) {
			GlobalTranslator.translator().removeSource(myTranslator);
			myTranslator = null;
		}

	}
}
