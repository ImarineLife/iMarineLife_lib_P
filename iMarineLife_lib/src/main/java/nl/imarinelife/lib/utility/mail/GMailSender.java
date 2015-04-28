package nl.imarinelife.lib.utility.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.util.Log;

public class GMailSender extends javax.mail.Authenticator {
	private static String TAG = "GMailSender";
	
	private String mailhost = "smtp.gmail.com";
	private String user;
	private String password;
	private Session session;
	private Multipart _multipart = new MimeMultipart();

	static {
		Security.addProvider(new JSSEProvider());
	}

	public GMailSender(String user, String password) {
		this.user = user;
		this.password = password;

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailhost);
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");

		session = Session.getDefaultInstance(props, this);
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, password);
	}

	public synchronized void sendMail(String subject, String body,
			String sender, String recipients) throws Exception {
		sendMail(subject, body, sender, recipients, null);
	}

	public synchronized void sendMail(String subject, String body,
			String sender, String recipients, String fileName) throws Exception {
		Log.d(TAG, "sendMail[" + subject + "]["+body+"]["+sender+"][to: "+recipients+"][fileName: "+fileName+"]");
		MimeMessage message = new MimeMessage(session);
		DataHandler handler = new DataHandler(new ByteArrayDataSource(
				body.getBytes(), "text/plain"));
		message.setSender(new InternetAddress(sender));
		message.setFrom(new InternetAddress(sender));
		message.setSubject(subject);
		message.setDataHandler(handler);
		if (recipients.indexOf(',') > 0)
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(recipients));
		else
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(
					recipients));
		if (fileName != null) {
			addAttachment(fileName, body);
			message.setContent(_multipart);
		}

        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
		Transport.send(message);

	}

	public synchronized void sendMail(String subject, String body,
			String sender, String recipients, String cc, String fileName)
			throws Exception {
		Log.d(TAG, "sendMail[" + subject + "]["+body+"]["+sender+"][to: "+recipients+"][cc: "+cc+"][fileName: "+fileName+"]");
		MimeMessage message = new MimeMessage(session);
		DataHandler handler = new DataHandler(new ByteArrayDataSource(
				body.getBytes(), "text/plain"));
		message.setSender(new InternetAddress(sender));
		message.setFrom(new InternetAddress(sender));
		message.setSubject(subject);
		message.setDataHandler(handler);
		if (recipients.indexOf(',') > 0)
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(recipients));
		else
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(
					recipients));
		if (cc != null && cc.trim().length()>0) {
			if (cc.indexOf(',') > 0)
				message.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(cc));
			else
				message.setRecipient(Message.RecipientType.CC,
						new InternetAddress(cc));
		}
		if (fileName != null) {
			addAttachment(fileName, body);
			message.setContent(_multipart);
		}
		Transport.send(message);

	}

	public class ByteArrayDataSource implements DataSource {
		private byte[] data;
		private String type;

		public ByteArrayDataSource(byte[] data, String type) {
			super();
			this.data = data;
			this.type = type;
		}

		public ByteArrayDataSource(byte[] data) {
			super();
			this.data = data;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getContentType() {
			if (type == null)
				return "application/octet-stream";
			else
				return type;
		}

		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}

		public String getName() {
			return "ByteArrayDataSource";
		}

		public OutputStream getOutputStream() throws IOException {
			throw new IOException("Not Supported");
		}
	}

	public void addAttachment(String filename, String content) throws Exception {
		BodyPart messageBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(filename);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename);
		_multipart.addBodyPart(messageBodyPart);

		BodyPart messageBodyPart2 = new MimeBodyPart();
		messageBodyPart2.setText(content);

		_multipart.addBodyPart(messageBodyPart2);
	}

}
