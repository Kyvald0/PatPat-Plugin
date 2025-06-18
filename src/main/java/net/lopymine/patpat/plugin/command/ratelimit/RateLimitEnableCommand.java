package net.lopymine.patpat.plugin.command.ratelimit;

import lombok.experimental.ExtensionMethod;
import net.kyori.adventure.text.format.NamedTextColor;

import net.lopymine.patpat.plugin.extension.CommandSenderExtension;

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
