package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for sending email notifications.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Service
public class MailsService {

	private static final Logger log = LoggerFactory.getLogger(MailsService.class);

	public static final String REQUEST_CREATED = "REQUEST_CREATED";
	public static final String REQUEST_MODIFIED = "REQUEST_MODIFIED";
	public static final String REQUEST_STATUS_UPDATED = "REQUEST_STATUS_UPDATED";
	public static final String REQUEST_SIGNED = "REQUEST_SIGNED";

	private static final String NEW_LINE = "<br/>";

	private static final String ADMINS_MAILS = "appAdmin.emails";
	private static final String HOST_KEY = "host";
	private static final String FROM_KEY = "from";

	private static final String PRODUCTION_AUTHORITIES_MESSAGE_KEY = "production.authorities.message";
	private static final String PRODUCTION_AUTHORITIES_SUBJECT_KEY = "production.authorities.subject";

	private static final String ADD_ADMIN_SUBJECT_KEY = "admins.add.subject";
	private static final String ADD_ADMIN_MESSAGE_KEY = "admins.add.message";

	private static final String FOOTER_KEY = "footer";

	private static final String REQUEST_ID_FIELD = "%REQUEST_ID%";
	private static final String NEW_STATUS_FIELD = "%NEW_STATUS%";
	private static final String SERVICE_NAME_FIELD = "%SERVICE_NAME%";
	private static final String APPROVAL_LINK_FIELD = "%APPROVAL_LINK%";
	private static final String SERVICE_DESCRIPTION_FIELD = "%SERVICE_DESCRIPTION%";
	private static final String REQUEST_DETAIL_LINK_FIELD = "%REQUEST_DETAIL_LINK%";

	@Autowired
	private Properties messagesProperties;

	@Autowired
	private AppConfig appConfig;

	@Value("${host.url}")
	private String hostUrl;

	public void notifyAuthorities(Request req, Map<String, String> authoritiesLinksMap) {
		for (String email: authoritiesLinksMap.keySet()) {
			String link = authoritiesLinksMap.get(email);
			if (!authoritiesApproveProductionTransferNotify(link, req, email)) {
				log.warn("Failed to send approval notification to {} for req id: {}, link: {}",
						email, req.getReqId(), link);
			}
		}
	}

	public boolean notifyNewAdmins(Facility facility, Map<String, String> adminsLinksMap) {
		for (String email: adminsLinksMap.keySet()) {
			String link = adminsLinksMap.get(email);
			if (!adminAddRemoveNotify(link, facility, email)) {
				log.warn("Failed to send approval notification to {} for facility id: {}, link: {}",
						email, facility.getId(), link);
			}
		}

		return true;
	}

	public boolean authoritiesApproveProductionTransferNotify(String approvalLink, Request req, String recipient) {
		log.debug("authoritiesApproveProductionTransferNotify(approvalLink: {}, req: {}, recipient: {})",
				approvalLink, req, recipient);

		String subject = messagesProperties.getProperty(PRODUCTION_AUTHORITIES_SUBJECT_KEY);
		subject = replacePlaceholders(subject, req);

		String message = messagesProperties.getProperty(PRODUCTION_AUTHORITIES_MESSAGE_KEY);
		message = replacePlaceholders(message, req);
		message = replaceApprovalLink(message, approvalLink);

		boolean res = sendMail(subject, message, recipient);
		log.debug("authoritiesApproveProductionTransferNotify() returns: {}", res);
		return res;
	}

	public boolean adminAddRemoveNotify(String approvalLink, Facility facility,  String recipient) {
		log.debug("authoritiesApproveProductionTransferNotify(approvalLink: {}, facility: {}, recipient: {})",
				approvalLink, facility, recipient);

		String subject = messagesProperties.getProperty(ADD_ADMIN_SUBJECT_KEY);
		subject = replacePlaceholders(subject, facility);

		String message = messagesProperties.getProperty(ADD_ADMIN_MESSAGE_KEY);
		message = replacePlaceholders(message, facility);
		message = replaceApprovalLink(message, approvalLink);

		boolean res = sendMail(subject, message, recipient);
		log.debug("authoritiesApproveProductionTransferNotify() returns: {}", res);
		return res;
	}

	private boolean sendMail(String host, String from, String to, String subject, String msg) {
		log.debug("sendMail(host: {}, from: {}, to: {}, subject: {}, msg: {})", host, from, to, subject, msg);
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		Session session = Session.getDefaultInstance(props);
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setContent(msg, "text/html; charset=utf-8" );
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(msg, "text/html");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);
			message.setContent(multipart);

			log.debug("sending message");
			Transport.send(message);
		} catch (MessagingException e) {
			log.debug("sendMail() returns: FALSE");
			return false;
		}

		log.debug("sendMail() returns: TRUE");
		return true;
	}

	public void notifyUser(Request req, String action) {
		String subject = getSubject(action, "USER");
		String message = getMessage(action, "USER");

		subject = replacePlaceholders(subject, req);
		message = replacePlaceholders(message, req);

		String userMail = req.getAdminContact(appConfig.getAdminsAttributeName());

		boolean res = sendMail(subject, message, userMail);
		if (!res) {
			log.warn("Failed to send notification ({}, {}) to {}", subject, message, userMail);
		}
	}

	public void notifyAppAdmins(Request req, String action) {
		String[] appAdminsMails = messagesProperties.getProperty(ADMINS_MAILS).split(",");

		String subject = getSubject(action, "ADMIN");
		String message = getMessage(action, "ADMIN");

		subject = replacePlaceholders(subject, req);
		message = replacePlaceholders(message, req);

		for (String adminMail: appAdminsMails) {
			if (sendMail(subject, message, adminMail)) {
				log.trace("Sent mail to admin: {}", adminMail);
			} else {
				log.warn("Failed to send admin notification to: {}", adminMail);
			}
		}
	}

	private String replaceApprovalLink(String containerString, String link) {
		if (containerString.contains(APPROVAL_LINK_FIELD)) {
			return containerString.replaceAll(APPROVAL_LINK_FIELD, link);
		}

		return containerString;
	}

	private String replacePlaceholders(String containerString, Facility fac) {
		containerString = replacePlaceholder(containerString, SERVICE_NAME_FIELD, fac.getName());
		containerString = replacePlaceholder(containerString, SERVICE_DESCRIPTION_FIELD, fac.getDescription());

		return containerString;
	}

	private String replacePlaceholders(String containerString, Request req) {
		containerString = replacePlaceholder(containerString, REQUEST_ID_FIELD, req.getReqId().toString());
		containerString = replacePlaceholder(containerString, NEW_STATUS_FIELD, req.getStatus().toString());
		containerString = replacePlaceholder(containerString, SERVICE_NAME_FIELD, req.getFacilityName());
		containerString = replacePlaceholder(containerString, SERVICE_DESCRIPTION_FIELD, req.getFacilityDescription());
		containerString = replacePlaceholder(containerString, REQUEST_DETAIL_LINK_FIELD, hostUrl + "/auth/requests/detail/" + req.getReqId());

		return containerString;
	}

	private String replacePlaceholder(String container, String replaceKey, String replaceWith) {
		if (container.contains(replaceKey)) {
			return container.replaceAll(replaceKey, replaceWith);
		}

		return container;
	}

	private boolean sendMail(String subject, String message, String userMail) {
		String host = messagesProperties.getProperty(HOST_KEY);
		String from = messagesProperties.getProperty(FROM_KEY);

		message = message.concat(NEW_LINE).concat(messagesProperties.getProperty(FOOTER_KEY));

		return sendMail(host, from, userMail, subject, message);
	}

	private String getSubject(String action, String role) {
		return messagesProperties.getProperty(getPropertyPrefix(action) + '.' + role.toLowerCase() + '.' + "subject");
	}

	private String getMessage(String action, String role) {
		return messagesProperties.getProperty(getPropertyPrefix(action) + '.' + role.toLowerCase() + '.' + "message");
	}

	private String getPropertyPrefix(String action) {
		switch (action) {
			case REQUEST_CREATED:
				return "create";
			case REQUEST_MODIFIED:
				return "update";
			case REQUEST_STATUS_UPDATED:
				return "status_updated";
			case REQUEST_SIGNED:
				return "signed";
			default:
				return "";
		}
	}
}