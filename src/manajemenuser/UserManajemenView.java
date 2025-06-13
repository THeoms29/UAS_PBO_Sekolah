package manajemenuser;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UserManajemenView extends JFrame {

    // Komponen Form
    private JTextField txtId = new JTextField();
    private JTextField txtNama = new JTextField();
    private JTextField txtUsername = new JTextField();
    private JPasswordField txtPassword = new JPasswordField();
    private JComboBox<String> cbRole;

    // Tombol Aksi
    public JButton btnSimpan, btnUpdate, btnHapus, btnBatal;

    // Tabel
    public JTable tableUsers;
    public DefaultTableModel tableModel;

    public UserManajemenView() {
        setTitle("Manajemen Pengguna");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Tutup jendela ini saja
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        // Panel utama
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Pengguna"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID (tidak bisa diedit)
        txtId.setEditable(false);
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(txtId, gbc);

        // Nama
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(txtNama, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(txtPassword, gbc);
        // Tambahkan label kecil sebagai petunjuk
        JLabel lblPasswordHint = new JLabel("Kosongkan jika tidak ingin mengubah password");
        lblPasswordHint.setFont(new Font("Arial", Font.ITALIC, 10));
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(lblPasswordHint, gbc);

        // Role
        String[] roles = {"guru", "staff", "kepala_sekolah"};
        cbRole = new JComboBox<>(roles);
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; formPanel.add(cbRole, gbc);

        // Panel Tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnSimpan = new JButton("Simpan Baru");
        btnUpdate = new JButton("Update");
        btnHapus = new JButton("Hapus");
        btnBatal = new JButton("Batal");
        buttonPanel.add(btnSimpan);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnBatal);

        // Gabungkan Form dan Tombol
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Tabel Pengguna
        String[] columnHeaders = {"ID", "Nama", "Username", "Role"};
        tableModel = new DefaultTableModel(columnHeaders, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Membuat tabel read-only
            }
        };
        tableUsers = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableUsers);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Daftar Pengguna"));

        // Menambahkan listener klik pada tabel
        tableUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = tableUsers.getSelectedRow();
                if (selectedRow != -1) {
                    txtId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                    txtNama.setText(tableModel.getValueAt(selectedRow, 1).toString());
                    txtUsername.setText(tableModel.getValueAt(selectedRow, 2).toString());
                    cbRole.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
                    
                    // Mengatur state tombol
                    btnSimpan.setEnabled(false);
                    btnUpdate.setEnabled(true);
                    btnHapus.setEnabled(true);
                }
            }
        });

        // Gabungkan semua ke panel utama
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
        
        resetForm(); // Panggil untuk set state awal
    }
    
    // Metode untuk mereset form ke keadaan awal
    public void resetForm() {
        txtId.setText("");
        txtNama.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        cbRole.setSelectedIndex(0);
        tableUsers.clearSelection();
        btnSimpan.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);
    }

    // Getters untuk input
    public String getId() { return txtId.getText(); }
    public String getNama() { return txtNama.getText().trim(); }
    public String getUsername() { return txtUsername.getText().trim(); }
    public String getPassword() { return new String(txtPassword.getPassword()); }
    public String getRole() { return (String) cbRole.getSelectedItem(); }

    // Setters untuk action listeners
    public void setSimpanAction(java.awt.event.ActionListener a) { btnSimpan.addActionListener(a); }
    public void setUpdateAction(java.awt.event.ActionListener a) { btnUpdate.addActionListener(a); }
    public void setHapusAction(java.awt.event.ActionListener a) { btnHapus.addActionListener(a); }
    public void setBatalAction(java.awt.event.ActionListener a) { btnBatal.addActionListener(a); }
}