package net.lopymine.patpat.plugin.command.ratelimit;

import lombok.experimental.ExtensionMethod;
import net.kyori.adventure.text.format.NamedTextColor;

import net.lopymine.patpat.plugin.extension.CommandSenderExtension;

@ExtensionMethod(CommandSenderExtension.class)
public class RateLimitDisableCommand extends RateLimitToggleCommand {

	@Override
	protected String getTranslationKey() {
		return "disable";
	}

	@Override
	protected boolean getValue() {
		return false;
	}

	@Override
	protected NamedTextColor getColor() {
		return NamedTextColor.RED;
	}

}