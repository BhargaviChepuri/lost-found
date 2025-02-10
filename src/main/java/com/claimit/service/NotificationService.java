package com.claimit.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.claimit.dto.ItemStatus;
import com.claimit.entity.ClaimHistory;
import com.claimit.entity.Items;
import com.claimit.entity.User;
import com.claimit.repo.ItemsRepo;
import com.claimit.repo.UserRepo;

@Service
@Transactional
public class NotificationService {

	@Autowired
	private EmailService emailService;

	@Autowired
	private ItemsRepo itemsRepo;

	@Autowired
	private UserRepo userRepo;

	/**
	 * Handle notification logic when the status changes.
	 */

	public void handleStatusChangeNotification(ClaimHistory claimHistory) {
		String userEmail = claimHistory.getUser().getEmail();
		String adminEmail = "bineetha5246@gmail.com";
		ItemStatus status = claimHistory.getClaimStatus();
		LocalDateTime claimDate = claimHistory.getClaimDate();
		String message = buildMessage(claimHistory);
		if (userEmail.equals(claimHistory.getUser().getEmail())) {
			emailService.sendEmail(userEmail, "Claim IT - Status Update",
					buildEmailTemplate(message, status, claimDate));
		}
		emailService.sendEmail(adminEmail, "Claim IT - Status Update for Claim " + claimHistory.getClaimId(),
				buildEmailTemplate(message, status, claimDate));
	}

	/**
	 * Build the notification message based on the status.
	 */
	private String buildMessage(ClaimHistory claimHistory) {
		switch (claimHistory.getClaimStatus()) {
		case UNCLAIMED:
			return "Your item is now unclaimed.";
		case PENDING_APPROVAL:
			return "Your item is in pending approval.So, Please wait for it";
		case PENDING_PICKUP:
			return "Your item is ready for pickup. Please collect it.";
		case CLAIMED:
			return "Your item has been successfully claimed. Thank you!";
		default:
			return "Your claim status has been updated.";
		}
	}

	/**
	 * Build the email template.
	 */
	private String buildEmailTemplate(String message, ItemStatus status, LocalDateTime claimDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formattedDate = claimDate.format(formatter); // Format the claimDate

		return "<!DOCTYPE html>" + "<html lang=\"en\">" + "<head>" + "<meta charset=\"UTF-8\">"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" + "<style>"
				+ "body { margin: 0; padding: 0; }" + ".email-container {" + "    width: 100%;"
				+ "    max-width: 600px;" + "    margin: 0 auto;" + "    background-color: #ffffff;"
				+ "    border: 1px solid #dddddd;" + "}" + ".email-header {" + "    background-color: #2e86c1;"
				+ "    color: #000000;" + "    padding: 10px 20px;" + "    display: flex;"
				+ "    justify-content: space-between;" + "    align-items: center;" + "}" + ".email-header img {"
				+ "    max-width: 100px;" + "    height: auto;" + "}" + ".email-header h2 {" + "    margin: 0;"
				+ "    font-size: 30px;" + "    color: white;" + "    text-align: center;" + "    padding: 10px;"
				+ "    flex-grow: 1;" + "}" + ".email-body { padding: 20px; }"
				+ ".email-body p { color: #333333; font-size: 16px; line-height: 1.5; }" + ".email-footer {"
				+ "    background-color: #2e86c1;" + "    color: white;" + "    text-align: center;"
				+ "    padding: 10px;" + "    font-size: 12px;" + "}" + "table {" + "    width: 100%;"
				+ "    border-collapse: collapse;" + "    margin-top: 20px;" + "    font-size: 16px;" + "}" + "th, td {"
				+ "    border: 1px solid #dddddd;" + "    padding: 9px;" + "    text-align: center;"
				+ "    color: #333;" + "}" + "th {" + "    background-color: #f4f4f4;" + "}" + "tr:nth-child(even) {"
				+ "    background-color: #f9f9f9;" + "}" + "</style>" + "</head>" + "<body>"
				+ "    <div class=\"email-container\">" + "        <div class=\"email-header\">"
				+ "            <h2>Thank you for reaching out!</h2>" + "        </div>"
				+ "        <div class=\"email-body\">" + "            <p>Dear User,</p>" + "            <table>"
				+ "                <thead>" + "                    <tr>" + "                        <th>Message</th>"
				+ "                        <th>Status</th>" + "                        <th>Date</th>"
				+ "                    </tr>" + "                </thead>" + "                <tbody>"
				+ "                    <tr>"
				+ "                        <td style=\"padding: 10px; line-height: 1.6; font-size: 14px; color: #333;\">"
				+ "                            <p>" + message + "</p>" + "                        </td>"
				+ "                        <td style=\"padding: 10px; text-align: center; color: #333;\">" + status
				+ "                        </td>"
				+ "                        <td style=\"padding: 10px; text-align: center; color: #333;\">"
				+ formattedDate + "                        </td>" + "                    </tr>"
				+ "                </tbody>" + "            </table>" + "        </div>"
				+ "        <div class=\"email-footer\">"
				+ "            <p>&copy; 2024 Claim IT. All rights reserved.</p>" + "        </div>" + "    </div>"
				+ "</body>" + "</html>";
	}

	private String generateHtmlTemplate(Items item, String message) {
		return String.format("<!DOCTYPE html>" + "<html>" + "<head>" + "    <style>"
				+ "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; }"
				+ "        .container { width: 100%%; max-width: 600px; margin: 20px auto; border: 1px solid #ddd; padding: 20px; }"
				+ "        .header { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 10px; }"
				+ "        table { width: 100%%; border-collapse: collapse; margin-top: 20px; }"
				+ "        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }"
				+ "        th { background-color: #f4f4f4; }" + "    </style>" + "</head>" + "<body>"
				+ "    <div class='container'>" + "        <div class='header'>Item Expiration Notification</div>"
				+ "        <p>Dear User,</p>" + "        <p>The following item will be archived/removed in %d days:</p>"
				+ "        <table>" + "            <tr><th>Item Name</th><td>%s</td></tr>"
				+ "            <tr><th>Expiration Date</th><td>%s</td></tr>" + "        </table>"
				+ "        <p>Please take the necessary action before the expiration date.</p>"
				+ "        <p>Thank you,<br>ClaimItHub Team</p>" + "    </div>" + "</body>" + "</html>", message,
				item.getItemName(), item.getExpirationDate().toString());
	}

	/**
	 * Notifies users about item expiration and saves the item with expiration
	 * logic.
	 * 
	 * This method checks all items in the system and calculates whether they are
	 * close to expiration. If the item is nearing expiration (30, 10, 2, or 1 day
	 * left), a notification is sent to the associated users, and the item is saved
	 * with updated expiration logic.
	 * 
	 * Subclasses that override this method must ensure the item expiration logic is
	 * respected, and notifications are appropriately sent.
	 */
	public void notifyAndSaveItem() {
		List<Items> items = itemsRepo.findAll();
		LocalDateTime now = LocalDateTime.now();
		for (Items item : items) {
			if (item.getReceivedDate() != null) {
				LocalDateTime foundDateTime = item.getReceivedDate().toInstant().atZone(ZoneId.systemDefault())
						.toLocalDateTime();
				LocalDateTime expirationDate = foundDateTime.plusDays(30);
				item.setExpirationDate(Date.from(expirationDate.atZone(ZoneId.systemDefault()).toInstant()));
				long daysLeft = ChronoUnit.DAYS.between(now, expirationDate);
				if (daysLeft == 30 || daysLeft == 10 || daysLeft == 2 || daysLeft == 1) {
					String message = generateUserNotificationMessage(daysLeft, item);
					saveItemWithExpirationLogic(item);
					sendNotifications(item, message);
				}
			}
		}
	}

	private void saveItemWithExpirationLogic(Items item) {
		Items savedItem = itemsRepo.save(item);
		if (savedItem == null) {
			System.out.println("Data not saved");
		}
	}

	private void sendNotifications(Items item, String message) {
		List<User> users = userRepo.findByItemsItemId(item.getItemId());

		List<String> userEmails = users.stream().map(User::getEmail).collect(Collectors.toList());
		System.out.println("User emails: " + userEmails);

		for (String email : userEmails) {
			emailService.sendEmail(email, "Item Expiration Notification", generateHtmlTemplate(item, message));
		}
	}

	private String generateUserNotificationMessage(long daysLeft, Items item) {
		return String.format("Your item '%s' is %d days away from expiration. Please take action before it's too late!",
				item.getItemName(), daysLeft);
	}

}
