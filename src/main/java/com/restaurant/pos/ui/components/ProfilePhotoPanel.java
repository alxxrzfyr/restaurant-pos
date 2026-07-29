package com.restaurant.pos.ui.components;

import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import com.restaurant.pos.util.ImageStorage;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public final class ProfilePhotoPanel extends JPanel {

    private static final int AVATAR_SIZE = 44;
    private static final Color AVATAR_BG = new Color(239, 246, 255);
    private static final Color AVATAR_BORDER = new Color(226, 232, 240);
    private static final Color AVATAR_TEXT = new Color(37, 99, 235);

    private String currentPhotoPath;
    private String displayName;
    private BufferedImage currentImage;
    private boolean editable;
    private Consumer<String> onPhotoChanged;

    public ProfilePhotoPanel(String initialPhotoPath, String displayName, boolean editable) {
        super(null);
        this.displayName = displayName;
        this.editable = editable;
        setOpaque(false);
        setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        setMinimumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        setMaximumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        loadImage(initialPhotoPath);
        if (editable) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Click to upload profile photo");
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    pickPhoto();
                }
            });
        }
    }

    public ProfilePhotoPanel(String initialPhotoPath, String displayName) {
        this(initialPhotoPath, displayName, false);
    }

    public ProfilePhotoPanel(String initialPhotoPath) {
        this(initialPhotoPath, null, false);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        repaint();
    }

    public void setOnPhotoChanged(Consumer<String> callback) {
        this.onPhotoChanged = callback;
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    public void setPhoto(String photoPath) {
        loadImage(photoPath);
        repaint();
    }

    private void loadImage(String path) {
        this.currentPhotoPath = path;
        this.currentImage = null;
        if (path != null && !path.isBlank()) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                try {
                    BufferedImage raw = ImageIO.read(f);
                    if (raw != null) {
                        this.currentImage = raw;
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void pickPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Upload Profile Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF, WEBP)", "jpg", "jpeg", "png", "gif", "webp"));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File chosen = chooser.getSelectedFile();
            try {
                String savedPath = ImageStorage.saveProfilePhoto(chosen);
                if (savedPath != null) {
                    loadImage(savedPath);
                    repaint();
                    if (onPhotoChanged != null) {
                        onPhotoChanged.accept(savedPath);
                    }
                }
            } catch (IOException ex) {
                System.err.println("Error saving profile photo: " + ex.getMessage());
            }
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h);
        int x = (w - size) / 2;
        int y = (h - size) / 2;

        Ellipse2D clip = new Ellipse2D.Float(x, y, size, size);
        g2.setClip(clip);

        if (currentImage != null) {
            int imgW = currentImage.getWidth();
            int imgH = currentImage.getHeight();
            double scale = Math.max((double) size / imgW, (double) size / imgH);
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);
            int drawX = x + (size - drawW) / 2;
            int drawY = y + (size - drawH) / 2;
            g2.drawImage(currentImage, drawX, drawY, drawW, drawH, null);
        } else {
            g2.setColor(AVATAR_BG);
            g2.fillOval(x, y, size, size);

            g2.setClip(null);
            String initials = getInitials(displayName);
            g2.setColor(AVATAR_TEXT);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(initials)) / 2;
            int ty = y + ((size - fm.getHeight()) / 2) + fm.getAscent() - 1;
            g2.drawString(initials, tx, ty);
        }

        g2.setClip(null);
        g2.setColor(AVATAR_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x + 1, y + 1, size - 2, size - 2);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(AVATAR_SIZE, AVATAR_SIZE);
    }
}
