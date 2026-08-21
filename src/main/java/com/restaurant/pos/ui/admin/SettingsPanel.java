package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.BusinessSettings;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;

public final class SettingsPanel extends JPanel {

    private final AppContext context;
    private final User currentUser;

    private final JTextField businessNameField = new JTextField(20);
    private final JTextField branchNameField   = new JTextField(20);
    private final JTextField addressField      = new JTextField(20);
    private final JTextField phoneField        = new JTextField(20);
    private final JTextField tinField          = new JTextField(20);
    private final JTextField vatRegNoField     = new JTextField(20);
    private final JTextField birPermitNoField  = new JTextField(20);
    private final JTextField posSerialNoField  = new JTextField(20);
    private final JTextField machineNoField    = new JTextField(20);
    private final JTextField vatRateField      = new JTextField(10);

    private final JComboBox<String> printerTypeCombo = new JComboBox<>(new String[]{"Standard Printer", "Thermal Receipt Printer"});
    private final JComboBox<String> availablePrintersCombo = new JComboBox<>();

    public SettingsPanel(AppContext context, User currentUser) {
        super(new BorderLayout(0, 16));
        this.context = context;
        this.currentUser = currentUser;

        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeaderBar(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);

        loadSettings();
        populatePrinters();
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Application Settings & Configuration");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Configure business profile, BIR tax compliance, hardware devices, and database backups");
        subtitle.setFont(AppTheme.captionFont());
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);

        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, "growx");

        JButton saveBtn = new JButton("Save Settings");
        saveBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        saveBtn.setIcon(Icons.check(Color.WHITE, 16));
        saveBtn.setIconTextGap(8);
        saveBtn.setBackground(AppTheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        saveBtn.addActionListener(e -> onSaveSettings());

        header.add(saveBtn, "h 38!");
        return header;
    }

    private JPanel buildMainContent() {
        JPanel container = new JPanel(new MigLayout("insets 0, fill", "[grow 55, fill]16[grow 45, fill]", "[fill]"));
        container.setOpaque(false);

        container.add(buildBusinessProfileCard(), "grow");
        container.add(buildRightColumn(), "grow");

        return container;
    }

    private JPanel buildBusinessProfileCard() {
        JPanel card = new JPanel(new MigLayout("insets 16 18 16 18, fillx, wrap 2", "[grow, fill]14[grow, fill]", "[]12[]2[]10[]2[]10[]2[]10[]2[]10[]2[]10[]2[]"));
        card.setBackground(AppTheme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel cardHeader = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("Business Profile & Tax Compliance");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Official entity registration details displayed on customer receipts");
        sub.setFont(AppTheme.captionFont());
        sub.setForeground(AppTheme.TEXT_MUTED);
        cardHeader.add(title);
        cardHeader.add(sub);
        card.add(cardHeader, "span 2, gapbottom 4");

        card.add(createFieldGroup("Restaurant Name", businessNameField));
        card.add(createFieldGroup("Branch Name (Optional)", branchNameField));

        card.add(createFieldGroup("Store Address", addressField), "span 2");

        card.add(createFieldGroup("Contact Phone", phoneField));
        card.add(createFieldGroup("Taxpayer ID (TIN)", tinField));

        card.add(createFieldGroup("VAT Registration No", vatRegNoField));
        card.add(createFieldGroup("BIR Permit No", birPermitNoField));

        card.add(createFieldGroup("POS Serial No", posSerialNoField));
        card.add(createFieldGroup("Machine No (MIN)", machineNoField));

        card.add(createFieldGroup("VAT / Tax Rate (%)", vatRateField));

        return card;
    }

    private JPanel buildRightColumn() {
        JPanel rightCol = new JPanel(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]", "[grow, fill]16[grow, fill]"));
        rightCol.setOpaque(false);

        rightCol.add(buildPrinterCard(), "grow");
        rightCol.add(buildDatabaseCard(), "grow");

        return rightCol;
    }

    private JPanel buildPrinterCard() {
        JPanel card = new JPanel(new MigLayout("insets 16 18 16 18, fillx, wrap 1", "[grow, fill]", "[]12[]2[]10[]2[]"));
        card.setBackground(AppTheme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel cardHeader = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("Receipt Printer Setup");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Configure thermal ESC/POS or standard desktop printing devices");
        sub.setFont(AppTheme.captionFont());
        sub.setForeground(AppTheme.TEXT_MUTED);
        cardHeader.add(title);
        cardHeader.add(sub);
        card.add(cardHeader, "gapbottom 4");

        card.add(createFieldGroup("Printer Driver Mode", printerTypeCombo));
        card.add(createFieldGroup("Default Output Printer", availablePrintersCombo));

        return card;
    }

    private JPanel buildDatabaseCard() {
        JPanel card = new JPanel(new MigLayout("insets 16 18 16 18, fillx, wrap 1", "[grow, fill]", "[]12[]14[]"));
        card.setBackground(AppTheme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel cardHeader = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("Database Maintenance & Safety");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Create standalone database backups or restore archives");
        sub.setFont(AppTheme.captionFont());
        sub.setForeground(AppTheme.TEXT_MUTED);
        cardHeader.add(title);
        cardHeader.add(sub);
        card.add(cardHeader, "gapbottom 4");

        JLabel desc = new JLabel("<html>Create a point-in-time snapshot of sales data, user accounts, and inventory catalog. Backups are stored in the local storage directory.</html>");
        desc.setFont(AppTheme.bodyFont());
        desc.setForeground(AppTheme.TEXT_SECONDARY);
        card.add(desc, "gapbottom 4");

        JPanel actions = new JPanel(new MigLayout("insets 0", "[]10[]"));
        actions.setOpaque(false);

        JButton backupNowBtn = new JButton("Backup Database");
        backupNowBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        backupNowBtn.setIcon(Icons.database(AppTheme.TEXT_PRIMARY, 14));
        backupNowBtn.setIconTextGap(8);
        backupNowBtn.setBackground(AppTheme.CARD);
        backupNowBtn.setForeground(AppTheme.TEXT_PRIMARY);
        backupNowBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        backupNowBtn.setFocusPainted(false);
        backupNowBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backupNowBtn.addActionListener(e -> onBackupNow());

        JButton restoreBtn = new JButton("Restore Database...");
        restoreBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        restoreBtn.setIcon(Icons.download(AppTheme.WARNING, 14));
        restoreBtn.setIconTextGap(8);
        restoreBtn.setBackground(AppTheme.WARNING_BG);
        restoreBtn.setForeground(AppTheme.WARNING);
        restoreBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.WARNING_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        restoreBtn.setFocusPainted(false);
        restoreBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        restoreBtn.addActionListener(e -> onRestore());

        actions.add(backupNowBtn, "h 38!");
        actions.add(restoreBtn, "h 38!");
        card.add(actions);

        return card;
    }

    private JPanel createFieldGroup(String labelText, javax.swing.JComponent input) {
        JPanel group = new JPanel(new MigLayout("insets 0, fillx, wrap 1", "[grow, fill]", "[]3[]"));
        group.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        label.setForeground(AppTheme.TEXT_SECONDARY);
        group.add(label);

        input.setFont(AppTheme.bodyFont());
        group.add(input, "h 36!");

        return group;
    }

    private void loadSettings() {
        BusinessSettings settings = context.settingsService().load();
        businessNameField.setText(settings.businessName());
        branchNameField.setText(settings.branchName());
        addressField.setText(settings.address());
        phoneField.setText(settings.phone());
        tinField.setText(settings.tin());
        vatRegNoField.setText(settings.vatRegNo());
        birPermitNoField.setText(settings.birPermitNo());
        posSerialNoField.setText(settings.posSerialNo());
        machineNoField.setText(settings.machineNo());
        vatRateField.setText(settings.vatRatePercent().toPlainString());
    }

    private void populatePrinters() {
        availablePrintersCombo.removeAllItems();
        try {
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            if (services.length == 0) {
                availablePrintersCombo.addItem("No system printers found");
            } else {
                for (PrintService service : services) {
                    availablePrintersCombo.addItem(service.getName());
                }
            }
        } catch (Exception ex) {
            availablePrintersCombo.addItem("Default Printer");
        }
    }

    private void onSaveSettings() {
        String name = businessNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Business name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String vatStr = vatRateField.getText().trim();
        BigDecimal vatRate;
        try {
            vatRate = new BigDecimal(vatStr);
            if (vatRate.compareTo(BigDecimal.ZERO) < 0 || vatRate.compareTo(new BigDecimal("100")) > 0) {
                JOptionPane.showMessageDialog(this, "VAT rate must be between 0% and 100%.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid VAT rate format (e.g. 12.00).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BusinessSettings updated = BusinessSettings.builder()
                .businessName(name)
                .branchName(branchNameField.getText().trim())
                .address(addressField.getText().trim())
                .phone(phoneField.getText().trim())
                .tin(tinField.getText().trim())
                .vatRegNo(vatRegNoField.getText().trim())
                .birPermitNo(birPermitNoField.getText().trim())
                .posSerialNo(posSerialNoField.getText().trim())
                .machineNo(machineNoField.getText().trim())
                .vatRatePercent(vatRate)
                .build();

        try {
            context.settingsService().save(updated, currentUser.id(), currentUser.username());
            JOptionPane.showMessageDialog(this, "Settings saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onBackupNow() {
        try {
            Path backupPath = context.backupService().backupNow(currentUser.id(), currentUser.username());
            JOptionPane.showMessageDialog(this, "Backup created successfully at:\n" + backupPath.toAbsolutePath(),
                    "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage(), "Backup Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRestore() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Database Backup File to Restore");
        chooser.setFileFilter(new FileNameExtensionFilter("SQLite Database Backup (*.db)", "db"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File backupFile = chooser.getSelectedFile();

        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: Restoring from backup will OVERWRITE the current live database.\n" +
                "All unsaved changes will be lost.\n\n" +
                "Are you sure you want to restore from:\n" + backupFile.getAbsolutePath() + "?",
                "Confirm Database Restore",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            context.backupService().restore(backupFile.toPath(), currentUser.id(), currentUser.username());
            JOptionPane.showMessageDialog(this, "Database successfully restored from backup.\nSettings will re-load now.",
                    "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
            loadSettings();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Restore failed: " + ex.getMessage(), "Restore Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
