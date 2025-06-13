package net.lopymine.patpat.plugin.command.ratelimit.set;

import lombok.experimental.ExtensionMethod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import net.lopymine.patpat.plugin.command.api.ICommand;
import net.lopymine.patpat.plugin.command.ratelimit.RateLimitManager;
import net.lopymine.patpat.plugin.command.ratelimit.Time;
import net.lopymine.patpat.plugin.config.PatPatConfig;
import net.lopymine.patpat.plugin.config.RateLimitConfig;
import net.lopymine.patpat.plugin.extension.CommandSenderExtension;
import net.lopymine.patpat.plugin.util.StringUtils;

import java.util.Collections;
import java.util.List;

@ExtensionMethod(CommandSenderExtension.class)
public class IntervalCommand implements ICommand {

	@Override
	public List<String> getSuggestions(CommandSender sender, String[] strings) {
		return Collections.emptyList();
	}

	@Override
	public void execute(CommandSender sender, String[] strings) {
		PatPatConfig config = PatPatConfig.getInstance();
		RateLimitConfig rateLimitConfig = config.getRateLimit();
		if (strings.length == 0) {
			sender.sendTranslatable("patpat.command.ratelimit.set.interval.info",
					Component.text(rateLimitConfig.getTokenInterval().toString())
							.color(NamedTextColor.GOLD));
			return;
		}
		if (strings.length > 1) {
			sender.sendPatPatMessage(this.getExampleOfUsage());
			return;
		}
		try {
			Time value = Time.of(strings[0]);
			if (value.getValue() <= 0) {
				sender.sendTranslatable("patpat.command.error.time_less_than",
						Component.text(value.toString()).color(NamedTextColor.GOLD),
						Component.text("1sec").color(NamedTextColor.GOLD)
				);
				return;
			}
			rateLimitConfig.setTokenInterval(value);
			config.save();
			RateLimitManager.reloadTask();
			sender.sendTranslatable("patpat.command.ratelimit.set.interval",
					Component.text(value.toString()).color(NamedTextColor.GOLD));
		} catch (IllegalArgumentException ignored) {
			sender.sendTranslatable("patpat.command.error.time_not_time",
					Component.text(strings[0]).color(NamedTextColor.GOLD)
			);
		}
	}

	@Override
	public String getPermissionKey() {
		return StringUtils.permission("ratelimit.set.interval");
	}

	@Override
	public String getExampleOfUsage() {
		return "/patpat ratelimit set interval [value]";
	}

	@Override
	public Component getDescription() {
		return Component.translatable("patpat.command.ratelimit.set.interval.description");
	}

}
