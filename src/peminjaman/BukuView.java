package peminjaman;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BukuView extends JFrame {
    public JTable tableBuku;
    public DefaultTableModel modelBuku;
    
    public JTextField txtId;
    public JTextField txtJudul;
    public JTextField txtPenulis;
    public JSpinner spnJumlah;
    
    public JButton btnSimpan;
    public JButton btnUpdate;
    public JButton btnHapus;
    public JButton btnBatal;
    public JButton btnKembali;
    
    public JTextField txtCari;
    public JButton btnCari;
    public JButton btnRefresh;
    
    public JLabel statusLabel;
    
    public BukuView() {
        setTitle("Manajemen Buku Perpustakaan");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }
    
    private void initComponents() {
        // Panel utama dengan BoxLayout untuk mengatur komponen secara vertikal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setOpaque(false);
        
        // Panel navigasi
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setOpaque(false);
        btnKembali = new JButton("Kembali ke Modul Peminjaman");

        btnKembali.setBackground(new Color(70,130,180)); // Warna merah
        btnKembali.setForeground(Color.WHITE);
        btnKembali.setOpaque(true); // Menghilangkan border fokus
        btnKembali.setFocusPainted(false); // Menghilangkan efek fokus

        navPanel.add(btnKembali);
        
        // Panel Form di dalam panel terpisah dengan ukuran tetap
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBorder(BorderFactory.createTitledBorder("Form Buku"));
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        txtId = new JTextField();
        txtId.setEditable(false); // ID tidak bisa diedit
        txtJudul = new JTextField();
        txtPenulis = new JTextField();
        spnJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        
        formPanel.add(new JLabel("ID:"));
        formPanel.add(txtId);
        formPanel.add(new JLabel("Judul:"));
        formPanel.add(txtJudul);
        formPanel.add(new JLabel("Penulis:"));
        formPanel.add(txtPenulis);
        formPanel.add(new JLabel("Jumlah:"));
        formPanel.add(spnJumlah);
        
        // Panel Tombol Form
        JPanel btnFormPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        btnSimpan = new JButton("Simpan");
        btnUpdate = new JButton("Update");
        btnHapus = new JButton("Hapus");
        btnBatal = new JButton("Batal");

        btnSimpan.setBackground(new Color(70, 130, 180)); // Warna biru
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setOpaque(true);
        btnSimpan.setBorderPainted(false);
        
        btnUpdate.setBackground(new Color(34, 139, 34)); // Warna hijau
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setOpaque(true);
        btnUpdate.setBorderPainted(false);
        
        btnHapus.setBackground(new Color(220, 20, 60)); // Warna merah crimson
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setOpaque(true);
        btnHapus.setBorderPainted(false);
        
        btnBatal.setBackground(new Color(128, 128, 128)); // Warna abu-abu
        btnBatal.setForeground(Color.WHITE);
        btnBatal.setOpaque(true);
        btnBatal.setBorderPainted(false);
        
        btnFormPanel.add(btnSimpan);
        btnFormPanel.add(btnUpdate);
        btnFormPanel.add(btnHapus);
        btnFormPanel.add(btnBatal);
        
        formContainer.add(formPanel, BorderLayout.CENTER);
        formContainer.add(btnFormPanel, BorderLayout.SOUTH);
        
        // Panel cari dalam panel terpisah
        JPanel cariContainer = new JPanel(new BorderLayout(5, 5));
        cariContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JPanel cariPanel = new JPanel(new BorderLayout(5, 0));

        txtCari = new JTextField();
        btnCari = new JButton("Cari");
        btnRefresh = new JButton("Refresh");

        btnCari.setBackground(new Color(70, 130, 180)); // Warna biru
        btnCari.setForeground(Color.WHITE);
        btnCari.setOpaque(true);
        btnCari.setBorderPainted(false);
        
        btnRefresh.setBackground(new Color(70, 130, 180)); // Warna biru
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setOpaque(true);
        btnRefresh.setBorderPainted(false);
        
        cariPanel.add(new JLabel("Cari (Judul/Penulis): "), BorderLayout.WEST);
        cariPanel.add(txtCari, BorderLayout.CENTER);
        
        JPanel btnCariPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCariPanel.add(btnCari);
        btnCariPanel.add(btnRefresh);
        cariPanel.add(btnCariPanel, BorderLayout.EAST);
        
        cariContainer.add(cariPanel, BorderLayout.CENTER);
        
        // Tabel dalam scroll pane
        modelBuku = new DefaultTableModel(
            new String[] {"ID", "Judul", "Penulis", "Jumlah"}, 
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabel tidak bisa diedit langsung
            }
        };
        
        tableBuku = new JTable(modelBuku);
        
        // Menambahkan event klik pada tabel
        tableBuku.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableBuku.getSelectedRow();
                if (row != -1) {
                    txtId.setText(tableBuku.getValueAt(row, 0).toString());
                    txtJudul.setText(tableBuku.getValueAt(row, 1).toString());
                    txtPenulis.setText(tableBuku.getValueAt(row, 2).toString());
                    spnJumlah.setValue(Integer.valueOf(tableBuku.getValueAt(row, 3).toString()));
                    
                    // Aktifkan tombol update dan hapus
                    btnUpdate.setEnabled(true);
                    btnHapus.setEnabled(true);
                    btnSimpan.setEnabled(false);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tableBuku);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Daftar Buku"));
        scrollPane.setPreferredSize(new Dimension(750, 300));
        
        // Panel tabel container dengan ukuran yang dapat mengecil/membesar
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        // Status bar
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusLabel.setOpaque(true);
        
        // Menambahkan semua ke panel utama dengan urutan yang benar
        mainPanel.add(formContainer);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacing
        mainPanel.add(cariContainer);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacing
        mainPanel.add(tableContainer);

        // === PANEL LATAR DENGAN GAMBAR ===
        JPanel panelBackground = new JPanel() {
            private Image bg;

            {
                try {
                    bg = new ImageIcon(getClass().getResource("/shared/Asset/BG1.jpeg")).getImage();
                } catch (Exception e) {
                    System.out.println("Gambar background tidak ditemukan: " + e.getMessage());
                    bg = null;
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bg != null) {
                    g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        panelBackground.setLayout(new BorderLayout());

        panelBackground.add(mainPanel, BorderLayout.CENTER);
        setContentPane(panelBackground);
        
        // Layout utama
        setLayout(new BorderLayout());
        add(navPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        // Set keadaan awal tombol
        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);
    }
    
    // Methods penghubung action
    public void setSimpanAction(java.awt.event.ActionListener act) {
        btnSimpan.addActionListener(act);
    }
    
    public void setUpdateAction(java.awt.event.ActionListener act) {
        btnUpdate.addActionListener(act);
    }
    
    public void setHapusAction(java.awt.event.ActionListener act) {
        btnHapus.addActionListener(act);
    }
    
    public void setBatalAction(java.awt.event.ActionListener act) {
        btnBatal.addActionListener(act);
    }
    
    public void setCariAction(java.awt.event.ActionListener act) {
        btnCari.addActionListener(act);
    }
    
    public void setRefreshAction(java.awt.event.ActionListener act) {
        btnRefresh.addActionListener(act);
    }
    
    public void setKembaliAction(java.awt.event.ActionListener act) {
        btnKembali.addActionListener(act);
    }
    
    // Methods untuk mendapatkan input
    public String getInputJudul() {
        return txtJudul.getText().trim();
    }
    
    public String getInputPenulis() {
        return txtPenulis.getText().trim();
    }
    
    public int getInputJumlah() {
        return (Integer) spnJumlah.getValue();
    }
    
    public String getInputId() {
        return txtId.getText().trim();
    }
    
    public String getInputCari() {
        return txtCari.getText().trim();
    }
    
    // Methods untuk mengeset input
    public void resetForm() {
        txtId.setText("");
        txtJudul.setText("");
        txtPenulis.setText("");
        spnJumlah.setValue(1);
        btnSimpan.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);
        tableBuku.clearSelection();
    }
    
    public void setStatus(String message) {
        statusLabel.setText(message);
    }
}