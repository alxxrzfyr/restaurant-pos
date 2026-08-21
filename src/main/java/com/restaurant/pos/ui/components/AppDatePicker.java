package com.restaurant.pos.ui.components;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import net.miginfocom.swing.MigLayout;

public final class AppDatePicker extends JPanel {

    private final DatePicker datePicker;

    public AppDatePicker() {
        this(LocalDate.now());
    }

    public AppDatePicker(LocalDate initialDate) {
        super(new MigLayout("insets 0, fill", "[grow, fill]", "[grow, fill]"));
        setOpaque(false);

        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("yyyy-MM-dd");
        settings.setAllowKeyboardEditing(false);
        settings.setAllowEmptyDates(false);
        settings.setFontValidDate(AppTheme.bodyFont());

        datePicker = new DatePicker(settings);
        datePicker.setDate(initialDate != null ? initialDate : LocalDate.now());

        JButton btn = datePicker.getComponentToggleCalendarButton();
        btn.setText("");
        btn.setIcon(Icons.calendar(AppTheme.TEXT_PRIMARY, 15));
        btn.setBackground(AppTheme.CARD);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        JTextField tf = datePicker.getComponentDateTextField();
        tf.setFont(AppTheme.bodyFont());
        tf.setEditable(false);
        tf.setFocusable(false);
        tf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tf.setBackground(AppTheme.CARD);
        tf.setForeground(AppTheme.TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(2, 10, 2, 10)));

        tf.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                btn.doClick();
            }
        });

        add(datePicker, "grow");
    }

    public LocalDate getSelectedDate() {
        LocalDate date = datePicker.getDate();
        return date != null ? date : LocalDate.now();
    }

    public void setSelectedDate(LocalDate date) {
        if (date != null) {
            datePicker.setDate(date);
        }
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }
}
