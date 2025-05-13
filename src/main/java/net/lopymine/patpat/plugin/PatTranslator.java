package net.lopymine.patpat.plugin;

import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;

import net.lopymine.patpat.plugin.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatTranslator implements Translator {

	private static final String DEFAULT_LANG = "en_US";

	private static final String LANG_FOLDER = "lang/";
	private static final String LANG_FILETYPE = ".json";

	private final Map<String, Map<String, String>> localizations = new HashMap<>();

	private static PatTranslator instance;

	public static PatTranslator getInstance() {
		if (instance == null) {
			instance = new PatTranslator();
		}
		return instance;
	}

	public static void register() {
		GlobalTranslator.translator().addSource(getInstance());
	}

	public static void unregister() {
		if (instance != null) {
			GlobalTranslator.translator().removeSource(instance);
			instance = null;
		}
	}

	@SneakyThrows
	public PatTranslator() {
		registerInternalLangs();
		registerExternalLangs();
	}

	@SneakyThrows
	private void registerInternalLangs() {
		List<String> filenames = getInternalLangFiles();
		for (String filename : filenames) {
			readLangResourceFromJar(filename);
		}
	}

	private static List<String> getInternalLangFiles() throws IOException {
		URL url = PatPatPlugin.getInstance().getClass().getClassLoader().getResource(LANG_FOLDER);
		if (url == null) {
			return Collections.emptyList();
		}
		if (!Objects.equals(url.getProtocol(), "jar")) {
			return Collections.emptyList();
		}
		JarURLConnection connection = (JarURLConnection) url.openConnection();
		try (JarFile jar = connection.getJarFile()) {
			List<String> result = jar.stream()
					.sequential()
					.filter(entry -> !entry.isDirectory()
							&& entry.getName().startsWith(LANG_FOLDER)
							&& entry.getName().endsWith(LANG_FILETYPE))
					.map(JarEntry::getName)
					.map(str -> str.substring(LANG_FOLDER.length(), str.length() - LANG_FILETYPE.length()))
					.toList();
			PatLogger.debug("Found internal lang files: " + result);
			return result;
		}
	}


	private void readLangResourceFromJar(String lang) {
		Map<String, String> langResource = ResourceUtils.loadLangFromJar("%s%s%s".formatted(LANG_FOLDER, lang, LANG_FILETYPE));
		if (langResource == null) {
			return;
		}
		// Fix single quotes
		langResource.replaceAll((k, v) -> langResource.get(k).replace("'", "''"));

		this.localizations.computeIfAbsent(lang, k -> new HashMap<>()).putAll(langResource);
	}

	private void registerExternalLangs() {
		File langFolder = new File(PatPatPlugin.getInstance().getDataFolder(), "lang");
		if (!langFolder.exists() && !langFolder.isDirectory()) {
			return;
		}
		File[] jsonFiles = langFolder.listFiles((dir, name) ->
				name.toLowerCase().endsWith(LANG_FILETYPE) && new File(dir, name).isFile());
		if (jsonFiles == null) {
			return;
		}
		PatLogger.debug("Found external lang files: " + Arrays.stream(jsonFiles).map(File::getName).toList());

		for (File jsonFile : jsonFiles) {
			String name = jsonFile.getName();
			String lang = name.substring(0, name.length() - LANG_FILETYPE.length());
			Map<String, String> langResource = ResourceUtils.loadLang(jsonFile);
			if (langResource == null) {
				return;
			}
			PatLogger.info(lang);
			// Fix single quotes
			langResource.replaceAll((k, v) -> langResource.get(k).replace("'", "''"));

			this.localizations.computeIfAbsent(lang, k -> new HashMap<>()).putAll(langResource);
		}

	}

	@Override
	public @NotNull Key name() {
		return Key.key("patpat:translator");
	}

	@Nullable
	@Override
	public MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
		if (!key.startsWith("patpat")) {
			return null;
		}
		String lang = locale.toString();
		Map<String, String> localization = this.localizations.getOrDefault(lang, null);
		if (localization == null) {
			String message = this.localizations.get(DEFAULT_LANG).getOrDefault(key, null);
			return message == null ? null : new MessageFormat(message);
		}
		String message = localization.getOrDefault(key, this.localizations.get(DEFAULT_LANG).getOrDefault(key, null));
		return message == null ? null : new MessageFormat(message);
	}
}
