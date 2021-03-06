/*
 *
 * Erstellt am: 22 Jan 2020 21:15:48
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.bot.core;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import coffee.michel.sebcord.configuration.persistence.ConfigurationPersistenceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

/**
 * @author Jonas Michel
 *
 */
@Component
public class GuildEventFilter implements JDAEventFilter {

	@Autowired
	private ConfigurationPersistenceManager	cpm;
	private long							handledServerId;

	@PostConstruct
	public void init() {
		handledServerId = cpm.getBotConfig().getHandledServerId();
	}

	@Override
	public boolean allow(GenericEvent e) {
		if (e instanceof GenericGuildEvent) {
			Guild guild = ((GenericGuildEvent) e).getGuild();
			var guildID = guild.getId();
			if (!String.valueOf(handledServerId).equals(guildID))
				return false;
		}
		return true;
	}

}
