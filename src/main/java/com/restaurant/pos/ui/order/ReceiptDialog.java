package com.restaurant.pos.ui.order;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.BusinessSettings;
import com.restaurant.pos.model.CheckoutResult;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.model.Payment;
import com.restaurant.pos.service.ReceiptFormatter;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;

public final class ReceiptDialog extends JDialog {

    private final AppContext context;
    private final Order order;
    private final Payment payment;

    private final JTextArea receiptArea = new JTextArea(24, ReceiptFormatter.RECEIPT_WIDTH);

    public ReceiptDialog(Frame owner, AppContext context, CheckoutResult result) {
        this(owner, context, result.order(), result.payment());
    }

    public ReceiptDialog(Frame owner, AppContext context, Order order, Payment payment) {
        super(owner, "Receipt Preview - Order #" + order.orderNumber(), true);
        this.context = context;
        this.order = order;
        this.payment = payment;

        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new MigLayout("insets 20, wrap 1, fill", "[440!]"));
        panel.setBackground(AppTheme.CARD);

        JLabel title = new JLabel("Order Receipt #" + order.orderNumber());
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(title, "gapbottom 12");

        receiptArea.setEditable(false);
        receiptArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        receiptArea.setBackground(Color.decode("#F8FAFC"));
        receiptArea.setForeground(AppTheme.TEXT_PRIMARY);
        receiptArea.setMargin(new Insets(16, 20, 16, 20));
        receiptArea.setText(buildReceiptText());
        receiptArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        panel.add(scrollPane, "grow, h 460!, gapbottom 16");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow]8[grow]8[grow]"));
        buttonPanel.setOpaque(false);

        com.restaurant.pos.ui.components.SecondaryButton printBtn = new com.restaurant.pos.ui.components.SecondaryButton("Print", Icons.printer(AppTheme.TEXT_PRIMARY, 16));
        printBtn.addActionListener(e -> printReceipt());

        com.restaurant.pos.ui.components.SecondaryButton pdfBtn = new com.restaurant.pos.ui.components.SecondaryButton("Save PDF", Icons.download(AppTheme.TEXT_PRIMARY, 16));
        pdfBtn.addActionListener(e -> exportPdf());

        com.restaurant.pos.ui.components.PrimaryButton closeBtn = new com.restaurant.pos.ui.components.PrimaryButton("Done", Icons.check(Color.WHITE, 16));
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(printBtn, "growx, h 40!");
        buttonPanel.add(pdfBtn, "growx, h 40!");
        buttonPanel.add(closeBtn, "growx, h 40!");
        panel.add(buttonPanel, "growx");

        return panel;
    }

    private String buildReceiptText() {
        BusinessSettings settings = context.settingsService().load();
        return ReceiptFormatter.formatReceipt(order, payment, settings);
    }

    private void printReceipt() {
        try {
            receiptArea.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Receipt as PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        chooser.setSelectedFile(new File("receipt-" + order.orderNumber() + ".pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            writePdf(file);
            JOptionPane.showMessageDialog(this, "PDF saved to:\n" + file.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "PDF export failed: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writePdf(File file) throws IOException {
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.COURIER);
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 70;
                float yStart = page.getMediaBox().getHeight() - margin;
                float leading = 14f;

                cs.beginText();
                cs.setFont(fontBold, 10);
                cs.setLeading(leading);
                cs.newLineAtOffset(margin, yStart);

                String[] lines = buildReceiptText().split("\n");
                for (String line : lines) {
                    if (line.contains("TOTAL") || line.contains("Order #") || line.contains("===")) {
                        cs.setFont(fontBold, 10);
                    } else {
                        cs.setFont(fontRegular, 10);
                    }
                    String safeLine = line.replace("\u20B1", "PHP ").replace("₱", "PHP ");
                    cs.showText(safeLine);
                    cs.newLine();
                }

                cs.endText();
            }

            doc.save(file);
        }
    }
}
