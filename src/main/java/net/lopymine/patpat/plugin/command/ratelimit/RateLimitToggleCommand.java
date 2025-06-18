package net.lopymine.patpat.plugin.command.ratelimit;

import lombok.experimental.ExtensionMethod;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import net.lopymine.patpat.plugin.command.api.ICommand;
import net.lopymine.patpat.plugin.config.*;
import net.lopymine.patpat.plugin.extension.CommandSenderExtension;
import net.lopymine.patpat.plugin.util.StringUtils;

import java.util.*;

@ExtensionMethod(CommandSenderExtension.class)
public abstract class RateLimitToggleCommand implements ICommand {

	@Override
	public List<String> getSuggestions(CommandSender sender, String[] strings) {
		return Collections.emptyList();
	}

	@Override
	public void execute(CommandSender sender, String[] strings) {
		PatPatConfig config = PatPatConfig.getInstance();
		RateLimitConfig rateLimitConfig = config.getRateLimit();
		if (rateLimitConfig.isEnabled() == this.getValue()) {
			sender.sendMsg("patpat.command.ratelimit.%s.already".formatted(this.getTranslationKey()));
			return;
		}
		rateLimitConfig.setEnabled(this.getValue());
		config.save();
		RateLimitManager.reloadTask();

		TranslatableComponent text = Component.translatable("patpat.command.ratelimit.%s.success".formatted(this.getTranslationKey()))
				.color(this.getColor());
		sender.sendMsg(text);
	}

	@Override
	public String getPermissionKey() {
		return StringUtils.permission("ratelimit.toggle");
	}

	@Override
	public String getExampleOfUsage() {
		return "/patpat ratelimit %s".formatted(this.getTranslationKey());
	}


	@Override
	public Component getDescription() {
		return Component.translatable("patpat.command.ratelimit.%s.description".formatted(this.getTranslationKey()));
	}

	protected abstract String getTranslationKey();

	protected abstract boolean getValue();

	protected abstract NamedTextColor getColor();
}
