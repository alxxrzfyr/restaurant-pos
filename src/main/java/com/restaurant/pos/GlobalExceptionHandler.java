package com.restaurant.pos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Uncaught exception in thread {}: ", t.getName(), e);

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "An unexpected application error occurred. Please contact support.",
                    "Application Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }
}
