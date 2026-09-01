package com.example.spring_boot_project_api.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public final class QrCodeUtil {

    private static final int DEFAULT_SIZE = 300;

    private QrCodeUtil() {}

    public static String generateToken() {
        return "TKT-" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateBase64(String content) {
        return generateBase64(content, DEFAULT_SIZE);
    }

    public static String generateBase64(String content, int size) {
        try {
            BufferedImage image = generateImage(content, size);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    public static byte[] generatePngBytes(String content) {
        try {
            BufferedImage image = generateImage(content, DEFAULT_SIZE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code image: " + e.getMessage(), e);
        }
    }

    private static BufferedImage generateImage(String content, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        java.util.Map<EncodeHintType, Object> hints = new java.util.HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(matrix, new MatrixToImageConfig(
                MatrixToImageConfig.BLACK, MatrixToImageConfig.WHITE));
    }
}
