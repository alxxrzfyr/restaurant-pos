package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.BusinessSettings;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;

public final class SettingsPanel extends JPanel {

    private final AppContext context;
    private final User currentUser;

    private final JTextField businessNameField = new JTextField(25);
    private final JTextField branchNameField = new JTextField(25);
    private final JTextField addressField = new JTextField(25);
    private final JTextField phoneField = new JTextField(25);
    private final JTextField tinField = new JTextField(25);
    private final JTextField vatRegNoField = new JTextField(25);
    private final JTextField birPermitNoField = new JTextField(25);
    private final JTextField posSerialNoField = new JTextField(25);
    private final JTextField machineNoField = new JTextField(25);
    private final JTextField vatRateField = new JTextField(10);

    private final JComboBox<String> printerTypeCombo = new JComboBox<>(new String[]{"Standard Printer", "Thermal Receipt Printer"});
    private final JComboBox<String> availablePrintersCombo = new JComboBox<>();

    public SettingsPanel(AppContext context, User currentUser) {
        super(new BorderLayout(0, 12));
        this.context = context;
        this.currentUser = currentUser;

        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeaderBar(), BorderLayout.NORTH);
        add(buildFormPane(), BorderLayout.CENTER);

        loadSettings();
        populatePrinters();
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        header.setOpaque(false);

        JLabel title = new JLabel("Application Settings");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title);

        JButton saveBtn = new JButton("Save Settings");
        saveBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        saveBtn.setBackground(AppTheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> onSaveSettings());

        header.add(saveBtn, "h 38!");
        return header;
    }

    private JScrollPane buildFormPane() {
        JPanel form = new JPanel(new MigLayout("fillx, insets 10, wrap 2", "[right, 180!][grow,fill]"));
        form.setOpaque(false);

        addSectionHeader(form, "Business Profile");

        form.add(createLabel("Restaurant Name:"));
        businessNameField.setFont(AppTheme.bodyFont());
        form.add(businessNameField, "h 36!");

        form.add(createLabel("Branch Name (optional):"));
        branchNameField.setFont(AppTheme.bodyFont());
        form.add(branchNameField, "h 36!");

        form.add(createLabel("Address:"));
        addressField.setFont(AppTheme.bodyFont());
        form.add(addressField, "h 36!");

        form.add(createLabel("Phone Number:"));
        phoneField.setFont(AppTheme.bodyFont());
        form.add(phoneField, "h 36!");

        form.add(createLabel("TIN:"));
        tinField.setFont(AppTheme.bodyFont());
        form.add(tinField, "h 36!");

        form.add(createLabel("VAT Reg No:"));
        vatRegNoField.setFont(AppTheme.bodyFont());
        form.add(vatRegNoField, "h 36!");

        form.add(createLabel("BIR Permit No:"));
        birPermitNoField.setFont(AppTheme.bodyFont());
        form.add(birPermitNoField, "h 36!");

        form.add(createLabel("POS Serial No:"));
        posSerialNoField.setFont(AppTheme.bodyFont());
        form.add(posSerialNoField, "h 36!");

        form.add(createLabel("Machine No (MIN):"));
        machineNoField.setFont(AppTheme.bodyFont());
        form.add(machineNoField, "h 36!");

        form.add(createLabel("Tax / VAT Rate (%):"));
        vatRateField.setFont(AppTheme.bodyFont());
        form.add(vatRateField, "h 36!");

        addSectionHeader(form, "Database Backup & Restore");

        JPanel backupBtnPanel = new JPanel(new MigLayout("insets 0", "[][grow]"));
        backupBtnPanel.setOpaque(false);

        JButton backupNowBtn = new JButton("Backup Now");
        backupNowBtn.setFont(AppTheme.bodyFont());
        backupNowBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backupNowBtn.addActionListener(e -> onBackupNow());

        JButton restoreBtn = new JButton("Restore Database...");
        restoreBtn.setFont(AppTheme.bodyFont());
        restoreBtn.setBackground(AppTheme.WARNING);
        restoreBtn.setForeground(Color.BLACK);
        restoreBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        restoreBtn.addActionListener(e -> onRestore());

        backupBtnPanel.add(backupNowBtn, "h 36!");
        backupBtnPanel.add(restoreBtn, "h 36!, gapleft 15");

        form.add(createLabel("Backup / Restore:"));
        form.add(backupBtnPanel);

        addSectionHeader(form, "Printer Setup");

        form.add(createLabel("Printer Mode:"));
        printerTypeCombo.setFont(AppTheme.bodyFont());
        form.add(printerTypeCombo, "h 36!");

        form.add(createLabel("Default Printer:"));
        availablePrintersCombo.setFont(AppTheme.bodyFont());
        form.add(availablePrintersCombo, "h 36!");

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private void addSectionHeader(JPanel panel, String titleText) {
        JLabel sectionLabel = new JLabel(titleText);
        sectionLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        sectionLabel.setForeground(AppTheme.PRIMARY);
        panel.add(sectionLabel, "span 2, gaptop 15, gapbottom 10");
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.bodyFont());
        return label;
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
