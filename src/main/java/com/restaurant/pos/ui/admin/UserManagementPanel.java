package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class UserManagementPanel extends JPanel {

    private static final int ROW_HEIGHT = 36;
    private static final DateTimeFormatter LOCK_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final AppContext context;
    private final User currentUser;

    private final UserTableModel tableModel = new UserTableModel();
    private final JTable userTable = new JTable(tableModel);

    public UserManagementPanel(AppContext context, User currentUser) {
        super(new BorderLayout(0, 12));
        this.context = context;
        this.currentUser = currentUser;

        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeaderBar(), BorderLayout.NORTH);
        add(buildTablePane(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        header.setOpaque(false);

        JLabel title = new JLabel("User Accounts & Employee Roles");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title);

        JPanel toolbar = new JPanel(new MigLayout("insets 0", "[][][][][]"));
        toolbar.setOpaque(false);

        JButton addUserBtn = createButton("Add User", false);
        addUserBtn.addActionListener(e -> onAddUser());

        JButton editUserBtn = createButton("Edit User", false);
        editUserBtn.addActionListener(e -> onEditUser());

        JButton toggleActiveBtn = createButton("Enable / Disable", false);
        toggleActiveBtn.addActionListener(e -> onToggleActive());

        JButton resetPassBtn = createButton("Reset Password", false);
        resetPassBtn.addActionListener(e -> onResetPassword());

        JButton changeRoleBtn = createButton("Change Role", false);
        changeRoleBtn.addActionListener(e -> onChangeRole());

        toolbar.add(addUserBtn, "h 38!");
        toolbar.add(editUserBtn, "h 38!");
        toolbar.add(toggleActiveBtn, "h 38!");
        toolbar.add(resetPassBtn, "h 38!");
        toolbar.add(changeRoleBtn, "h 38!");

        header.add(toolbar);
        return header;
    }

    private JButton createButton(String text, boolean danger) {
        JButton button = new JButton(text);
        button.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (danger) {
            button.setBackground(AppTheme.DANGER);
            button.setForeground(Color.WHITE);
        }
        return button;
    }

    private JScrollPane buildTablePane() {
        userTable.setRowHeight(ROW_HEIGHT);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        userTable.setFont(AppTheme.bodyFont());
        com.restaurant.pos.ui.components.StripedTableCellRenderer.apply(userTable);

        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && userTable.getSelectedRow() >= 0) {
                    onEditUser();
                }
            }
        });

        return new JScrollPane(userTable);
    }

    private void loadData() {
        List<User> users = context.userService().findAll();
        tableModel.setUsers(users);
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
}
