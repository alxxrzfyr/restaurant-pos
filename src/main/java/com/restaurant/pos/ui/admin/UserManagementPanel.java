package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class UserManagementPanel extends JPanel {

    private static final int ROW_HEIGHT = 38;
    private static final DateTimeFormatter LOCK_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final AppContext context;
    private final User currentUser;

    private final UserTableModel tableModel = new UserTableModel();
    private final JTable userTable = new JTable(tableModel);
    private final JLabel userCountLabel = new JLabel();

    public UserManagementPanel(AppContext context, User currentUser) {
        super(new BorderLayout(0, 16));
        this.context = context;
        this.currentUser = currentUser;

        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeaderBar(), BorderLayout.NORTH);
        add(buildTablePane(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("User Accounts & Employee Access");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Manage cashier & administrator logins, security locks, and role access permissions");
        subtitle.setFont(AppTheme.captionFont());
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);

        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, "growx");

        JButton addUserBtn = createButton("Add User", Icons.userPlus(Color.WHITE, 14), true);
        addUserBtn.addActionListener(e -> onAddUser());
        header.add(addUserBtn, "h 38!");

        return header;
    }

    private JButton createButton(String text, javax.swing.Icon icon, boolean primary) {
        JButton button = new JButton(text);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(8);
        }
        button.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        if (primary) {
            button.setBackground(AppTheme.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        } else {
            button.setBackground(AppTheme.CARD);
            button.setForeground(AppTheme.TEXT_PRIMARY);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                    BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        }
        return button;
    }

    private JPanel buildTablePane() {
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setBackground(AppTheme.CARD);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JPanel tableHeaderBar = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        tableHeaderBar.setOpaque(false);

        JPanel countBox = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        countBox.setOpaque(false);
        JLabel sectionTitle = new JLabel("System Users");
        sectionTitle.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        sectionTitle.setForeground(AppTheme.TEXT_PRIMARY);
        userCountLabel.setFont(AppTheme.captionFont());
        userCountLabel.setForeground(AppTheme.TEXT_MUTED);
        countBox.add(sectionTitle);
        countBox.add(userCountLabel);
        tableHeaderBar.add(countBox, "growx");

        JPanel toolbar = new JPanel(new MigLayout("insets 0", "[]8[]8[]8[]"));
        toolbar.setOpaque(false);

        JButton editUserBtn = createButton("Edit User", Icons.edit(AppTheme.TEXT_PRIMARY, 14), false);
        editUserBtn.addActionListener(e -> onEditUser());

        JButton changeRoleBtn = createButton("Change Role", Icons.shield(AppTheme.TEXT_PRIMARY, 14), false);
        changeRoleBtn.addActionListener(e -> onChangeRole());

        JButton resetPassBtn = createButton("Reset Password", Icons.key(AppTheme.TEXT_PRIMARY, 14), false);
        resetPassBtn.addActionListener(e -> onResetPassword());

        JButton toggleActiveBtn = createButton("Enable / Disable", Icons.toggleOn(AppTheme.TEXT_PRIMARY, 14), false);
        toggleActiveBtn.addActionListener(e -> onToggleActive());

        toolbar.add(editUserBtn, "h 36!");
        toolbar.add(changeRoleBtn, "h 36!");
        toolbar.add(resetPassBtn, "h 36!");
        toolbar.add(toggleActiveBtn, "h 36!");

        tableHeaderBar.add(toolbar, "align right");
        container.add(tableHeaderBar, BorderLayout.NORTH);

        userTable.setRowHeight(ROW_HEIGHT);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        userTable.setFont(AppTheme.bodyFont());
        com.restaurant.pos.ui.components.StripedTableCellRenderer.apply(userTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 8; i++) {
            if (i != 5) {
                userTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        userTable.getColumnModel().getColumn(5).setCellRenderer(new RoundedPillUserStatusRenderer());

        userTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        userTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        userTable.getColumnModel().getColumn(7).setPreferredWidth(140);

        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && userTable.getSelectedRow() >= 0) {
                    onEditUser();
                }
            }
        });

        container.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return container;
    }

    private void loadData() {
        List<User> users = context.userService().findAll();
        tableModel.setUsers(users);
        long activeCount = users.stream().filter(User::active).count();
        userCountLabel.setText(users.size() + " total users (" + activeCount + " active)");
    }

    private Frame getParentFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    private void onAddUser() {
        UserDialog dialog = new UserDialog(getParentFrame(), context, currentUser);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void onEditUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "Select User", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        User targetUser = tableModel.getUserAt(selectedRow);
        UserDialog dialog = new UserDialog(getParentFrame(), context, currentUser, targetUser);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void onToggleActive() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to enable/disable.", "Select User", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        User targetUser = tableModel.getUserAt(selectedRow);

        if (targetUser.id().equals(currentUser.id())) {
            JOptionPane.showMessageDialog(this, "You cannot disable your own active account.", "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean newActiveState = !targetUser.active();
        String actionText = newActiveState ? "enable" : "disable";

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + actionText + " user '" + targetUser.username() + "'?",
                "Confirm User Status Change", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                context.userService().setActive(targetUser.id(), newActiveState, currentUser.id(), currentUser.username());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onResetPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to reset password.", "Select User", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        User targetUser = tableModel.getUserAt(selectedRow);
        ResetPasswordDialog dialog = new ResetPasswordDialog(getParentFrame(), context, currentUser, targetUser);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            JOptionPane.showMessageDialog(this, "Password for '" + targetUser.username() + "' has been reset.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }
    }

    private void onChangeRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to change role.", "Select User", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        User targetUser = tableModel.getUserAt(selectedRow);

        Role newRole = (Role) JOptionPane.showInputDialog(this,
                "Select new role for '" + targetUser.username() + "':",
                "Change Role",
                JOptionPane.QUESTION_MESSAGE,
                null,
                Role.values(),
                targetUser.role());

        if (newRole != null && newRole != targetUser.role()) {
            try {
                context.userService().changeRole(targetUser.id(), newRole, currentUser.id(), currentUser.username());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error changing role: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static final class UserTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"ID", "Username", "Display Name", "Role", "Photo", "Status", "Failed Logins", "Locked Until"};
        private List<User> users = new ArrayList<>();

        void setUsers(List<User> users) {
            this.users = new ArrayList<>(users);
            fireTableDataChanged();
        }

        User getUserAt(int row) {
            return users.get(row);
        }

        @Override
        public int getRowCount() { return users.size(); }
        @Override
        public int getColumnCount() { return COLUMNS.length; }
        @Override
        public String getColumnName(int column) { return COLUMNS[column]; }
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            User u = users.get(rowIndex);
            Instant now = Instant.now();
            return switch (columnIndex) {
                case 0 -> u.id();
                case 1 -> u.username();
                case 2 -> u.displayName();
                case 3 -> u.role().name();
                case 4 -> u.photoPath() != null ? "Yes" : "No";
                case 5 -> u.active() ? "Active" : "Disabled";
                case 6 -> u.failedLoginAttempts();
                case 7 -> u.isCurrentlyLocked(now) ? LOCK_FORMAT.format(u.lockedUntil()) : "-";
                default -> "";
            };
        }
    }

    private static final class RoundedPillUserStatusRenderer extends DefaultTableCellRenderer {
        private String currentStatus = "";
        private boolean isRowSelected = false;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            this.currentStatus = value != null ? value.toString() : "";
            this.isRowSelected = isSelected;
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            setOpaque(false);

            if ("Active".equalsIgnoreCase(currentStatus)) {
                setForeground(AppTheme.SUCCESS);
            } else {
                setForeground(AppTheme.DANGER);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int pillW = 74;
            int pillH = 22;
            int x = (w - pillW) / 2;
            int y = (h - pillH) / 2;

            if ("Active".equalsIgnoreCase(currentStatus)) {
                g2.setColor(isRowSelected ? Color.decode("#BBF7D0") : AppTheme.SUCCESS_BG);
                g2.fillRoundRect(x, y, pillW, pillH, 12, 12);
                g2.setColor(AppTheme.SUCCESS_BORDER);
                g2.drawRoundRect(x, y, pillW, pillH, 12, 12);
            } else {
                g2.setColor(isRowSelected ? Color.decode("#FECACA") : AppTheme.DANGER_BG);
                g2.fillRoundRect(x, y, pillW, pillH, 12, 12);
                g2.setColor(AppTheme.DANGER_BORDER);
                g2.drawRoundRect(x, y, pillW, pillH, 12, 12);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
