package NilaiSiswa;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import main.MainController;
import shared.Koneksi;

public class InputNilaiView extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(NilaiSiswa.InputNilaiView.class.getName());
    private JLabel labelGuruMapel, labelTitle;
    private JComboBox<Map<String, Object>> comboKelas, comboSiswa;
    private JComboBox<String> comboSemester;
    private JTextField txtNilaiUH, txtNilaiUTS, txtNilaiUAS;
    private JButton btnTambahNilai, btnKeWaliKelas, btnEdit;
    private JTable tableNilai;
    private DefaultTableModel tableModel;
    private NilaiController controller;
    private MainController mainController;
    private Image contentBackground;
    private Color primaryColor = new Color(70, 130, 180);
    private Color buttonColor = new Color(0, 105, 180);
    private Color navButtonColor = new Color(139, 69, 19);
    private int mapelId;

    public void setController(NilaiController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller tidak boleh null");
        }
        this.controller = controller;
        LOGGER.info("Controller diatur untuk InputNilaiView: " + controller.getClass().getName());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        if (mainController != null && mainController.getCurrentUser() != null) {
            int guruId = mainController.getCurrentUser().getId();
            this.mapelId = mainController.getMapelIdForGuru(guruId);
            String namaMapel = mapelId != -1 ? getNamaMapel(mapelId) : "-";
            labelGuruMapel.setText("Guru: " + mainController.getCurrentUser().getNama() + " | Mapel: " + namaMapel + " | Semester: -");
        } else {
            labelGuruMapel.setText("Guru: - | Mapel: - | Semester: -");
            LOGGER.warning("MainController atau currentUser null saat mengatur label Guru/Mapel");
        }
    }

    private String getNamaMapel(int mapelId) {
        try {
            NilaiModel nilaiModel = new NilaiModel(Koneksi.getConnection());
            return nilaiModel.getNamaMapel(mapelId);
        } catch (SQLException e) {
            LOGGER.severe("Gagal mengambil nama mapel: " + e.getMessage());
            return "-";
        }
    }

    private void loadBackgrounds() {
        try {
            java.net.URL imgURL = getClass().getResource("/shared/Asset/BG1.JPEG");
            if (imgURL == null) {
                LOGGER.warning("Resource /shared/Asset/BG1.JPEG tidak ditemukan di classpath");
                contentBackground = null;
            } else {
                ImageIcon contentIcon = new ImageIcon(imgURL);
                if (contentIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    contentBackground = contentIcon.getImage();
                    LOGGER.info("Latar belakang BG1.JPEG berhasil dimuat");
                } else {
                    LOGGER.warning("Gagal memuat BG1.JPEG, status: " + contentIcon.getImageLoadStatus());
                    contentBackground = null;
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error memuat latar belakang: " + e.getMessage());
            contentBackground = null;
        }
    }

    private void initComponents() {
        setTitle("Aplikasi Input Nilai Siswa");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
            if (mainController != null) {
                mainController.showMainMenu();
                LOGGER.info("InputNilaiView ditutup, kembali ke MainMenuView");
            }
            dispose();
        }
    });

        // Panel Header
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelHeader.setPreferredSize(new Dimension(100, 80));
        panelHeader.setOpaque(false);

        labelTitle = new JLabel("INPUT NILAI SISWA");
        labelTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        labelTitle.setForeground(primaryColor);
        labelTitle.setHorizontalAlignment(SwingConstants.CENTER);

        labelGuruMapel = new JLabel("Guru: - | Mapel: - | Semester: -");
        labelGuruMapel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelGuruMapel.setForeground(primaryColor.darker());
        labelGuruMapel.setHorizontalAlignment(SwingConstants.CENTER);

        panelHeader.add(labelTitle, BorderLayout.NORTH);
        panelHeader.add(labelGuruMapel, BorderLayout.CENTER);

        // Main Content Panel
        JPanel mainContentPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainContentPanel.setOpaque(false);

        // Left Panel untuk Form
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(240, 248, 255, 200));
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 2),
                "Form Input Nilai",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                primaryColor));

        // Panel Form
        JPanel panelForm = new JPanel();
        panelForm.setLayout(new BoxLayout(panelForm, BoxLayout.Y_AXIS));
        panelForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 30, 15));
        panelForm.setOpaque(false);

        // Panel Kelas
        JPanel panelKelas = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelKelas.setOpaque(false);
        JLabel lblKelas = new JLabel("Kelas:");
        lblKelas.setPreferredSize(new Dimension(80, 20));
        panelKelas.add(lblKelas);

        comboKelas = new JComboBox<>();
        comboKelas.setPreferredSize(new Dimension(200, 25));
        comboKelas.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Map) {
                    value = ((Map<String, Object>) value).get("nama_kelas");
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        panelKelas.add(comboKelas);
        panelForm.add(panelKelas);

        // Panel Semester
        JPanel panelSemester = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelSemester.setOpaque(false);
        JLabel lblSemester = new JLabel("Semester:");
        lblSemester.setPreferredSize(new Dimension(80, 20));
        panelSemester.add(lblSemester);

        comboSemester = new JComboBox<>(new String[]{"Semester 1", "Semester 2"});
        comboSemester.setPreferredSize(new Dimension(120, 25));
        panelSemester.add(comboSemester);
        panelForm.add(panelSemester);

        // Panel Siswa
        JPanel panelSiswa = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelSiswa.setOpaque(false);
        JLabel lblSiswa = new JLabel("Nama:");
        lblSiswa.setPreferredSize(new Dimension(80, 20));
        panelSiswa.add(lblSiswa);

        comboSiswa = new JComboBox<>();
        comboSiswa.setPreferredSize(new Dimension(250, 25));
        comboSiswa.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Map) {
                    value = ((Map<String, Object>) value).get("nama") + " - " + ((Map<String, Object>) value).get("nis");
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        panelSiswa.add(comboSiswa);
        panelForm.add(panelSiswa);

        // Panel Input Nilai
        JPanel panelNilaiInput = new JPanel(new GridLayout(3, 2, 10, 10));
        panelNilaiInput.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1),
                "Input Nilai",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                primaryColor));

        // Nilai UH
        JPanel panelUH = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelUH.setOpaque(false);
        JLabel lblUH = new JLabel("Nilai UH:");
        lblUH.setPreferredSize(new Dimension(80, 20));
        panelUH.add(lblUH);

        txtNilaiUH = new JTextField();
        txtNilaiUH.setPreferredSize(new Dimension(60, 25));
        panelUH.add(txtNilaiUH);
        panelNilaiInput.add(panelUH);

        // Nilai UTS
        JPanel panelUTS = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelUTS.setOpaque(false);
        JLabel lblUTS = new JLabel("Nilai UTS:");
        lblUTS.setPreferredSize(new Dimension(80, 20));
        panelUTS.add(lblUTS);

        txtNilaiUTS = new JTextField();
        txtNilaiUTS.setPreferredSize(new Dimension(60, 25));
        panelUTS.add(txtNilaiUTS);
        panelNilaiInput.add(panelUTS);

        // Nilai UAS
        JPanel panelUAS = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelUAS.setOpaque(false);
        JLabel lblUAS = new JLabel("Nilai UAS:");
        lblUAS.setPreferredSize(new Dimension(80, 20));
        panelUAS.add(lblUAS);

        txtNilaiUAS = new JTextField();
        txtNilaiUAS.setPreferredSize(new Dimension(60, 25));
        panelUAS.add(txtNilaiUAS);
        panelNilaiInput.add(panelUAS);

        panelForm.add(panelNilaiInput);

        // Panel Tombol
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panelButton.setOpaque(false);
        panelButton.setBackground(new Color(255, 255, 255, 150));
        panelButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnTambahNilai = createStyledButton("Tambah Nilai", buttonColor);
        addButtonShadow(btnTambahNilai);
        panelButton.add(btnTambahNilai);

        btnEdit = createStyledButton("Edit", buttonColor);
        addButtonShadow(btnEdit);
        panelButton.add(btnEdit);

        btnKeWaliKelas = createStyledButton("Ke Wali Kelas", navButtonColor);
        addButtonShadow(btnKeWaliKelas);
        panelButton.add(btnKeWaliKelas);

        panelForm.add(panelButton);

        leftPanel.add(panelForm, BorderLayout.CENTER);

        // Right Panel untuk Tabel Nilai
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(240, 248, 255, 200));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 2),
                "Daftar Nilai Siswa",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                primaryColor));

        // Tabel Nilai
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Nama Siswa", "NIS", "UH", "UTS", "UAS", "Nilai Akhir"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableNilai = new JTable(tableModel) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 220));
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        tableNilai.setRowHeight(25);
        tableNilai.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tableNilai.getColumnCount(); i++) {
            tableNilai.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tableNilai);
        scrollPane.setPreferredSize(new Dimension(550, 500));
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Gabungkan Panel Kiri dan Kanan
        mainContentPanel.add(leftPanel);
        mainContentPanel.add(rightPanel);

        // Panel Main dengan Background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (contentBackground != null) {
                    int width = getWidth();
                    int height = getHeight();
                    g.drawImage(contentBackground, 0, 0, width, height, this);
                } else {
                    g.setColor(new Color(240, 248, 255));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setOpaque(false);

        mainPanel.add(panelHeader, BorderLayout.NORTH);
        mainPanel.add(mainContentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Tambahkan listener ke kolom input untuk mengaktifkan tombol
        DocumentListener inputListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateButtonState();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateButtonState();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateButtonState();
            }
            private void updateButtonState() {
                String uh = txtNilaiUH.getText().trim();
                String uts = txtNilaiUTS.getText().trim();
                String uas = txtNilaiUAS.getText().trim();
                boolean enabled = !uh.isEmpty() || !uts.isEmpty() || !uas.isEmpty();
                btnTambahNilai.setEnabled(enabled);
                LOGGER.info("Status tombol Tambah Nilai: " + (enabled ? "Aktif" : "Nonaktif") + ", Input: UH=" + uh + ", UTS=" + uts + ", UAS=" + uas);
            }
        };
        txtNilaiUH.getDocument().addDocumentListener(inputListener);
        txtNilaiUTS.getDocument().addDocumentListener(inputListener);
        txtNilaiUAS.getDocument().addDocumentListener(inputListener);

        // Event Listeners
        btnTambahNilai.addActionListener(e -> {
            btnTambahNilai.setEnabled(false); // Nonaktifkan tombol sementara
            if (controller != null) {
                controller.tambahAtauUpdateNilai();
                if (mainController != null) {
                    mainController.notifyWaliKelasDataChanged();
                }
            }
            btnTambahNilai.setEnabled(true); // Aktifkan kembali tombol
        });

        btnEdit.addActionListener(e -> editSelectedRow());

        btnKeWaliKelas.addActionListener(e -> bukaWaliKelasView());

        comboKelas.addActionListener(e -> {
            if (controller != null && comboKelas.getSelectedItem() != null) {
                Map<String, Object> selectedKelas = (Map<String, Object>) comboKelas.getSelectedItem();
                String semester = comboSemester.getSelectedItem() != null ? 
                    comboSemester.getSelectedItem().toString().replace("Semester ", "") : "1";
                controller.kelasDipilih((String) selectedKelas.get("nama_kelas"), semester);
                if (mainController != null) {
                    mainController.setLastSelectedKelas((String) selectedKelas.get("nama_kelas"));
                    mainController.setLastSelectedSemester(semester);
                }
                LOGGER.info("Kelas dipilih dan disimpan: " + selectedKelas.get("nama_kelas") + ", semester=" + semester);
            }
        });

        comboSemester.addActionListener(e -> {
            if (controller != null && comboKelas.getSelectedItem() != null) {
                Map<String, Object> selectedKelas = (Map<String, Object>) comboKelas.getSelectedItem();
                String semester = comboSemester.getSelectedItem() != null 
                    ? comboSemester.getSelectedItem().toString().replace("Semester ", "") 
                    : "1";
                controller.kelasDipilih((String) selectedKelas.get("nama_kelas"), semester);
                if (mainController != null) {
                    mainController.setLastSelectedKelas((String) selectedKelas.get("nama_kelas"));
                    mainController.setLastSelectedSemester(semester);
                }
                LOGGER.info("Semester dipilih dan disimpan: " + semester);
            }
        });
    }

    private void editSelectedRow() {
        int selectedRow = tableNilai.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih baris data yang akan diedit!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            LOGGER.warning("Tidak ada baris yang dipilih untuk diedit");
            return;
        }

        try {
            String uh = tableNilai.getValueAt(selectedRow, 3) != null ? tableNilai.getValueAt(selectedRow, 3).toString() : "";
            String uts = tableNilai.getValueAt(selectedRow, 4) != null ? tableNilai.getValueAt(selectedRow, 4).toString() : "";
            String uas = tableNilai.getValueAt(selectedRow, 5) != null ? tableNilai.getValueAt(selectedRow, 5).toString() : "";

            txtNilaiUH.setText(uh);
            txtNilaiUTS.setText(uts);
            txtNilaiUAS.setText(uas);

            String nis = tableNilai.getValueAt(selectedRow, 2) != null ? tableNilai.getValueAt(selectedRow, 2).toString() : "";
            boolean siswaDitemukan = false;
            for (int i = 0; i < comboSiswa.getItemCount(); i++) {
                Map<String, Object> siswa = comboSiswa.getItemAt(i);
                if (siswa.get("nis").equals(nis)) {
                    comboSiswa.setSelectedIndex(i);
                    siswaDitemukan = true;
                    break;
                }
            }
            if (!siswaDitemukan) {
                LOGGER.warning("Siswa dengan NIS=" + nis + " tidak ditemukan di comboSiswa");
            }

            LOGGER.info("Data dari baris " + (selectedRow + 1) + " dimuat untuk diedit: UH=" + uh + ", UTS=" + uts + ", UAS=" + uas);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data untuk diedit: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal memuat data untuk diedit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void bukaWaliKelasView() {
        SwingUtilities.invokeLater(() -> {
            try {
                LOGGER.info("Membuka WaliKelasView...");
                if (mainController != null && mainController.getCurrentUser() != null) {
                    mainController.openWaliKelasModule();
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Sesi login tidak valid, gagal membuka Wali Kelas!", "Error", JOptionPane.ERROR_MESSAGE);
                    LOGGER.severe("MainController atau currentUser null saat membuka WaliKelasView");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal membuka fitur Wali Kelas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Gagal memuat WaliKelasView: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(180, 35));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 2),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void addButtonShadow(JButton button) {
        Border line = new LineBorder(button.getBackground().darker());
        Border margin = new EmptyBorder(5, 15, 5, 15);
        Border compound = new CompoundBorder(line, margin);

        button.setBorder(compound);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        button.getModel().addChangeListener(e -> {
            if (button.getModel().isPressed()) {
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(button.getBackground().darker().darker(), 2),
                        BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            } else {
                button.setBorder(compound);
            }
        });
    }

    public InputNilaiView() {
        loadBackgrounds();
        initComponents();
    }

    public JComboBox<Map<String, Object>> getComboKelas() {
        return comboKelas;
    }

    public JComboBox<Map<String, Object>> getComboSiswa() {
        return comboSiswa;
    }

    public JComboBox<String> getComboSemester() {
        return comboSemester;
    }

    public JTextField getTxtNilaiUH() {
        return txtNilaiUH;
    }

    public JTextField getTxtNilaiUTS() {
        return txtNilaiUTS;
    }

    public JTextField getTxtNilaiUAS() {
        return txtNilaiUAS;
    }

    public JButton getBtnTambahNilai() {
        return btnTambahNilai;
    }

    public void setComboKelas(List<Map<String, Object>> daftarKelas) {
        comboKelas.removeAllItems();
        for (Map<String, Object> kelas : daftarKelas) {
            comboKelas.addItem(kelas);
        }
        LOGGER.info("ComboBox kelas diisi dengan " + daftarKelas.size() + " item");
    }

    public void setSiswaList(List<Map<String, Object>> daftarSiswa) {
        comboSiswa.removeAllItems();
        for (Map<String, Object> siswa : daftarSiswa) {
            comboSiswa.addItem(siswa);
        }
        LOGGER.info("ComboBox siswa diisi dengan " + daftarSiswa.size() + " item");
    }

    public void setTableData(List<Map<String, Object>> daftarNilai) {
        tableModel.setRowCount(0);
        if (daftarNilai == null || daftarNilai.isEmpty()) {
            LOGGER.warning("Daftar nilai kosong, tabel tidak diisi");
            JOptionPane.showMessageDialog(this, "Tidak ada data nilai untuk kelas dan semester yang dipilih.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            tableModel.addRow(new Object[]{"-", "Tidak ada data", "-", "", "", "", 0.0});
            return;
        }
        for (Map<String, Object> nilai : daftarNilai) {
            Object[] row = new Object[7];
            row[0] = nilai.get("siswa_id");
            row[1] = nilai.get("nama");
            row[2] = nilai.get("nis");
            row[3] = nilai.get("nilai_uh") != null ? String.valueOf(nilai.get("nilai_uh")) : "";
            row[4] = nilai.get("nilai_uts") != null ? String.valueOf(nilai.get("nilai_uts")) : "";
            row[5] = nilai.get("nilai_uas") != null ? String.valueOf(nilai.get("nilai_uas")) : "";
            row[6] = nilai.get("nilai_akhir") != null ? String.format("%.2f", nilai.get("nilai_akhir")) : "";
            tableModel.addRow(row);
        }
        tableNilai.repaint();
        LOGGER.info("Tabel diisi dengan " + daftarNilai.size() + " baris data");
    }

    public void setLabelGuruMapel(String text) {
        labelGuruMapel.setText(text);
        LOGGER.info("Label Guru/Mapel diperbarui: " + text);
    }

    public JTable getTableNilai() {
        return tableNilai;
    }
}