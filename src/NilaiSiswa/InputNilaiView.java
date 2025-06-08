package NilaiSiswa;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class InputNilaiView extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(InputNilaiView.class.getName());
    private JLabel labelGuruMapel, labelTitle;
    private JComboBox<Map<String, Object>> comboKelas, comboSiswa;
    private JComboBox<String> comboSemester;
    private JTextField txtNilaiUH, txtNilaiUTS, txtNilaiUAS;
    private JButton btnTambahNilai, btnSimpan, btnKeWaliKelas;
    private JTable tableNilai;
    private DefaultTableModel tableModel;
    private NilaiController controller;
    private Image contentBackground;
    private Color primaryColor = new Color(70, 130, 180);
    private Color secondaryColor = new Color(34, 139, 34);
    private Color buttonColor = new Color(0, 105, 180);
    private Color saveButtonColor = new Color(34, 139, 34);
    private Color navButtonColor = new Color(139, 69, 19);
    private Set<Integer> modifiedRows = new HashSet<>();

    public void setController(NilaiController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller tidak boleh null");
        }
        this.controller = controller;
        LOGGER.info("Controller diatur untuk InputNilaiView: " + controller.getClass().getName());
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
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

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

        // Panel Konten Utama
        JPanel mainContentPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainContentPanel.setOpaque(false);

        // Panel Kiri untuk Form Input
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
        panelForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
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
                    value = ((Map<?, ?>) value).get("nama_kelas");
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
                    value = ((Map<?, ?>) value).get("nama") + " - " + ((Map<?, ?>) value).get("nis");
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
        panelNilaiInput.setOpaque(false);

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

        btnTambahNilai = createStyledButton("Tambah/Update Nilai", buttonColor);
        addButtonShadow(btnTambahNilai);
        panelButton.add(btnTambahNilai);

        btnSimpan = createStyledButton("Simpan ke Database", saveButtonColor);
        addButtonShadow(btnSimpan);
        panelButton.add(btnSimpan);

        btnKeWaliKelas = createStyledButton("Ke Wali Kelas", navButtonColor);
        addButtonShadow(btnKeWaliKelas);
        panelButton.add(btnKeWaliKelas);

        panelForm.add(panelButton);

        leftPanel.add(panelForm, BorderLayout.CENTER);

        // Panel Kanan untuk Tabel Nilai
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
                return column >= 3 && column <= 5;
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

        tableNilai.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column >= 3 && column <= 5 && row >= 0) {
                modifiedRows.add(row);
                try {
                    double uh = tableNilai.getValueAt(row, 3) != null ? Double.parseDouble(tableNilai.getValueAt(row, 3).toString()) : 0;
                    double uts = tableNilai.getValueAt(row, 4) != null ? Double.parseDouble(tableNilai.getValueAt(row, 4).toString()) : 0;
                    double uas = tableNilai.getValueAt(row, 5) != null ? Double.parseDouble(tableNilai.getValueAt(row, 5).toString()) : 0;
                    double nilaiAkhir = (uh + uts + uas) / 3.0;
                    tableNilai.setValueAt(String.format("%.2f", nilaiAkhir), row, 6);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Nilai harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
                    LOGGER.severe("Gagal menghitung nilai akhir: " + ex.getMessage());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableNilai);
        scrollPane.setPreferredSize(new Dimension(550, 500));
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Gabungkan Panel Kiri dan Kanan
        mainContentPanel.add(leftPanel);
        mainContentPanel.add(rightPanel);

        // Panel Utama dengan Latar Belakang
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

        // Event Listeners
        btnTambahNilai.addActionListener(e -> tambahAtauUpdateNilai());
        btnSimpan.addActionListener(e -> simpanSemuaNilai());
        btnKeWaliKelas.addActionListener(e -> bukaWaliKelasView());
        comboKelas.addActionListener(e -> {
            if (controller != null && comboKelas.getSelectedItem() != null) {
                Map<String, Object> selectedKelas = (Map<String, Object>) comboKelas.getSelectedItem();
                String semester = comboSemester.getSelectedItem() != null ? 
                    comboSemester.getSelectedItem().toString().replace("Semester ", "") : "1";
                controller.kelasDipilih((String) selectedKelas.get("nama_kelas"), semester);
                LOGGER.info("Kelas dipilih: " + selectedKelas.get("nama_kelas"));
            }
        });
        comboSemester.addActionListener(e -> {
            if (controller != null && comboKelas.getSelectedItem() != null) {
                Map<String, Object> selectedKelas = (Map<String, Object>) comboKelas.getSelectedItem();
                String semester = comboSemester.getSelectedItem() != null ? 
                    comboSemester.getSelectedItem().toString().replace("Semester ", "") : "1";
                controller.kelasDipilih((String) selectedKelas.get("nama_kelas"), semester);
                LOGGER.info("Semester dipilih: " + comboSemester.getSelectedItem());
            }
        });
    }

    private void bukaWaliKelasView() {
        SwingUtilities.invokeLater(() -> {
            try {
                LOGGER.info("Membuka WaliKelasView...");
                WaliKelasView waliKelasView = new WaliKelasView();
                WaliKelasModel waliKelasModel = new WaliKelasModel();
                new WaliKelasController(waliKelasView, waliKelasModel);
                waliKelasView.setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal membuka fitur Wali Kelas: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Gagal membuka WaliKelasView: " + ex.getMessage());
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

    private void tambahAtauUpdateNilai() {
    if (controller == null) {
        JOptionPane.showMessageDialog(this, "Controller belum diinisialisasi!", "Error", JOptionPane.ERROR_MESSAGE);
        LOGGER.severe("Controller null saat tambahAtauUpdateNilai");
        return;
    }

    Map<String, Object> siswa = (Map<String, Object>) comboSiswa.getSelectedItem();
    if (siswa == null || siswa.get("siswa_id") == null) {
        JOptionPane.showMessageDialog(this, "Pilih siswa terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        LOGGER.warning("Siswa tidak dipilih atau siswa_id null");
        return;
    }

    if (txtNilaiUH.getText().trim().isEmpty() || txtNilaiUTS.getText().trim().isEmpty() || txtNilaiUAS.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Semua nilai harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        LOGGER.warning("Input nilai kosong");
        return;
    }

    try {
        int nilaiUH = Integer.parseInt(txtNilaiUH.getText().trim());
        int nilaiUTS = Integer.parseInt(txtNilaiUTS.getText().trim());
        int nilaiUAS = Integer.parseInt(txtNilaiUAS.getText().trim());

        if (nilaiUH < 0 || nilaiUH > 100 || nilaiUTS < 0 || nilaiUTS > 100 || nilaiUAS < 0 || nilaiUAS > 100) {
            JOptionPane.showMessageDialog(this, "Nilai harus antara 0-100!", "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Nilai di luar rentang: UH=" + nilaiUH + ", UTS=" + nilaiUTS + ", UAS=" + nilaiUAS);
            return;
        }

        int siswaId = (int) siswa.get("siswa_id");
        String semester = comboSemester.getSelectedItem().toString().replace("Semester ", "");

        // Panggil controller untuk menyimpan nilai
        boolean berhasil = controller.testInputNilai(siswaId, nilaiUH, nilaiUTS, nilaiUAS, semester);
        
        if (berhasil) {
            txtNilaiUH.setText("");
            txtNilaiUTS.setText("");
            txtNilaiUAS.setText("");
            JOptionPane.showMessageDialog(this, "Nilai berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Nilai harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        LOGGER.severe("Gagal parsing nilai: " + e.getMessage());
    }
}

    private int findSiswaRow(int siswaId) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object tableSiswaId = tableModel.getValueAt(i, 0);
            if (tableSiswaId != null && tableSiswaId instanceof Integer && (int) tableSiswaId == siswaId) {
                return i;
            }
        }
        return -1;
    }

    private void simpanSemuaNilai() {
        if (controller == null) {
            JOptionPane.showMessageDialog(this, "Controller belum diinisialisasi!", "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Controller null saat simpanSemuaNilai");
            return;
        }

        if (modifiedRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data baru atau yang diubah untuk disimpan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            LOGGER.warning("Tidak ada perubahan untuk disimpan");
            return;
        }

        String semester = comboSemester.getSelectedItem() != null ? 
            comboSemester.getSelectedItem().toString().replace("Semester ", "") : null;
        if (semester == null || (!semester.equals("1") && !semester.equals("2"))) {
            JOptionPane.showMessageDialog(this, "Pilih semester yang valid!", "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Semester tidak valid: " + semester);
            return;
        }

        List<Map<String, Object>> dataNilai = new ArrayList<>();
        for (Integer row : modifiedRows) {
            try {
                Object siswaIdObj = tableModel.getValueAt(row, 0);
                Object nilaiUHObj = tableModel.getValueAt(row, 3);
                Object nilaiUTSObj = tableModel.getValueAt(row, 4);
                Object nilaiUASObj = tableModel.getValueAt(row, 5);

                if (siswaIdObj == null || !(siswaIdObj instanceof Integer)) {
                    JOptionPane.showMessageDialog(this, "ID siswa tidak valid pada baris " + (row + 1), "Error", JOptionPane.ERROR_MESSAGE);
                    LOGGER.severe("ID siswa null atau bukan integer pada baris " + (row + 1));
                    return;
                }
                int siswaId = (int) siswaIdObj;

                if (nilaiUHObj == null || nilaiUTSObj == null || nilaiUASObj == null) {
                    JOptionPane.showMessageDialog(this, "Nilai tidak lengkap pada baris " + (row + 1), "Error", JOptionPane.ERROR_MESSAGE);
                    LOGGER.severe("Nilai tidak lengkap pada baris " + (row + 1));
                    return;
                }

                int nilaiUH = parseNilai(nilaiUHObj, row, "UH");
                int nilaiUTS = parseNilai(nilaiUTSObj, row, "UTS");
                int nilaiUAS = parseNilai(nilaiUASObj, row, "UAS");

                if (nilaiUH == -1 || nilaiUTS == -1 || nilaiUAS == -1) {
                    return;
                }

                if (nilaiUH < 0 || nilaiUH > 100 || nilaiUTS < 0 || nilaiUTS > 100 || nilaiUAS < 0 || nilaiUAS > 100) {
                    JOptionPane.showMessageDialog(this, "Nilai harus antara 0-100 pada baris " + (row + 1), "Error", JOptionPane.ERROR_MESSAGE);
                    LOGGER.severe("Nilai di luar rentang pada baris " + (row + 1));
                    return;
                }

                Map<String, Object> nilai = new HashMap<>();
                nilai.put("siswa_id", siswaId);
                nilai.put("nilai_uh", nilaiUH);
                nilai.put("nilai_uts", nilaiUTS);
                nilai.put("nilai_uas", nilaiUAS);
                nilai.put("semester", semester);
                dataNilai.add(nilai);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error memproses data pada baris " + (row + 1) + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Error memproses data pada baris " + (row + 1) + ": " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        boolean berhasil = controller.simpanNilai(dataNilai);
        if (berhasil) {
            JOptionPane.showMessageDialog(this, "Data nilai berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            modifiedRows.clear();
            if (comboKelas.getSelectedItem() != null) {
                Map<String, Object> selectedKelas = (Map<String, Object>) comboKelas.getSelectedItem();
                controller.kelasDipilih((String) selectedKelas.get("nama_kelas"), semester);
                LOGGER.info("Data nilai disimpan dan tabel diperbarui untuk kelas: " + selectedKelas.get("nama_kelas"));
            }
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data nilai ke database!", "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal menyimpan data nilai ke database");
        }
    }

    private int parseNilai(Object nilai, int row, String jenis) {
        try {
            if (nilai == null || nilai.toString().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nilai " + jenis + " kosong pada baris " + (row + 1), "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Nilai " + jenis + " kosong pada baris " + (row + 1));
                return -1;
            }
            return Integer.parseInt(nilai.toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Nilai " + jenis + " harus berupa angka pada baris " + (row + 1), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal parsing nilai " + jenis + " pada baris " + (row + 1) + ": " + e.getMessage());
            return -1;
        }
    }

    public void setComboBoxKelas(List<Map<String, Object>> kelasList) {
        DefaultComboBoxModel<Map<String, Object>> model = new DefaultComboBoxModel<>();
        for (Map<String, Object> kelas : kelasList) {
            model.addElement(kelas);
        }
        comboKelas.setModel(model);
    }

    public void setSiswaList(List<Map<String, Object>> siswaList) {
        DefaultComboBoxModel<Map<String, Object>> model = new DefaultComboBoxModel<>();
        for (Map<String, Object> siswa : siswaList) {
            model.addElement(siswa);
        }
        comboSiswa.setModel(model);
    }

    public void setTableData(List<Map<String, Object>> nilaiList) {
        tableModel.setRowCount(0);
        for (Map<String, Object> data : nilaiList) {
            tableModel.addRow(new Object[]{
                    data.get("siswa_id"),
                    data.get("nama"),
                    data.get("nis"),
                    data.get("nilai_uh"),
                    data.get("nilai_uts"),
                    data.get("nilai_uas"),
                    data.get("nilai_akhir")
            });
        }
        LOGGER.info("Tabel nilai diperbarui dengan " + nilaiList.size() + " baris data");
    }

    public void setLabelGuruMapel(String text) {
        labelGuruMapel.setText(text);
    }

    public JComboBox<Map<String, Object>> getComboKelas() {
        return comboKelas;
    }

    public JComboBox<String> getComboSemester() {
        return comboSemester;
    }
}