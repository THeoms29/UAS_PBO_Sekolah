package NilaiSiswa;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.logging.Logger;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import main.MainController;

public class WaliKelasView extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(WaliKelasView.class.getName());
    private JComboBox<String> comboKelas;
    private JComboBox<String> comboSemester;
    private JButton btnExportPerKelas;
    private JButton btnExportPerSiswa;
    private JButton btnExportPDFAll;
    private JButton btnKeInputNilai;
    private JButton btnInputNilaiBaru;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblKelasInfo;
    private JLabel lblStatus;
    private Image contentBackground;
    private Color primaryColor = new Color(70, 130, 180);
    private Color buttonColor = new Color(0, 105, 180);
    private Color exportButtonColor = new Color(0, 128, 0); // Hijau tua
    private Color navButtonColor = new Color(139, 69, 19);   // Cokelat
    private WaliKelasController controller;
    private MainController mainController;

    public void setController(WaliKelasController controller) {
        this.controller = controller;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    public WaliKelasView() {
        loadBackgrounds();
        initUI();
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

    private void initUI() {
        setTitle("Aplikasi Rekap Nilai & Absensi - Wali Kelas");
        setSize(1250, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int width = getWidth();
                int height = getHeight();
                if (width <= 0 || height <= 0) {
                    return;
                }
                if (contentBackground != null) {
                    g.drawImage(contentBackground, 0, 0, width, height, this);
                } else if (g instanceof Graphics2D) {
                    Graphics2D g2d = (Graphics2D) g;
                    Color color1 = new Color(240, 248, 255);
                    Color color2 = new Color(230, 240, 250);
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, width, height);
                } else {
                    g.setColor(new Color(240, 248, 255));
                    g.fillRect(0, 0, width, height);
                }
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        lblKelasInfo = new JLabel(" ", SwingConstants.LEFT);
        lblKelasInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblKelasInfo.setForeground(primaryColor);

        JLabel titleLabel = new JLabel("REKAP NILAI DAN ABSENSI SISWA");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(lblKelasInfo, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(Box.createRigidArea(new Dimension(150, 0)), BorderLayout.EAST);

        // Top Control Panel (Kelas, Semester, Export Buttons)
        JPanel topControlPanel = new JPanel(new GridBagLayout());
        topControlPanel.setOpaque(false);
        topControlPanel.setPreferredSize(new Dimension(1200, 80));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblKelas = new JLabel("Kelas:");
        lblKelas.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKelas.setForeground(primaryColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        topControlPanel.add(lblKelas, gbc);

        comboKelas = new JComboBox<>();
        comboKelas.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1;
        gbc.gridy = 0;
        topControlPanel.add(comboKelas, gbc);

        JLabel lblSemester = new JLabel("Semester:");
        lblSemester.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSemester.setForeground(primaryColor);
        gbc.gridx = 2;
        gbc.gridy = 0;
        topControlPanel.add(lblSemester, gbc);

        comboSemester = new JComboBox<>(new String[]{"Semester 1", "Semester 2"});
        comboSemester.setPreferredSize(new Dimension(120, 30));
        gbc.gridx = 3;
        gbc.gridy = 0;
        topControlPanel.add(comboSemester, gbc);

        btnExportPerKelas = createStyledButton("Export PDF Per Kelas", exportButtonColor);
        btnExportPerKelas.setToolTipText("Export data untuk seluruh kelas ke PDF");
        addButtonShadow(btnExportPerKelas);
        gbc.gridx = 4;
        gbc.gridy = 0;
        topControlPanel.add(btnExportPerKelas, gbc);
        LOGGER.info("Button Export PDF Per Kelas added to topControlPanel");

        btnExportPerSiswa = createStyledButton("Export PDF Per Siswa", exportButtonColor);
        btnExportPerSiswa.setToolTipText("Export data untuk siswa terpilih ke PDF");
        addButtonShadow(btnExportPerSiswa);
        gbc.gridx = 5;
        gbc.gridy = 0;
        topControlPanel.add(btnExportPerSiswa, gbc);
        LOGGER.info("Button Export PDF Per Siswa added to topControlPanel");

        btnKeInputNilai = createStyledButton("Ke Input Nilai", navButtonColor);
        btnKeInputNilai.setToolTipText("Navigasi ke halaman input nilai");
        addButtonShadow(btnKeInputNilai);
        gbc.gridx = 6;
        gbc.gridy = 0;
        topControlPanel.add(btnKeInputNilai, gbc);
        LOGGER.info("Button Ke Input Nilai added to topControlPanel");

        // Panel Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 2),
                "Daftar Nilai dan Absensi",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                primaryColor));

        tableModel = new DefaultTableModel(
                new Object[]{"NIS", "Nama", "Nilai UH", "Nilai UTS", "Nilai UAS", "Nilai Akhir", "Hadir", "Izin", "Sakit", "Alfa", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1200, 400));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Button Panel (Export PDF All, Navigation Buttons)
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setPreferredSize(new Dimension(1200, 100));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        btnExportPDFAll = createStyledButton("Export PDF All", exportButtonColor);
        btnExportPDFAll.setToolTipText("Export semua data kelas ke PDF");
        btnExportPDFAll.setVisible(true);
        addButtonShadow(btnExportPDFAll);
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(btnExportPDFAll, gbc);
        LOGGER.info("Button Export PDF All added to buttonPanel");

        btnInputNilaiBaru = createStyledButton("Input Nilai Baru", navButtonColor);
        btnInputNilaiBaru.setToolTipText("Tambah data nilai baru");
        addButtonShadow(btnInputNilaiBaru);
        gbc.gridx = 1;
        gbc.gridy = 0;
        buttonPanel.add(btnInputNilaiBaru, gbc);
        LOGGER.info("Button Input Nilai Baru added to buttonPanel");

        // Panel Status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setOpaque(false);

        lblStatus = new JLabel("Status: Siap");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(primaryColor.darker());
        statusPanel.add(lblStatus);

        // Assemble Main Panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(topControlPanel, BorderLayout.PAGE_START);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(statusPanel, BorderLayout.PAGE_END);

        setContentPane(mainPanel);

        // Force revalidate and repaint
        mainPanel.revalidate();
        mainPanel.repaint();
        LOGGER.info("Main panel revalidated and repainted");
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

        LOGGER.info("Created styled button: " + text + " with foreground color: " + button.getForeground());
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

    public void setKelasList(String[] kelasList) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(kelasList);
        comboKelas.setModel(model);
        LOGGER.info("Kelas list set with " + kelasList.length + " items");
    }

    public void setTableData(Object[][] data) {
        tableModel.setRowCount(0);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
        LOGGER.info("Table data set with " + data.length + " rows");
    }

    public void setKelasInfo(String text) {
        lblKelasInfo.setText(text);
        LOGGER.info("Kelas info set to: " + text);
    }

    public void setStatusMessage(String text) {
        lblStatus.setText("Status: " + text);
        LOGGER.info("Status message set to: " + text);
    }

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

    public JButton getBtnExportPDFAll() {
        return btnExportPDFAll;
    }

    public JButton getBtnKeInputNilai() {
        return btnKeInputNilai;
    }

    public JButton getBtnInputNilaiBaru() {
        return btnInputNilaiBaru;
    }

    public JTable getTable() {
        return table;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }
}