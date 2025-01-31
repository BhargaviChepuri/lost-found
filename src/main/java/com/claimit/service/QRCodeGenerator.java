package com.claimit.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

public class QRCodeGenerator {
	public static String generateQRCodeBase64(String uniqueId, String itemName, String itemStatus, int width,
			int height) {
		try {
			// Convert item details to JSON format
			String qrContent = "{\"uniqueId\":\"" + uniqueId + "\",\"itemName\":\"" + itemName + "\",\"itemStatus\":\""
					+ itemStatus + "\"}";

			// Generate QR code
			BitMatrix bitMatrix = new MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, width, height);
			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

			// Convert QR image to Base64
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ImageIO.write(qrImage, "png", outputStream);
			String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());

			if (base64Image.isEmpty()) {
				System.out.println("⚠️ QR Code Base64 string is empty!");
			}

			return base64Image;
		} catch (Exception e) {
			System.out.println("❌ QR Code Generation Failed: " + e.getMessage());
			e.printStackTrace();
			return null; // Ensure it does not return a broken image
		}
	}
}
