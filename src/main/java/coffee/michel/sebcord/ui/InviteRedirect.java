/*
 *
 * Erstellt am: 24 Jan 2020 20:12:57
 * Erstellt von: Jonas Michel
 */
package coffee.michel.sebcord.ui;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import coffee.michel.sebcord.bot.core.JDADCClient;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * @author Jonas Michel
 *
 */
@Route("join")
public class InviteRedirect extends Div implements BeforeEnterObserver {
	private static final long	serialVersionUID	= 6414204170203170029L;

	@Autowired
	private JDADCClient			client;

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		TextChannel systemChannel = client.getGuild().getSystemChannel();
		String inviteURL = systemChannel.createInvite().setMaxAge(300).setMaxUses(1).setTemporary(true)
				.reason("Invite created by sebcord-bot").complete().getUrl();

		UI.getCurrent().getPage().setLocation(inviteURL);
	}

}