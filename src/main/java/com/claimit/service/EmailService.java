
package com.claimit.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.claimit.dto.ItemStatus;
import com.claimit.entity.Items;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	/**
	 * Send an email using Spring's JavaMailSender.
	 */
	public void sendEmail(String to, String subject, String body) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, true);

			mailSender.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to send email.");
		}
	}

	public void sendClaimConfirmationEmail(String userEmail, Items item) {
		String subject = "Item Claim Confirmation";

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedExpirationDate = dateFormat.format(item.getExpirationDate());

		String message = buildEmailTemplate("Dear " + item.getUser().getUserName() + ",\n\nYour claim for item "
				+ item.getItemName() + " has been successful. " + ".", item.getStatus(),
				formattedExpirationDate.toString());

		sendEmail(userEmail, subject, message);
	}

// Send claim notification to admin
	public void sendClaimNotificationToAdmin(Items item) {
		String adminEmail = "bineetha5246@gmail.com";
		String subject = "New Item Claimed";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedExpirationDate = dateFormat.format(item.getExpirationDate());
		String message = "The item <strong>" + item.getItemName() + "</strong> has been claimed by " + "<strong>"
				+ item.getUser().getUserName() + " (" + item.getUser().getEmail() + ")</strong>.";
		String emailContent = buildEmailTemplateForClaimNotification(message, ItemStatus.PENDING_APPROVAL,
				formattedExpirationDate.toString(), item.getReceivedDate());

		sendEmail(adminEmail, subject, emailContent);

	}

	public void sendExpirationReminder(String userEmail, Items item, int days) {
		String subject = "Reminder: Your item is about to expire!";

		String message;
		if (days == 30) {
			message = "Dear " + item.getUser() + ",<br><br>" + "Your claim for item <strong>" + item.getItemName()
					+ "</strong> will expire soon on <strong>" + item.getExpirationDate()
					+ "</strong>. Please pick it up before then.";
		} else if (days == 3) {
			message = "Dear " + item.getUser() + ",<br><br>" + "Your claim for item <strong>" + item.getItemName()
					+ "</strong> will expire in <strong>30 days</strong> on <strong>" + item.getExpirationDate()
					+ "</strong>. Please pick it up before the expiration date.";
		} else {
			message = "Dear " + item.getUser() + ",<br><br>" + "Your claim for item <strong>" + item.getItemName()
					+ "</strong> will expire on <strong>" + item.getExpirationDate() + "</strong>.";
		}

		String emailContent = buildEmailTemplate(message, ItemStatus.EXPIRING_SOON,
				item.getExpirationDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

		sendEmail(userEmail, subject, emailContent);
	}

	// Send archived item notification to admin
	public void sendArchivedNotificationToAdmin(Items item) {
		String adminEmail = "bineetha5246@gmail.com";
		String subject = "Archived Item Notification";

		String message = "The item " + item.getItemName()
				+ " has been removed or archived due to the expiration date being over.";

		ItemStatus status = ItemStatus.ARCHIVED;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedExpirationDate = dateFormat.format(item.getExpirationDate());

		String emailBody = buildEmailTemplate(message, status, formattedExpirationDate);

		sendEmail(adminEmail, subject, emailBody);
	}

	// Send archived item notification to the user
	public void sendArchivedNotificationToUser(Items item) {
		String userEmail = item.getUser().getEmail();
		String subject = " Item Has Been Archived";
		String message = "Dear User, \n\nThe item '" + item.getItemName()
				+ "' has been archived due to the expiration date being over."
				+ "If you have any questions, please contact support.";

		ItemStatus status = ItemStatus.ARCHIVED;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedExpirationDate = dateFormat.format(item.getExpirationDate());

		String emailBody = buildEmailTemplate(message, status, formattedExpirationDate);

		sendEmail(userEmail, subject, emailBody);
	}

	// Send archived item notification to the user
	private String buildEmailTemplate(String message, ItemStatus status, String expirationDate) {
		return "<!DOCTYPE html>" + "<html lang=\"en\">" + "<head>" + "<meta charset=\"UTF-8\">"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" + "<style>"
				+ "body { margin: 0; padding: 0; }" + ".email-container {" + "    width: 100%;"
				+ "    max-width: 600px;" + "    margin: 0 auto;" + "    background-color: #ffffff;"
				+ "    border: 1px solid #dddddd;" + "}" + ".email-header {" + "    background-color: #2e86c1;"
				+ "    color: white;" + "    text-align: center;" + "    padding: 10px;" + "} "
				+ ".email-body { padding: 20px; font-family: Arial, sans-serif; }"
				+ ".email-body p { color: #333333; font-size: 16px; line-height: 1.5; }" + ".email-footer {"
				+ "    background-color: #2e86c1;" + "    color: white;" + "    text-align: center;"
				+ "    padding: 10px;" + "    font-size: 12px;" + "}" + "table {" + "    width: 100%;"
				+ "    border-collapse: collapse;" + "    margin-top: 20px;" + "}" + "th, td {"
				+ "    border: 1px solid #dddddd;" + "    padding: 10px;" + "    text-align: center;" + "} </style>"
				+ "</head>" + "<body>" + "    <div class=\"email-container\">" + "        <div class=\"email-header\">"
				+ "            <h2>Claim IT  Notification</h2>" + "        </div>"
				+ "        <div class=\"email-body\">" + "            <p>" + message + "</p>" + "            <table>"
				+ "                <thead>" + "                    <tr>" + "                        <th>Status</th>"
				+ "                        <th>Expiration Date</th>" + "                    </tr>"
				+ "                </thead>" + "                <tbody>" + "                    <tr>"
				+ "                        <td>" + status + "</td>" + "                        <td>" + expirationDate
				+ "</td>" + "                    </tr>" + "                </tbody>" + "            </table>"
				+ "        </div>" + "        <div class=\"email-footer\">"
				+ "            <p>&copy; 2024 ClaimIt. All rights reserved.</p>" + "        </div>" + "    </div>"
				+ "</body>" + "</html>";
	}

	private String buildEmailTemplateForClaimNotification(String message, ItemStatus status, String expirationDate,
			Date receivedDate) {
		return "<!DOCTYPE html>" + "<html lang=\"en\">" + "<head>" + "<meta charset=\"UTF-8\">"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" + "<style>"
				+ "body { margin: 0; padding: 0; }" + ".email-container {" + "    width: 100%;"
				+ "    max-width: 600px;" + "    margin: 0 auto;" + "    background-color: #ffffff;"
				+ "    border: 1px solid #dddddd;" + "}" + ".email-header {" + "    background-color: #2e86c1;"
				+ "    color: white;" + "    text-align: center;" + "    padding: 10px;" + "}"
				+ ".email-body { padding: 20px; font-family: Arial, sans-serif; }"
				+ ".email-body p { color: #333333; font-size: 16px; line-height: 1.5; }" + ".email-footer {"
				+ "    background-color: #2e86c1;" + "    color: white;" + "    text-align: center;"
				+ "    padding: 10px;" + "    font-size: 12px;" + "}" + "table {" + "    width: 100%;"
				+ "    border-collapse: collapse;" + "    margin-top: 20px;" + "}" + "th, td {"
				+ "    border: 1px solid #dddddd;" + "    padding: 10px;" + "    text-align: center;" + "}" + "</style>"
				+ "</head>" + "<body>" + "    <div class=\"email-container\">" + "        <div class=\"email-header\">"
				+ "            <h2>Claim IT  Notification</h2>" + "        </div>"
				+ "        <div class=\"email-body\">" + "            <p>" + message + "</p>" + "            <table>"
				+ "                <thead>" + "                    <tr>" + "                        <th>Status</th>"
				+ "                        <th>expirationDate</th>" + "                    </tr>"
				+ "                </thead>" + "                <tbody>" + "                    <tr>"
				+ "                        <td>" + status + "</td>" + "                        <td>" + expirationDate
				+ "</td>" + "                    </tr>" + "                </tbody>" + "            </table>"
				+ "        </div>" + "        <div class=\"email-footer\">"
				+ "            <p>&copy; 2024 ClaimIt. All rights reserved.</p>" + "        </div>" + "    </div>"
				+ "</body>" + "</html>";
	}

	public String buildEmailTemplate(String message, ItemStatus status, LocalDateTime expirationDateTime) {
		StringBuilder emailTemplate = new StringBuilder();

		emailTemplate.append("<html>").append("<head><style>")
				.append("body { font-family: Arial, sans-serif; font-size: 14px; color: #333; }")
				.append("strong { color: #1a73e8; }")
				.append(".status { font-weight: bold; padding: 10px; margin: 10px 0; border-radius: 5px; }")
				.append(".expiring-soon { background-color: #f8d7da; color: #721c24; }")
				.append(".archived { background-color: #d1ecf1; color: #0c5460; }").append("</style></head>")
				.append("<body>").append("<h2>Item Expiration Reminder</h2>");

		emailTemplate.append("<div class=\"status ");
		if (status == ItemStatus.EXPIRING_SOON) {
			emailTemplate.append("expiring-soon");
		} else if (status == ItemStatus.ARCHIVED) {
			emailTemplate.append("archived");
		}
		emailTemplate.append("\">").append(message).append("</div>");

		if (expirationDateTime != null) {
			emailTemplate.append("<p><strong>Expiration Date:</strong> "
					+ expirationDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "</p>");
		}

		emailTemplate.append("<p>If you have any questions, please contact support.</p>").append("</body>")
				.append("</html>");

		return emailTemplate.toString();
	}

	public void sendEmailWithQRCode(String toEmail, String uniqueId, String itemName, String itemStatus) {
		try {
			// Generate QR code with item details
			String qrCodeBase64 = QRCodeGenerator.generateQRCodeBase64(uniqueId, itemName, itemStatus, 200, 200);

			if (qrCodeBase64 == null || qrCodeBase64.isEmpty()) {
			    System.out.println("❌ Failed to generate QR Code. Email will be sent without QR.");
			    return;  // Stop sending email if QR code generation failed
			}
			
			String qrCodeImage = "data:image/png;base64," + qrCodeBase64;

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setTo(toEmail);
			helper.setSubject("Claim Your Item");
			helper.setText("<h3>Scan to View Item Details</h3>"
			    + "<p>Scan the QR code below:</p>"
			    + "<img src='" + qrCodeImage + "' alt='QR Code'/>", true);

			mailSender.send(message);
			System.out.println("✅ Email sent with QR Code!");

		} catch (Exception e) {
			throw new RuntimeException("Error while sending email with QR Code", e);
		}
	}

}
