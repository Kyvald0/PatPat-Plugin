package net.lopymine.patpat.plugin.command.ratelimit;

import lombok.experimental.ExtensionMethod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import net.lopymine.patpat.plugin.command.api.ICommand;
import net.lopymine.patpat.plugin.config.PatPatConfig;
import net.lopymine.patpat.plugin.config.RateLimitConfig;
import net.lopymine.patpat.plugin.extension.CommandSenderExtension;
import net.lopymine.patpat.plugin.util.StringUtils;

import java.util.Collections;
import java.util.List;

@ExtensionMethod(CommandSenderExtension.class)
public class RateLimitEnableCommand extends RateLimitToggleCommand {

	@Override
	protected String getTranslationKey() {
		return "enable";
	}

	@Override
	protected boolean getValue() {
		return true;
	}

	@Override
	protected NamedTextColor getColor() {
		return NamedTextColor.GREEN;
	}
}
