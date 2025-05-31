package login;

import java.awt.*;
import javax.swing.*;

public class LoginView extends JFrame {
    // Konstanta untuk pengaturan UI
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 300;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);

    // Komponen form login
    public JTextField usernameField;
    public JPasswordField passwordField;
    public JButton btnLogin;
    public JButton btnExit;
    public JLabel statusLabel;

    public LoginView() {
        setTitle("Login - Sistem Informasi SMPN 1 Adiluwih");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        // Panel utama dengan background
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Panel header dengan judul
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel form login
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panel status
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, 80));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("SISTEM INFORMASI");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel schoolLabel = new JLabel("SMPN 1 Adiluwih");
        schoolLabel.setFont(new Font("Arial", Font.BOLD, 16));
        schoolLabel.setForeground(Color.WHITE);
        schoolLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(Box.createVerticalGlue());
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(schoolLabel);
        headerPanel.add(Box.createVerticalGlue());

        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Label dan field username
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(LABEL_FONT);
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        usernameField.setFont(LABEL_FONT);
        usernameField.setPreferredSize(new Dimension(150, 30));
        formPanel.add(usernameField, gbc);

        // Label dan field password
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(LABEL_FONT);
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(15);
        passwordField.setFont(LABEL_FONT);
        passwordField.setPreferredSize(new Dimension(150, 30));
        formPanel.add(passwordField, gbc);

        // Panel tombol
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = createButtonPanel();
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        btnLogin = new JButton("Login");
        btnLogin.setFont(LABEL_FONT);
        btnLogin.setBackground(PRIMARY_COLOR);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(80, 35));
        btnLogin.setFocusPainted(false);

        btnExit = new JButton("Keluar");
        btnExit.setFont(LABEL_FONT);
        btnExit.setBackground(new Color(231, 76, 60));
        btnExit.setForeground(Color.WHITE);
        btnExit.setPreferredSize(new Dimension(80, 35));
        btnExit.setFocusPainted(false);

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);

        return buttonPanel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, 30));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusPanel.add(statusLabel);

        return statusPanel;
    }

    // Action listeners
    public void setLoginAction(java.awt.event.ActionListener action) {
        btnLogin.addActionListener(action);
        // Enter key pada password field juga trigger login
        passwordField.addActionListener(action);
    }

    public void setExitAction(java.awt.event.ActionListener action) {
        btnExit.addActionListener(action);
    }

    // Getter methods
    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    // Utility methods
    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        statusLabel.setText(" ");
    }

    public void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : Color.BLACK);
    }

    public void focusUsername() {
        usernameField.requestFocus();
    }
}