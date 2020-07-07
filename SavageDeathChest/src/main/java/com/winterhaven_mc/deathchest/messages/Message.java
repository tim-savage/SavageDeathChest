package com.winterhaven_mc.deathchest.messages;

import com.winterhaven_mc.util.AbstractMessage;
import org.bukkit.command.CommandSender;


public class Message extends AbstractMessage<MessageId, Macro> {


	/**
	 * Private class constructor; calls inherited super constructor
	 *
	 * @param recipient the message recipient
	 * @param messageId the enum entry representing the message to be displayed
	 */
	private Message(final CommandSender recipient, final MessageId messageId) {
		super(recipient, messageId);
	}


	/**
	 * Static class constructor
	 *
	 * @param recipient the message recipient
	 * @param messageId the enum entry representing the message to be displayed
	 * @return new instance of Message created with private constructor
	 */
	public static Message create(final CommandSender recipient, final MessageId messageId) {
		return new Message(recipient, messageId);
	}

}
