package net.lopymine.patpat.plugin;

import net.lopymine.patpat.plugin.config.PatPatConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PatLogger {

	private PatLogger() {
		throw new IllegalStateException("Logger class");
	}

	public static final Logger LOGGER = Logger.getLogger(PatPatPlugin.PLUGIN_ID);

	public static void info(String message, Object... args) {
		message = format(message, args);
		LOGGER.info(message);
	}

	public static void warn(String message, Exception e) {
		LOGGER.log(Level.WARNING, message, e);
	}

	public static void warn(String message, Object... args) {
		message = format(message, args);
		LOGGER.warning(message);
	}

	public static void error(String message, Exception e) {
		LOGGER.log(Level.SEVERE, message, e);
	}

	public static void error(String message, Object... args) {
		message = format(message, args);
		LOGGER.severe(message);
	}

	public static void debug(String message, Object... args) {
		if (!PatPatConfig.getInstance().isDebug()) {
			return;
		}
		message = "[DEBUG]: " + format(message, args);
		LOGGER.info(message);
	}


	private static String format(String message, Object... args) {
		if (args.length != 0) {
			return message.formatted(args);
		}
		return message;
	}
}
