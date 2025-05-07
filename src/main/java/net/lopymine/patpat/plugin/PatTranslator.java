package net.lopymine.patpat.plugin;

import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.Translator;

import net.lopymine.patpat.plugin.util.ResourceUtils;

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

	private static final String DEFAULT_LANG = "en-US";

	private static final String LANG_FOLDER = "lang/";
	private static final String LANG_FILETYPE = ".json";

	private final Map<String, Map<String, String>> localizations = new HashMap<>();

	@SneakyThrows
	public PatTranslator() {
		List<String> filenames = getBuiltinLangFiles();
		PatLogger.debug("Found builtin lang files: " + filenames);
		for (String filename : filenames) {
			readLangResource(filename);
		}
	}

	private static List<String> getBuiltinLangFiles() throws IOException {
		URL url = PatPatPlugin.getInstance().getClass().getClassLoader().getResource(LANG_FOLDER);
		if (url == null) {
			return Collections.emptyList();
		}
		if (!Objects.equals(url.getProtocol(), "jar")) {
			return Collections.emptyList();
		}
		JarURLConnection connection = (JarURLConnection) url.openConnection();
		try (JarFile jar = connection.getJarFile()) {
			return jar.stream()
					.sequential()
					.filter(entry -> !entry.isDirectory()
							&& entry.getName().startsWith(LANG_FOLDER)
							&& entry.getName().endsWith(LANG_FILETYPE))
					.map(JarEntry::getName)
					.map(str -> str.substring(LANG_FOLDER.length(), str.length() - LANG_FILETYPE.length()))
					.toList();
		}
	}

	private void readLangResource(String lang) {
		Map<String, String> langResource = ResourceUtils.loadLangFromJar("%s%s%s".formatted(LANG_FOLDER, lang, LANG_FILETYPE));
		if (langResource == null) {
			return;
		}
		// Fix single quotes
		langResource.replaceAll((k, v) -> langResource.get(k).replace("'", "''"));

		this.localizations.computeIfAbsent(lang, k -> new HashMap<>()).putAll(langResource);
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
		String lang = locale.toLanguageTag();
		Map<String, String> localization = this.localizations.getOrDefault(lang, null);
		if (localization == null) {
			String message = this.localizations.get(DEFAULT_LANG).getOrDefault(key, null);
			return message == null ? null : new MessageFormat(message);
		}
		String message = localization.getOrDefault(key, this.localizations.get(DEFAULT_LANG).getOrDefault(key, null));
		return message == null ? null : new MessageFormat(message);
	}
}
