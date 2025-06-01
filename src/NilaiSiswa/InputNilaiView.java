package NilaiSiswa;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map;

public class InputNilaiView extends JFrame {
    private JLabel labelGuruMapel, labelTitle;
    private JComboBox<Map<String, Object>> comboKelas, comboSiswa;
    private JComboBox<String> comboSemester;
    private JTextField txtNilaiUH, txtNilaiUTS, txtNilaiUAS;
    private JButton btnTambahNilai, btnSimpan;
    private JTable tableNilai;
    private DefaultTableModel tableModel;
    private NilaiController controller;
    private Image contentBackground;
    private Color primaryColor = new Color(70, 130, 180);
    private Color secondaryColor = new Color(34, 139, 34);
    private Color buttonColor = new Color(0, 105, 180); // Warna biru yang lebih gelap untuk tombol
    private Color saveButtonColor = new Color(34, 139, 34); // Warna hijau untuk tombol simpan

    private void loadBackgrounds() {
        try {
            ImageIcon contentIcon = new ImageIcon(getClass().getResource("/shared/Asset/BG1.JPEG"));
            if (contentIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                contentIcon = new ImageIcon("content_bg.jpg");
            }
            contentBackground = contentIcon.getImage();
        } catch (Exception e) {
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

        // Main content panel
        JPanel mainContentPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainContentPanel.setOpaque(false);

        // Left panel for input form
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(240, 248, 255, 200));
        leftPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(primaryColor, 2), 
            "Form Input Nilai",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            primaryColor));

        // Form panel
        JPanel panelForm = new JPanel();
        panelForm.setLayout(new BoxLayout(panelForm, BoxLayout.Y_AXIS));
        panelForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelForm.setOpaque(false);

        // Class panel
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

        // Semester panel
        JPanel panelSemester = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelSemester.setOpaque(false);
        JLabel lblSemester = new JLabel("Semester:");
        lblSemester.setPreferredSize(new Dimension(80, 20));
        panelSemester.add(lblSemester);
        
        comboSemester = new JComboBox<>(new String[]{"Semester 1", "Semester 2"});
        comboSemester.setPreferredSize(new Dimension(120, 25));
        panelSemester.add(comboSemester);
        panelForm.add(panelSemester);

        // Student panel
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

        // Score input panel
        JPanel panelNilaiInput = new JPanel(new GridLayout(3, 2, 10, 10));
        panelNilaiInput.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(primaryColor, 1), 
            "Input Nilai",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            primaryColor));
        panelNilaiInput.setOpaque(false);

        // UH score
        JPanel panelUH = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelUH.setOpaque(false);
        JLabel lblUH = new JLabel("Nilai UH:");
        lblUH.setPreferredSize(new Dimension(80, 20));
        panelUH.add(lblUH);
        
        txtNilaiUH = new JTextField();
        txtNilaiUH.setPreferredSize(new Dimension(60, 25));
        panelUH.add(txtNilaiUH);
        panelNilaiInput.add(panelUH);

        // UTS score
        JPanel panelUTS = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelUTS.setOpaque(false);
        JLabel lblUTS = new JLabel("Nilai UTS:");
        lblUTS.setPreferredSize(new Dimension(80, 20));
        panelUTS.add(lblUTS);
        
        txtNilaiUTS = new JTextField();
        txtNilaiUTS.setPreferredSize(new Dimension(60, 25));
        panelUTS.add(txtNilaiUTS);
        panelNilaiInput.add(panelUTS);

        // UAS score
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

        // Button panel
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
        
        panelForm.add(panelButton);

        leftPanel.add(panelForm, BorderLayout.CENTER);

        // Right panel for score table
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(240, 248, 255, 200));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(primaryColor, 2), 
            "Daftar Nilai Siswa",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            primaryColor));

        // Score table
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
        
        JScrollPane scrollPane = new JScrollPane(tableNilai);
        scrollPane.setPreferredSize(new Dimension(550, 500));
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Combine left and right panels
        mainContentPanel.add(leftPanel);
        mainContentPanel.add(rightPanel);

        // Main panel with background
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
        comboKelas.addActionListener(e -> {
            if (comboKelas.getSelectedItem() != null) {
                Map<String, Object> selectedKelas = (Map<String, Object>) comboKelas.getSelectedItem();
                controller.kelasDipilih((int) selectedKelas.get("id"));
            }
        });
        comboSemester.addActionListener(e -> {
            if (comboKelas.getSelectedItem() != null) {
                Map<String, Object> selectedKelas = (Map<String, Object>) comboKelas.getSelectedItem();
                controller.kelasDipilih((int) selectedKelas.get("id"));
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
        
        // Efek hover untuk tombol
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
        
        // Efek ketika tombol ditekan
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
        Map<String, Object> siswa = (Map<String, Object>) comboSiswa.getSelectedItem();
        if (siswa == null) {
            JOptionPane.showMessageDialog(this, "Pilih siswa terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int nilaiUH = Integer.parseInt(txtNilaiUH.getText());
            int nilaiUTS = Integer.parseInt(txtNilaiUTS.getText());
            int nilaiUAS = Integer.parseInt(txtNilaiUAS.getText());
            
            if (nilaiUH < 0 || nilaiUH > 100 || nilaiUTS < 0 || nilaiUTS > 100 || 
                nilaiUAS < 0 || nilaiUAS > 100) {
                JOptionPane.showMessageDialog(this, "Nilai harus antara 0-100!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double nilaiAkhir = (nilaiUH + nilaiUTS + nilaiUAS) / 3.0;
            boolean found = false;
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(siswa.get("siswa_id"))) {
                    tableModel.setValueAt(nilaiUH, i, 3);
                    tableModel.setValueAt(nilaiUTS, i, 4);
                    tableModel.setValueAt(nilaiUAS, i, 5);
                    tableModel.setValueAt(String.format("%.2f", nilaiAkhir), i, 6);
                    found = true;
                    break;
                }
            }

            if (!found) {
                tableModel.addRow(new Object[]{
                    siswa.get("siswa_id"),
                    siswa.get("nama"),
                    siswa.get("nis"),
                    nilaiUH,
                    nilaiUTS,
                    nilaiUAS,
                    String.format("%.2f", nilaiAkhir)
                });
            }

            txtNilaiUH.setText("");
            txtNilaiUTS.setText("");
            txtNilaiUAS.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Nilai harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simpanSemuaNilai() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data nilai untuk disimpan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Map<String, Object>> dataNilai = new ArrayList<>();
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Map<String, Object> nilai = new HashMap<>();
            nilai.put("siswa_id", tableModel.getValueAt(i, 0));
            nilai.put("nilai_uh", tableModel.getValueAt(i, 3));
            nilai.put("nilai_uts", tableModel.getValueAt(i, 4));
            nilai.put("nilai_uas", tableModel.getValueAt(i, 5));
            dataNilai.add(nilai);
        }

        boolean berhasil = controller.simpanNilai(dataNilai);
        if (berhasil) {
            JOptionPane.showMessageDialog(this, "Data nilai berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            if (comboKelas.getSelectedItem() != null) {
                Map<String, Object> selectedKelas = (Map<String, Object>) comboKelas.getSelectedItem();
                controller.kelasDipilih((int) selectedKelas.get("id"));
            }
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data nilai!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getSelectedSemester() {
        return comboSemester.getSelectedItem().toString().replace("Semester ", "");
    }

    public void setController(NilaiController controller) {
        this.controller = controller;
    }

    public void setLabelGuruMapel(String text) {
        labelGuruMapel.setText(text);
    }

    public void setComboBoxKelas(List<Map<String, Object>> kelasList) {
        comboKelas.removeAllItems();
        for (Map<String, Object> kelas : kelasList) {
            comboKelas.addItem(kelas);
        }
    }

    public void setSiswaList(List<Map<String, Object>> siswaList) {
        comboSiswa.removeAllItems();
        for (Map<String, Object> siswa : siswaList) {
            comboSiswa.addItem(siswa);
        }
    }

    public void setTableData(List<Map<String, Object>> data) {
        tableModel.setRowCount(0);
        for (Map<String, Object> row : data) {
            Object nilaiUH = row.get("nilai_uh") != null ? row.get("nilai_uh") : 0;
            Object nilaiUTS = row.get("nilai_uts") != null ? row.get("nilai_uts") : 0;
            Object nilaiUAS = row.get("nilai_uas") != null ? row.get("nilai_uas") : 0;
            double nilaiAkhir = (Double.parseDouble(nilaiUH.toString()) + 
                               Double.parseDouble(nilaiUTS.toString()) + 
                               Double.parseDouble(nilaiUAS.toString())) / 3.0;

            tableModel.addRow(new Object[]{
                row.get("siswa_id"),
                row.get("nama"),
                row.get("nis"),
                nilaiUH,
                nilaiUTS,
                nilaiUAS,
                String.format("%.2f", nilaiAkhir)
            });
        }
    }
}