package com.restaurant.pos.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class ImageStorage {

    private static final Path BASE_DIR = Path.of(System.getProperty("user.home"), ".restaurant-pos", "data", "images");
    private static final Path ITEM_DIR = BASE_DIR.resolve("items");
    private static final Path PROFILE_DIR = BASE_DIR.resolve("profiles");

    static {
        try {
            Files.createDirectories(ITEM_DIR);
            Files.createDirectories(PROFILE_DIR);
        } catch (IOException e) {
            System.err.println("Failed to create image storage directories: " + e.getMessage());
        }
    }

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(".png", ".jpg", ".jpeg", ".webp", ".gif");

    private ImageStorage() {}

    public static String saveItemImage(File sourceFile) throws IOException {
        validateImageFile(sourceFile);
        String extension = getExtension(sourceFile.getName());
        String targetName = "item_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path targetPath = ITEM_DIR.resolve(targetName).normalize();
        if (!targetPath.startsWith(ITEM_DIR.normalize())) {
            throw new SecurityException("Invalid target path: outside storage directory.");
        }
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toAbsolutePath().toString();
    }

    public static String saveProfilePhoto(File sourceFile) throws IOException {
        validateImageFile(sourceFile);
        String extension = getExtension(sourceFile.getName());
        String targetName = "user_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path targetPath = PROFILE_DIR.resolve(targetName).normalize();
        if (!targetPath.startsWith(PROFILE_DIR.normalize())) {
            throw new SecurityException("Invalid target path: outside storage directory.");
        }
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toAbsolutePath().toString();
    }

    private static void validateImageFile(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            return;
        }
        if (sourceFile.length() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Image file exceeds maximum allowed size of 5 MB.");
        }
        String extension = getExtension(sourceFile.getName()).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported image format: " + extension + ". Allowed formats: " + ALLOWED_EXTENSIONS);
        }
    }

    private static String getExtension(String filename) {
        if (filename == null) return ".png";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".png";
    }

    public static java.awt.image.BufferedImage scaleCenterCrop(java.awt.image.BufferedImage src, int targetWidth, int targetHeight) {
        if (src == null || targetWidth <= 0 || targetHeight <= 0) {
            return src;
        }
        double srcAspect = (double) src.getWidth() / src.getHeight();
        double targetAspect = (double) targetWidth / targetHeight;
        int cropW, cropH;
        if (srcAspect > targetAspect) {
            cropH = src.getHeight();
            cropW = (int) Math.round(cropH * targetAspect);
        } else {
            cropW = src.getWidth();
            cropH = (int) Math.round(cropW / targetAspect);
        }
        int cropX = Math.max(0, (src.getWidth() - cropW) / 2);
        int cropY = Math.max(0, (src.getHeight() - cropH) / 2);
        cropW = Math.min(cropW, src.getWidth() - cropX);
        cropH = Math.min(cropH, src.getHeight() - cropY);

        java.awt.image.BufferedImage cropped = src.getSubimage(cropX, cropY, cropW, cropH);

        java.awt.image.BufferedImage dest = new java.awt.image.BufferedImage(targetWidth, targetHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = dest.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(cropped, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return dest;
    }
}
