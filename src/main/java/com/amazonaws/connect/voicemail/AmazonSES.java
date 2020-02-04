package com.amazonaws.connect.voicemail;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

public class AmazonSES {

	private Regions region = Regions.AP_SOUTHEAST_2;

	private String sender = "";

	// Replace recipient@example.com with a "To" address. If your account
	// is still in the sandbox, this address must be verified.
	private String recipient = "";

	// The subject line for the email.
	private String subject = "";

	// The full path to the file that will be attached to the email.
	// If you're using Windows, escape backslashes as shown in this variable.
	private String attachment = "";

	// The email body for recipients with non-HTML email clients.
	private String bodyText = "";

	private String bodyHTML = "";

	public void send() throws Exception {

		Session session = Session.getDefaultInstance(new Properties());

		// Create a new MimeMessage object.
		MimeMessage message = new MimeMessage(session);

		// Add subject, from and to lines.
		message.setSubject(subject, "UTF-8");
		message.setFrom(new InternetAddress(sender));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));

		// Create a multipart/alternative child container.
		MimeMultipart msg_body = new MimeMultipart("alternative");

		// Create a wrapper for the HTML and text parts.
		MimeBodyPart wrap = new MimeBodyPart();

		// Define the text part.
		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setContent(bodyText, "text/plain; charset=UTF-8");

		// Define the HTML part.
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(bodyHTML, "text/html; charset=UTF-8");

		// Add the text and HTML parts to the child container.
		msg_body.addBodyPart(textPart);
		msg_body.addBodyPart(htmlPart);

		// Add the child container to the wrapper object.
		wrap.setContent(msg_body);

		// Create a multipart/mixed parent container.
		MimeMultipart msg = new MimeMultipart("mixed");

		// Add the parent container to the message.
		message.setContent(msg);

		// Add the multipart/alternative part to the message.
		msg.addBodyPart(wrap);

		if (StringUtils.isNotBlank(attachment)) {
			// Define the attachment
			MimeBodyPart att = new MimeBodyPart();
			DataSource fds = new FileDataSource(attachment);
			att.setDataHandler(new DataHandler(fds));
			att.setFileName(fds.getName());

			// Add the attachment to the message.
			msg.addBodyPart(att);
		}
		// Try to send the email.
		try {
			// Instantiate an Amazon SES client, which will make the service
			// call with the supplied AWS credentials.
			AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
					// Replace US_WEST_2 with the AWS Region you're using for
					// Amazon SES.
					.withRegion(region).build();
			// Send the email.
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			message.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
			client.sendRawEmail(rawEmailRequest);
			// Display an error if something goes wrong.
		} catch (Exception ex) {
			throw ex;
		}
	}

	public Regions getRegion() {
		return region;
	}

	public void setRegion(Regions region) {
		this.region = region;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	public String getBodyHTML() {
		return bodyHTML;
	}

	public void setBodyHTML(String bodyHTML) {
		this.bodyHTML = bodyHTML;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AmazonSES [region=");
		builder.append(region);
		builder.append(", sender=");
		builder.append(sender);
		builder.append(", recipient=");
		builder.append(recipient);
		builder.append(", subject=");
		builder.append(subject);
		builder.append(", attachment=");
		builder.append(attachment);
		builder.append(", bodyText=");
		builder.append(bodyText);
		builder.append(", bodyHTML=");
		builder.append(bodyHTML);
		builder.append("]");
		return builder.toString();
	}

}
