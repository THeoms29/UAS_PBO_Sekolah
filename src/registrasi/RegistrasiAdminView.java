package registrasi;

import java.awt.*;
import javax.swing.*;

public class RegistrasiAdminView extends JFrame {
    // Komponen UI
    private JTextField txtNama;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtKonfirmasiPassword;
    private JButton btnRegistrasi;
    private JButton btnKeluar;

    public RegistrasiAdminView() {
        setTitle("Registrasi Administrator Pertama");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Keluar jika jendela ditutup
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label judul
        JLabel lblJudul = new JLabel("Buat Akun Administrator", SwingConstants.CENTER);
        lblJudul.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 20, 5);
        panel.add(lblJudul, gbc);

        // Reset insets
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;

        // Form Nama
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        txtNama = new JTextField(20);
        panel.add(txtNama, gbc);

        // Form Username
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        txtUsername = new JTextField(20);
        panel.add(txtUsername, gbc);

        // Form Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        txtPassword = new JPasswordField(20);
        panel.add(txtPassword, gbc);

        // Form Konfirmasi Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Konfirmasi Password:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        txtKonfirmasiPassword = new JPasswordField(20);
        panel.add(txtKonfirmasiPassword, gbc);

        // Panel Tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRegistrasi = new JButton("Registrasi");
        btnKeluar = new JButton("Keluar");
        buttonPanel.add(btnRegistrasi);
        buttonPanel.add(btnKeluar);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        panel.add(buttonPanel, gbc);

        add(panel);
    }

    // Getter untuk mendapatkan input dari form
    public String getNama() { return txtNama.getText().trim(); }
    public String getUsername() { return txtUsername.getText().trim(); }
    public String getPassword() { return new String(txtPassword.getPassword()); }
    public String getKonfirmasiPassword() { return new String(txtKonfirmasiPassword.getPassword()); }

    // Metode untuk menambahkan action listener ke tombol
    public void setRegistrasiAction(java.awt.event.ActionListener action) {
        btnRegistrasi.addActionListener(action);
    }

    public void setKeluarAction(java.awt.event.ActionListener action) {
        btnKeluar.addActionListener(action);
    }
}