package NilaiSiswa;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import shared.Koneksi;

public class WaliKelasView extends JFrame {
    private JComboBox<String> comboKelas;
    private JComboBox<String> comboSemester;
    private JButton btnExportPerKelas;
    private JButton btnExportPerSiswa;
    private JButton btnKeInputNilai;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblKelasInfo;
    private JLabel lblStatus;
    private Image contentBackground;

    public WaliKelasView() {
        loadBackgrounds();
        initUI();
    }

    private void loadBackgrounds() {
        try {
            java.net.URL imgURL = getClass().getResource("/shared/Asset/BG1.JPEG");
            if (imgURL == null) {
                System.out.println("Resource /shared/Asset/BG1.JPEG tidak ditemukan di classpath");
                contentBackground = null;
            } else {
                ImageIcon contentIcon = new ImageIcon(imgURL);
                if (contentIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    contentBackground = contentIcon.getImage();
                    System.out.println("Latar belakang BG1.JPEG berhasil dimuat");
                } else {
                    System.out.println("Gagal memuat BG1.JPEG, status: " + contentIcon.getImageLoadStatus());
                    contentBackground = null;
                }
            }
        } catch (Exception e) {
            System.out.println("Error memuat latar belakang: " + e.getMessage());
            contentBackground = null;
        }
    }

    private void initUI() {
        setTitle("Aplikasi Rekap Nilai & Absensi - Wali Kelas");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (contentBackground != null) {
                    int width = getWidth();
                    int height = getHeight();
                    g.drawImage(contentBackground, 0, 0, width, height, this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    Color color1 = new Color(240, 248, 255);
                    Color color2 = new Color(230, 240, 250);
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        lblKelasInfo = new JLabel(" ", SwingConstants.LEFT);
        lblKelasInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblKelasInfo.setForeground(new Color(70, 130, 180));

        JLabel titleLabel = new JLabel("REKAP NILAI DAN ABSENSI SISWA");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(lblKelasInfo, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.SOUTH);

        // Panel Kontrol
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel lblKelas = new JLabel("Kelas:");
        lblKelas.setPreferredSize(new Dimension(80, 20));
        lblKelas.setForeground(Color.WHITE);

        comboKelas = new JComboBox<>();
        comboKelas.setPreferredSize(new Dimension(250, 35));
        comboKelas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboKelas.setMaximumSize(new Dimension(250, 35));

        JLabel lblSemester = new JLabel("Semester:");
        lblSemester.setPreferredSize(new Dimension(80, 20));
        lblSemester.setForeground(Color.WHITE);

        comboSemester = new JComboBox<>(new String[]{"Semester 1", "Semester 2"});
        comboSemester.setPreferredSize(new Dimension(120, 35));
        comboSemester.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboSemester.setMaximumSize(new Dimension(120, 35));

        btnExportPerKelas = createStyledButton("Export Per Kelas", new Color(70, 130, 180));
        btnExportPerSiswa = createStyledButton("Export Per Siswa", new Color(70, 130, 180));
        btnKeInputNilai = createStyledButton("Ke Input Nilai", new Color(139, 69, 19));

        controlPanel.add(lblKelas);
        controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanel.add(comboKelas);
        controlPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        controlPanel.add(lblSemester);
        controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanel.add(comboSemester);
        controlPanel.add(Box.createHorizontalGlue());
        controlPanel.add(btnExportPerKelas);
        controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanel.add(btnExportPerSiswa);
        controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanel.add(btnKeInputNilai);

        // Pengaturan Tabel
        String[] kolom = {"NIS", "Nama Siswa", "Nilai UH", "Nilai UTS", "Nilai UAS", "Nilai Akhir", 
                         "Hadir", "Izin", "Sakit", "Alfa", "Total"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 2 && columnIndex <= 9) return Double.class;
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(220, 240, 255));
        table.setSelectionForeground(Color.BLACK);

        table.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);
                
                if (value instanceof Double) {
                    double score = (Double) value;
                    if (score < 65) {
                        c.setBackground(new Color(255, 200, 200));
                    } else if (score < 75) {
                        c.setBackground(new Color(255, 255, 200));
                    } else {
                        c.setBackground(new Color(200, 255, 200));
                    }
                    
                    if (isSelected) {
                        c.setBackground(new Color(180, 220, 255));
                    }
                }
                
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Bilah Status
        lblStatus = new JLabel("Siap", SwingConstants.RIGHT);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(70, 130, 180));
        
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.add(lblStatus, BorderLayout.EAST);

        // Panel Tengah
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(controlPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);

        // Event Listeners
        btnKeInputNilai.addActionListener(e -> bukaInputNilaiView());
    }

    private void bukaInputNilaiView() {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Membuka InputNilaiView...");
                Connection conn = Koneksi.getConnection();
                if (conn == null) {
                    JOptionPane.showMessageDialog(this, "Gagal koneksi ke database!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String guruIdStr = null;
                while (guruIdStr == null || guruIdStr.trim().isEmpty()) {
                    guruIdStr = JOptionPane.showInputDialog(this, "Masukkan ID Guru:");
                    if (guruIdStr == null || guruIdStr.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "ID Guru diperlukan!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                int guruId = Integer.parseInt(guruIdStr);

                String mapelIdStr = null;
                while (mapelIdStr == null || mapelIdStr.trim().isEmpty()) {
                    mapelIdStr = JOptionPane.showInputDialog(this, "Masukkan ID Mapel:");
                    if (mapelIdStr == null || mapelIdStr.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "ID Mapel diperlukan!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                int mapelId = Integer.parseInt(mapelIdStr);

                try (PreparedStatement psGuru = conn.prepareStatement("SELECT id FROM users WHERE id = ? AND role = 'guru'")) {
                    psGuru.setInt(1, guruId);
                    if (!psGuru.executeQuery().next()) {
                        JOptionPane.showMessageDialog(this, "ID Guru " + guruId + " tidak valid!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                try (PreparedStatement psMapel = conn.prepareStatement("SELECT id FROM mapel WHERE id = ?")) {
                    psMapel.setInt(1, mapelId);
                    if (!psMapel.executeQuery().next()) {
                        JOptionPane.showMessageDialog(this, "ID Mapel " + mapelId + " tidak valid!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                InputNilaiView inputView = new InputNilaiView();
                NilaiModel model = new NilaiModel(conn);
                NilaiController controller = new NilaiController(inputView, model, guruId, mapelId);
                inputView.setController(controller);
                controller.loadDataAwal();
                inputView.setVisible(true);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID Guru atau Mapel harus berupa angka.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal membuka fitur Input Nilai: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(color);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(245, 245, 245));
                button.setForeground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(color);
            }
        });
        
        return button;
    }

    // Getter
    public JComboBox<String> getComboKelas() {
        return comboKelas;
    }

    public JComboBox<String> getComboSemester() {
        return comboSemester;
    }

    public JButton getBtnExportPerKelas() {
        return btnExportPerKelas;
    }

    public JButton getBtnExportPerSiswa() {
        return btnExportPerSiswa;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JTable getTable() {
        return table;
    }

    public void setKelasInfo(String kelas) {
        lblKelasInfo.setText("Kelas: " + kelas);
    }

    public void setStatusMessage(String message) {
        lblStatus.setText(message);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}